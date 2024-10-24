package net.farflat;

import static java.lang.Math.*;

public final class Mathematical {
    public static final long MAX_NORMAL_WORLD_POS = 411395285992L;
    public static final long MAX_SAFE_WORLD_POS = 1000000000L;

    public static double lerp(double a, double b, double t) {
        return (1-t)*a + t*b;
    }

    public static double scaleC(double x, double c, double s) {
        return c+s*(x-c);
    }
    public static int scaleC(int x, int c, int s) {
        return c+s*(x-c);
    }

    public static double squishOutside(double x, double r) {
        if (abs(x) < r)
            return x;
        return signum(x)*(abs(x)-r)/max(0,abs(x)/r)+signum(x)*r;
    }

    public static double squishOutsideZC(double x, double r) {
        return squishOutside(2*x-r, r)/2+r/2;
    }

    public static double lerp1(double pct, double start, double end) {
        return start + pct * (end - start);
    }

    public static double lerp3(double p_219807_0_, double p_219807_2_, double p_219807_4_, double p_219807_6_, double p_219807_8_, double p_219807_10_, double p_219807_12_, double p_219807_14_, double p_219807_16_, double p_219807_18_, double p_219807_20_) {
        return lerp1(p_219807_4_, lerp2(p_219807_0_, p_219807_2_, p_219807_6_, p_219807_8_, p_219807_10_, p_219807_12_), lerp2(p_219807_0_, p_219807_2_, p_219807_14_, p_219807_16_, p_219807_18_, p_219807_20_));
    }

    public static double lerp2(double p_219804_0_, double p_219804_2_, double p_219804_4_, double p_219804_6_, double p_219804_8_, double p_219804_10_) {
        return lerp1(p_219804_2_, lerp1(p_219804_0_, p_219804_4_, p_219804_6_), lerp1(p_219804_0_, p_219804_8_, p_219804_10_));
    }
}
