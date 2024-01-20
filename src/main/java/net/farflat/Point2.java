package net.farflat;

import java.math.BigDecimal;

public class Point2 {
    public final double x;
    public final double y;

    public Point2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point2 mult(double f) {
        return new Point2(x * f, y * f);
    }

    public static class BDX {
        public final BigDecimal x;
        public final double y;

        public BDX(BigDecimal x, double y) {
            this.x = x;
            this.y = y;
        }

        public BDX mult(double f) {
            return new BDX(x.multiply(BigDecimal.valueOf(f)), y * f);
        }
    }
}
