package net.farflat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public record AABB(BigDecimal x1, double y1, BigDecimal x2, double y2) {
    public AABB(double x1, double y1, double x2, double y2) {
        this(BigDecimal.valueOf(x1), y1, BigDecimal.valueOf(x2), y2);
    }

    public boolean isCollidingAxis(Axis axis, double axisPos) {
        return switch (axis) {
            case X -> axisPos > this.x1().doubleValue() && axisPos < this.x2().doubleValue();
            case Y -> axisPos > this.y1() && axisPos < this.y2();
        };
    }

    public boolean isCollidingAABB(AABB other) {
        return !(this.x1().compareTo(other.x2()) >= 0 || this.x2().compareTo(other.x1()) <= 0 || this.y1() >= other.y2() || this.y2() <= other.y1());
    }

    public boolean isCollidingPoint(Point2 point) {
        return this.x1().doubleValue() < point.x && this.x2().doubleValue() > point.x && this.y1() < point.y && this.y2() > point.y;
    }

    public AABB scale(double scale) {
        return new AABB(this.x1.multiply(BigDecimal.valueOf(scale)), this.y1*scale, this.x2.multiply(BigDecimal.valueOf(scale)), this.y2*scale);
    }

    public AABB roundInclusive() {
        return new AABB(this.x1.round(new MathContext(34, RoundingMode.FLOOR)), Math.floor(this.y1), this.x2.round(new MathContext(34, RoundingMode.CEILING)), Math.ceil(this.y2));
    }

    public AABB inflate(double x, double y) {
        return new AABB(this.x1.subtract(BigDecimal.valueOf(x)), this.y1-y, this.x2.add(BigDecimal.valueOf(x)), this.y2+y);
    }

    public AABB inflate(double s) {
        return inflate(s, s);
    }

    public AABB deflate(double x, double y) {
        return inflate(-x, -y);
    }

    public AABB deflate(double s) {
        return deflate(s, s);
    }

    public Point2.BDX getCenter() {
        return new Point2.BDX(this.x1.add(this.x2).divide(BigDecimal.valueOf(2)), (this.y1 + this.y2)/2);
    }

    public Point2.BDX getWidthHeight() {
        return new Point2.BDX(x2.subtract(x1), y2 - y1);
    }

    public AABB factor(double f) {
        Point2.BDX center = getCenter();
        Point2.BDX widthHeight = getWidthHeight();
        Point2.BDX wh = widthHeight.mult(f);
        return new AABB(center.x.subtract(wh.x.divide(BigDecimal.valueOf(2))), center.y - wh.y / 2,
                center.x.add(wh.x.divide(BigDecimal.valueOf(2))), center.y + wh.y / 2);
    }

    public AABB offset(Point2 pos) {
        return new AABB(x1.add(BigDecimal.valueOf(pos.x)), y2+pos.y, x2.add(BigDecimal.valueOf(pos.x)), y2+pos.y);
    }

    public AABB add(double x1, double y1, double x2, double y2) {
        return new AABB(this.x1.add(BigDecimal.valueOf(x1)), this.y1 + y1, this.x2.add(BigDecimal.valueOf(x2)), this.y2 + y2);
    }
}
