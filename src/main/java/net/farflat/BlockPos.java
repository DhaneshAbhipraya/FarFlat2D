package net.farflat;

import java.util.Objects;

public record BlockPos(java.math.BigInteger x, long y) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return y == blockPos.y && x.compareTo(blockPos.x) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
