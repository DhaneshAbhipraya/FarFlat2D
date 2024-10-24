package net.farflat;

import java.util.Random;

import static net.farflat.Mathematical.lerp3;

public class Noise {
    private final double x;
    private final double y;
    private final byte[] permuts;

    public Noise(Random rand) {
        this.x = rand.nextDouble() * 256.;
        this.y = rand.nextDouble() * 256.;
        this.permuts = new byte[256];

        for (int i = 0; i < 256; ++i) {
            this.permuts[i] = (byte)i;
        }

        for (int k = 0; k < 256; ++k) {
            int j = rand.nextInt(256 - k);
            byte b0 = this.permuts[j];
            this.permuts[k] = this.permuts[k+j];
            this.permuts[k+j] = b0;
        }
    }

    public double getValue(double x, double y) {
        double d0 = x + this.x;
        double d1 = y + this.y;
        int i = (int) Math.floor(d0);
        int j = (int) Math.floor(d1);
        double d2 = d0 - (double)i;
        double d3 = d1 - (double)j;
        double d4 = PerlinNoise.fade(d2);
        double d5 = PerlinNoise.fade(d3);

        return this.get(i, 0, j, d2, 0, d3, d4, 0, d5);
    }

    public double get(int paramI, int paramJ, int paramK, double paramD3, double paramD4D9, double paramD5, double paramD6, double paramD7, double paramD8) {
        int i = this.getPermutValue(paramI) + paramJ;
        int j = this.getPermutValue(i) + paramK;
        int k = this.getPermutValue(i + 1) + paramK;
        int l = this.getPermutValue(paramI + 1) + paramJ;
        int i1 = this.getPermutValue(l) + paramK;
        int j1 = this.getPermutValue(l + 1) + paramK;
        double d0 = dotGrad(this.getPermutValue(j), paramD3, paramD4D9, paramD5);
        double d1 = dotGrad(this.getPermutValue(i1), paramD3 - 1.0D, paramD4D9, paramD5);
        double d2 = dotGrad(this.getPermutValue(k), paramD3, paramD4D9 - 1.0D, paramD5);
        double d3 = dotGrad(this.getPermutValue(j1), paramD3 - 1.0D, paramD4D9 - 1.0D, paramD5);
        double d4 = dotGrad(this.getPermutValue(j + 1), paramD3, paramD4D9, paramD5 - 1.0D);
        double d5 = dotGrad(this.getPermutValue(i1 + 1), paramD3 - 1.0D, paramD4D9, paramD5 - 1.0D);
        double d6 = dotGrad(this.getPermutValue(k + 1), paramD3, paramD4D9 - 1.0D, paramD5 - 1.0D);
        double d7 = dotGrad(this.getPermutValue(j1 + 1), paramD3 - 1.0D, paramD4D9 - 1.0D, paramD5 - 1.0D);
        return lerp3(paramD6, paramD7, paramD8, d0, d1, d2, d3, d4, d5, d6, d7);
    }

    private static double dotGrad(int gradIndex, double xFactor, double yFactor, double zFactor) {
        int i = gradIndex & 15;
        return SimplexNoise.processGrad(SimplexNoise.grad3[i], xFactor, yFactor, zFactor);
    }

    private int getPermutValue(int permutIndex) {
        return this.permuts[permutIndex & 255] & 255;
    }
}
