package net.farflat;

import java.util.Random;

public class PerlinNoise {

    private static final int SIZE = 256;
    private static final int MASK = SIZE - 1;

    private final int[] perm;
    private double[] gradients;

    public PerlinNoise() {
        this(new Random());
    }

    public PerlinNoise(Random rand) {
        perm = new int[SIZE * 2];
        gradients = new double[SIZE * 3];

        // Initialize permutation and gradients
        for (int i = 0; i < SIZE; i++) {
            perm[i] = i;
            gradients[i * 3] = 2 * rand.nextDouble() - 1;
            gradients[i * 3 + 1] = 2 * rand.nextDouble() - 1;
            gradients[i * 3 + 2] = 0;
        }

        // Shuffle permutation array
        for (int i = SIZE - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }

        // Duplicate permutation array to avoid index wrapping
        System.arraycopy(perm, 0, perm, SIZE, SIZE);
    }

    public static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double grad(int hash, double x) {
        int h = hash & 15;
        double grad = 1 + (h & 7); // Gradient value 1-8
        if ((h & 8) != 0) grad = -grad; // Randomly invert half of them
        return (grad * x);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    public double noise(double x) {
        int X = (int) Math.floor(x) & MASK;
        x -= Math.floor(x);
        double u = fade(x);
        return lerp(u, grad(perm[X], x), grad(perm[X + 1], x - 1)) * 2;
    }

    public double perlin(double x, double persistence, int octaves, double mult) {
//        x = (float) x;
        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            double octave = noise(x * frequency) * amplitude;
            total += octave;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= mult;
        }

        return total / maxValue;
    }

    public static void main(String[] args) {
        PerlinNoise perlin = new PerlinNoise();
        for (int i = 0; i < 10; i++) {
            double x = 5.5+i/10.;
            double persistence = 0.5;
            int octaves = 4;

            double perlinValue = perlin.perlin(x, persistence, octaves, 2);
            System.out.println("Perlin Noise value at x=" + x + ": " + perlinValue);
        }
    }
}
