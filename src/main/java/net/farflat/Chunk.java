package net.farflat;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class Chunk {
    private final int chunkWidth;
    private final int chunkHeight;
    private final int[] blocks;
    private final BigInteger firstBlockPos;
    public boolean hasBeenAccessed;

    boolean generating = true;

    public Chunk(int chunkWidth, int chunkHeight, BigInteger firstBlockPos) {
        this.chunkWidth = chunkWidth;
        this.chunkHeight = chunkHeight;
        blocks = new int[chunkWidth * chunkHeight];
        this.firstBlockPos = firstBlockPos;
    }

    public void resetAccessedCheck() {
        hasBeenAccessed = false;
    }

    private int posToIndex(long x, long y) {
        return (int) (x + y * chunkWidth);
    }

    public static class TerrainGenerator extends Thread {
        private final Chunk target;
        private final BigInteger firstBlockPos;
        private final long noiseSeed;

        public TerrainGenerator(Chunk target, long noiseSeed) {
            this.target = target;
            this.firstBlockPos = target.firstBlockPos;
            this.noiseSeed = noiseSeed;
        }

        public void run() {
            for (BigInteger x = firstBlockPos; x.compareTo(firstBlockPos.add(BigInteger.valueOf(target.chunkWidth))) < 0; x=x.add(BigInteger.ONE)) {
                new TerrainSliceGenerator(target, x, noiseSeed).run();
            }
            target.generating = false;
        }

        static class TerrainSliceGenerator {
            private Chunk target;
            private final BigInteger x;
            private long noiseSeed;

            public TerrainSliceGenerator(Chunk target, BigInteger firstBlockPos, long noiseSeed) {
                this.target = target;
                this.x = firstBlockPos;
                this.noiseSeed = noiseSeed;
            }

            public void run() {
                if (StoryEffects.enable_infinite_quadrillion_paradise && x.compareTo(new BigInteger("1000000000000000")) >= 0) {
                    int temp1 = x.mod(BigInteger.valueOf(target.chunkWidth)).intValue();
                    for (int i = 0; i < target.chunkHeight / 2 - 1; i++) {
                        target.setBlockForGenerator(temp1, i, 2);
                    }
                    if (x.compareTo(target.firstBlockPos) == 0)
                        for (int i = target.chunkHeight / 2 - 1 - 10; i < target.chunkHeight / 2 - 2; i++) {
                            target.setBlockForGenerator(temp1, i, 0);
                        }
                    target.setBlockForGenerator(1, 5 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(2, 4 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(3, 5 + (target.chunkHeight / 2 - 1) - 7, 0);

                    target.setBlockForGenerator(5, 4 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(6, 3 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(7, 4 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(8, 3 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(9, 4 + (target.chunkHeight / 2 - 1) - 7, 0);

                    target.setBlockForGenerator(11, 5 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(12, 4 + (target.chunkHeight / 2 - 1) - 7, 0);
                    target.setBlockForGenerator(13, 5 + (target.chunkHeight / 2 - 1) - 7, 0);
                    return;
                }
                int temp1 = x.mod(BigInteger.valueOf(target.chunkWidth)).intValue();
                for (int i = 0; i < target.chunkHeight / 2 - 1; i++) {
                    target.setBlockForGenerator(temp1, i, 5);
                }
                Function<Double, Integer> biomeIdSupplier = p -> target.getBiomeId(Math.round(p), new PerlinNoise(new Random(noiseSeed + 1)));
                Function<Double, Integer> noiseSupplier = p -> Math.max(0, (int) target.getBiomeHeight(p, biomeIdSupplier.apply(p), new PerlinNoise(new Random(noiseSeed)), new Noise(new Random(noiseSeed))));
                Function<Double, Double> continentalnessNoiseSupplier = p -> Math.max(0, Math.min(1, new PerlinNoise(new Random(noiseSeed + 31311)).perlin(p / (Math.PI * 50), 0.5, 2, 2) / 2. + 1));
                Function<Double, Integer> heightOffsetDueToContinentalnessSupplier = p -> (int) (Math.pow(1.2 * continentalnessNoiseSupplier.apply(p), 0.5) * 10 - 0.5);
                Function<Point2, Double> twoDimensionalCaveNoiseSupplier = p -> SimplexNoise.noise(p.x / 53.101 + 0x3f951e * noiseSeed, p.y / 52.982 + noiseSeed + 0xfe9f1e * noiseSeed) + p.y / ((double) target.chunkHeight);
                Function<Point2, Double> twoDimensionalTravelCaveNoiseSupplier = p -> SimplexNoise.noise(p.x / 53.101 + .12 + 0x6f951e * noiseSeed, p.y / 52.982 + .12 + 0x9e121e * noiseSeed) + p.y / ((double) target.chunkHeight) + .2 + Math.random() / 100;
                int height = noiseSupplier.apply(x.doubleValue()) + heightOffsetDueToContinentalnessSupplier.apply(x.doubleValue());
//                int height = (int) (heightOffsetDueToContinentalnessSupplier.apply(x)*30+100);
                for (int i = 0; i < height; i++) {
                    target.setBlockForGenerator(temp1, i, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x.doubleValue()), List.of(1, 2, 3)).get(0));
                }
                int dirtHeight = 4;
                for (int i = height; i < height + dirtHeight; i++) {
                    target.setBlockForGenerator(temp1, i, biomeBlocks.getOrDefault(biomeIdSupplier.apply(x.doubleValue()), List.of(1, 2, 3)).get(1));
                }
                int blocktype = biomeBlocks.getOrDefault(biomeIdSupplier.apply(x.doubleValue()), List.of(1, 2, 3)).get(2);
                if (height + dirtHeight < target.chunkHeight / 2 - 1)
                    blocktype = biomeBlocks.getOrDefault(biomeIdSupplier.apply(x.doubleValue()), List.of(1, 2, 3)).get(1);
                target.setBlockForGenerator(temp1, height + dirtHeight, blocktype);
                for (int y = 0; y < target.chunkHeight; y++) {
                    if (BlockInfo.getBlockInfo(target.getBlock(temp1, y)).solid() && twoDimensionalCaveNoiseSupplier.apply(new Point2(x.doubleValue(), y)) < 0 || Math.abs(twoDimensionalTravelCaveNoiseSupplier.apply(new Point2(x.doubleValue(), y))) < .1)
                        target.setBlockForGenerator(temp1, y, 0);
                }
                int bottomBorderHeight = 7;
                for (int i = 1; i < bottomBorderHeight; i++) {
                    target.setBlockForGenerator(temp1, i, Math.random() < Math.pow(((float) i) / bottomBorderHeight, 0.5) ? 1 : 4);
                }
                target.setBlockForGenerator(temp1, 0, 4);
            }
        }
    }

    /**
     * 0: underground block ("stone") | 1: top layering block ("dirt") | 2: covering block ("grass block")
     */
    private static final Map<Integer, List<Integer>> biomeBlocks = Map.of(0, List.of(1, 2, 3), 1, List.of(1, 1, 1));

    int getBiomeId(Long x, PerlinNoise noise) {
//        Random rand = new Random(biomeSeed + (x / 8));
//        return lerp(rand.nextDouble() * biomes, );
        return noise.noise((float) (x / (Math.PI * 50))) < 0 ? 0 : 1;
    }

    double getBiomeHeight(Double x, int biomeId, PerlinNoise perlinNoise, Noise noise) {
        DoubleIntFunctionDouble getNoiseValue = (pos, octaves) -> {
            double total = 0;
            double frequency = 0.00522F;
            double amplitude = 1;
            double maxValue = 0;

            for (int i = 0; i < octaves; i++) {
                double octave = noise.getValue(x * frequency, chunkHeight / 2.) * amplitude;
                total += octave;
                maxValue += amplitude;
            }

            return total / maxValue;
        };
        double extreme = getExtreme(x, perlinNoise);
        double low = getNoiseValue.get(x * 684.412, 16) * extreme + chunkHeight / 2.;
        double high = getNoiseValue.get(x * 684.412, 16) * extreme + chunkHeight / 2.;
        double selector = getNoiseValue.get(x * 8.555, 8) * 10;
        double n = (1 - selector * selector * selector * selector * selector) * low + selector * selector * selector * selector * selector * high;
        return switch (biomeId) {
//            case 0 -> perlinNoise.perlin(x / (Math.PI * 10), 0.5, 8, 2) * 2. + 64.;
            case 0 -> n;
            case 1 -> n;
            default -> Math.random() * this.chunkHeight;
        };
    }

    private double getExtreme(Double x, PerlinNoise perlinNoise) {
        double[] extremes = new double[50];
        for (int i = 0; i < extremes.length; ++i) {
            int biomeId = getBiomeId((long) i - extremes.length / 2 + Math.round(x), perlinNoise);
            extremes[i] = switch (biomeId) {
                case 0 -> 100;
                case 1 -> 500;
                default -> 0;
            };
        }
        return Arrays.stream(extremes).average().orElse(0);
    }

    public boolean isInChunkBounds(int x, int y) {
        return posToIndex(x, y) >= 0 && posToIndex(x, y) < blocks.length;
    }

    public int getBlock(int x, int y) {
        if (isInChunkBounds(x, y)) {
            hasBeenAccessed = true;
            return blocks[posToIndex(x, y)];
        } else return 0;
    }

    public void setBlock(int x, int y, int blockType) {
        if (generating) return;
        setBlockForGenerator(x, y, blockType);
    }

    public void setBlockForGenerator(int x, int y, int blockType) {
        if (blockType < 0) return;
        if (isInChunkBounds(x, y)) {
            hasBeenAccessed = true;
            blocks[posToIndex(x, y)] = blockType;
        }
    }
}
