package net.farflat;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

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

    public static class TerrainGenerator extends Thread {
        private Chunk target;
        private long firstBlockPos;
        private long noiseSeed;

        public TerrainGenerator(Chunk target, long firstBlockPos, long noiseSeed) {
            this.target = target;
            this.firstBlockPos = firstBlockPos;
            this.noiseSeed = noiseSeed;
        }

        public void setTarget(Chunk target) {
            this.target = target;
        }

        public void setFirstBlockPos(long firstBlockPos) {
            this.firstBlockPos = firstBlockPos;
        }

        public void setNoiseSeed(long noiseSeed) {
            this.noiseSeed = noiseSeed;
        }

        public void run() {
            for (long x = firstBlockPos; x < firstBlockPos + target.chunkWidth; x++) {
                new TerrainSliceGenerator(target, x, noiseSeed).run();
            }
        }

        class TerrainSliceGenerator extends Thread {
            private Chunk target;
            private final long x;
            private long noiseSeed;

            public TerrainSliceGenerator(Chunk target, long firstBlockPos, long noiseSeed) {
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
                for (int i = 0; i < target.chunkHeight / 2 - 1; i++) {
                    target.setBlock((int) (x % target.chunkWidth), i, 5);
                }
                Function<Long, Integer> biomeIdSupplier = p -> target.getBiomeId(p, new PerlinNoise(new Random(noiseSeed + 1)), target.biomeSeed);
                Function<Long, Integer> noiseSupplier = p -> Math.max(0, (int) target.getBiomeHeight(p, biomeIdSupplier.apply(p), new PerlinNoise(new Random(noiseSeed))));
                Function<Long, Double> continentalnessNoiseSupplier = p -> Math.max(0, Math.min(1, new PerlinNoise(new Random(noiseSeed + 31311)).perlin(p / (Math.PI * 50), 0.5, 2, 2) / 2. + 1));
                Function<Long, Integer> heightOffsetDueToContinentalnessSupplier = p -> (int) (Math.pow(1.2 * continentalnessNoiseSupplier.apply(p), 0.5) * 10 - 0.5);
                Function<Point2, Double> twoDimensionalCaveNoiseSupplier = p -> SimplexNoise.noise(p.x / 53.101 + 0x3f951e * noiseSeed, p.y / 52.982 + noiseSeed + 0xfe9f1e * noiseSeed) + p.y / ((double) target.chunkHeight);
                Function<Point2, Double> twoDimensionalTravelCaveNoiseSupplier = p -> SimplexNoise.noise(p.x / 53.101 + .12 + 0x6f951e * noiseSeed, p.y / 52.982 + .12 + 0x9e121e * noiseSeed) + p.y / ((double) target.chunkHeight) + .2 + Math.random()/100;
                int height = noiseSupplier.apply(x) + heightOffsetDueToContinentalnessSupplier.apply(x);
//                int height = (int) (heightOffsetDueToContinentalnessSupplier.apply(x)*30+100);
                for (int i = 0; i < height; i++) {
                    target.setBlock((int) (x % target.chunkWidth), i, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x), List.of(1, 2, 3)).get(0));
                }
                int dirtHeight = 4;
                for (int i = height; i < height + dirtHeight; i++) {
                    target.setBlock((int) (x % target.chunkWidth), i, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x), List.of(1, 2, 3)).get(1));
                }
                target.setBlock((int) (x % target.chunkWidth), height + dirtHeight, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x), List.of(1, 2, 3)).get(2));
                for (int y = 0; y < target.chunkHeight; y++) {
                    if (BlockInfo.getBlockInfo(target.getBlock((int) (x % target.chunkWidth), y)).solid() && twoDimensionalCaveNoiseSupplier.apply(new Point2(x, y)) < 0 || Math.abs(twoDimensionalTravelCaveNoiseSupplier.apply(new Point2(x, y))) < .1)
                        target.setBlock((int) (x % target.chunkWidth), y, 0);
                }
                int bottomBorderHeight = 7;
                for (int i = 1; i < bottomBorderHeight; i++) {
                    target.setBlock((int) (x % target.chunkWidth), i, Math.random() < Math.pow(((float) i) / bottomBorderHeight, 0.5) ? 1 : 4);
                }
                target.setBlock((int) (x % target.chunkWidth), 0, 4);
            }
        }
    }

    /**
     * 0: underground block ("stone") | 1: top layering block ("dirt") | 2: covering block ("grass block")
     */
    private static final Map<Integer, List<Integer>> biomeBlocks = Map.of(0, List.of(1, 2, 3), 1, List.of(1, 1, 1));

    private static final int biomes = 3;

    int getBiomeId(Long x, PerlinNoise noise, long biomeSeed) {
//        Random rand = new Random(biomeSeed + (x / 8));
//        return lerp(rand.nextDouble() * biomes, );
        return noise.noise((float) (x / (Math.PI * 50))) < 0 ? 0 : 1;
    }

    double getBiomeHeight(Long x, int biomeId, PerlinNoise noise) {
        return switch (biomeId) {
//            case 0 -> noise.perlin(x / (Math.PI * 10), 0.5, 8, 2) * 2. + 64.;
            case 0 -> SimplexNoise.noise(x / 171.103, 0) * 2. + chunkHeight / 2.;
            case 1 -> noise.perlin(x / (Math.PI * 10), 0.5, 8, 2) * 10. + chunkHeight / 2.;
            default -> Math.random() * this.chunkHeight;
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
        if (isInChunkBounds(x, y - (x / chunkWidth))) {
            hasBeenAccessed = true;
            blocks[posToIndex(x, y - (x / chunkWidth))] = blockType;
        }
    }
}
