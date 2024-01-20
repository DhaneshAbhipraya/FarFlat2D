package net.farflat;

import static java.lang.Math.*;

public class Mathematical {
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
}
