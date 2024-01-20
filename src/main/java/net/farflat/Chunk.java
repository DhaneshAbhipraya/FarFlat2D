package net.farflat;

import java.util.*;
import java.util.function.Function;

import static net.farflat.Mathematical.lerp;

public class Chunk {
    private final int chunkWidth;
    private final int chunkHeight;
    private final int[] blocks;
    private long biomeSeed;
    public boolean hasBeenAccessed;

    public Chunk(int chunkWidth, int chunkHeight, long biomeSeed) {
        this.chunkWidth = chunkWidth;
        this.chunkHeight = chunkHeight;
        blocks = new int[chunkWidth * chunkHeight];
        this.biomeSeed = biomeSeed;
    }

    public void resetAccessedCheck() {
        hasBeenAccessed = false;
    }

    private int posToIndex(long x, long y) {
        return (int) (x + y * chunkWidth);
    }

    @Deprecated
    public void generate(int firstBlockPos, PerlinNoise noise) {
        for (int x = firstBlockPos; x < firstBlockPos + chunkWidth; x++) {
            Function<Integer, Integer> noiseSupplier = p->Math.max(0, (int) getBiomeHeight(p, getBiomeId(p, noise, biomeSeed), noise));
            int height = noiseSupplier.apply(x);
            for (int i = 0; i < height; i++) {
                setBlock(x, i, 1);
            }

            int dirtHeight = 4;
            for (int i = height; i < height + dirtHeight; i++) {
                setBlock(x, i, 2);
            }
            setBlock(x, height + dirtHeight, 3);
            int bottomBorderHeight = 7;
            for (int i = 1; i < bottomBorderHeight; i ++) {
                setBlock(x, i, Math.random() < Math.pow(((float)i)/bottomBorderHeight, 0.5) ? -1 : 4);
            }
            setBlock(x, 0, 4);
        }
    }

    public static class TerrainGenerator extends Thread {
        private Chunk target;
        private int firstBlockPos;
        private long noiseSeed;

        public TerrainGenerator(Chunk target, int firstBlockPos, long noiseSeed) {
            this.target = target;
            this.firstBlockPos = firstBlockPos;
            this.noiseSeed = noiseSeed;
        }

        public void setTarget(Chunk target) {
            this.target = target;
        }

        public void setFirstBlockPos(int firstBlockPos) {
            this.firstBlockPos = firstBlockPos;
        }

        public void setNoiseSeed(long noiseSeed) {
            this.noiseSeed = noiseSeed;
        }

        public void run() {
            for (int x = firstBlockPos; x < firstBlockPos + target.chunkWidth; x++) {
                new TerrainSliceGenerator(target, x, noiseSeed).run();
            }
        }

        class TerrainSliceGenerator extends Thread {
            private Chunk target;
            private final int x;
            private long noiseSeed;

            public TerrainSliceGenerator(Chunk target, int firstBlockPos, long noiseSeed) {
                this.target = target;
                this.x = firstBlockPos;
                this.noiseSeed = noiseSeed;
            }

            public void setTarget(Chunk target) {
                this.target = target;
            }

            public void setNoiseSeed(long noiseSeed) {
                this.noiseSeed = noiseSeed;
            }

            public void run() {
                for (int i = 0; i < 63; i++) {
                    target.setBlock(x, i, 5);
                }
                Function<Integer, Integer> biomeIdSupplier = p->target.getBiomeId(p, new PerlinNoise(new Random(noiseSeed+1)), target.biomeSeed);
                Function<Integer, Integer> noiseSupplier = p->Math.max(0, (int) target.getBiomeHeight(p, biomeIdSupplier.apply(p), new PerlinNoise(new Random(noiseSeed))));
                Function<Integer, Double> continentalnessNoiseSupplier = p->Math.max(0, Math.min(1, new PerlinNoise(new Random(noiseSeed+31311)).perlin(p/(Math.PI*50), 0.5, 2, 2)/2.+1));
                Function<Integer, Integer> heightOffsetDueToContinentalnessSupplier = p-> (int) (Math.pow(1.2*continentalnessNoiseSupplier.apply(p),0.5)*10-0.5);
//                Function<Point2, Integer> twoDimensionalCaveNoiseSupplier =
                int height = noiseSupplier.apply(x)+heightOffsetDueToContinentalnessSupplier.apply(x);
//                int height = (int) (heightOffsetDueToContinentalnessSupplier.apply(x)*30+100);
                for (int i = 0; i < height; i++) {
//                    if (twoDimensionalCaveNoiseSupplier)
                        target.setBlock(x, i, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x), List.of(1,2,3)).get(0));
                }
                int dirtHeight = 4;
                for (int i = height; i < height + dirtHeight; i++) {
                    target.setBlock(x, i, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x), List.of(1,2,3)).get(1));
                }
                target.setBlock(x, height + dirtHeight, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x), List.of(1,2,3)).get(2));
                int bottomBorderHeight = 7;
                for (int i = 1; i < bottomBorderHeight; i ++) {
                    target.setBlock(x, i, Math.random() < Math.pow(((float)i)/bottomBorderHeight, 0.5) ? -1 : 4);
                }
                target.setBlock(x, 0, 4);
            }
        }
    }

    /**
     * 0: underground block ("stone") | 1: top layering block ("dirt") | 2: covering block ("grass block")
     */
    private static final Map<Integer, List<Integer>> biomeBlocks = Map.of(0, List.of(1, 2, 3), 1, List.of(1, 1, 1));

    private static final int biomes = 3;

    int getBiomeId(int x, PerlinNoise noise, long biomeSeed) {
//        Random rand = new Random(biomeSeed + (x / 8));
//        return lerp(rand.nextDouble() * biomes, );
        return noise.noise((float) (x/(Math.PI*50))) < 0 ? 0 : 1;
    }

    double getBiomeHeight(int x, int biomeId, PerlinNoise noise) {
        return switch (biomeId) {
//            case 0 -> noise.perlin(x / (Math.PI * 10), 0.5, 8, 2) * 2. + 64.;
            case 0 -> SimplexNoise.noise(x/171.103, 0) * 2. + 64.;
            case 1 -> noise.perlin(x / (Math.PI * 10), 0.5, 8, 2) * 10. + 64.;
            default -> Math.random()*this.chunkHeight;
        };
    }

    public boolean isInChunkBounds(int x, int y) {
        return posToIndex(x, y) >= 0 && posToIndex(x, y) < blocks.length;
    }

    public int getBlock(int x, int y) {
        if (isInChunkBounds(x, y)) {
            hasBeenAccessed = true;
            return blocks[posToIndex(x, y)];
        }
        return 0;
    }

    public void setBlock(int x, int y, int blockType) {
        if (blockType < 0) return;
        if (isInChunkBounds(x, y-(x/chunkWidth))) {
            hasBeenAccessed = true;
            blocks[posToIndex(x, y - (x / chunkWidth))] = blockType;
        }
    }
}
