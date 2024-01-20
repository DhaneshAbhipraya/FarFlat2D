package net.farflat;

import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.max;

public class World {
    private final int chunkWidth = 32;
    private final int chunkHeight = 512;
    private long noiseSeed;
    Map<BigInteger, Chunk> loadedChunks;
    private long biomeSeed;

    public World(long noiseSeed) {
        this.loadedChunks = new TreeMap<>();
        this.noiseSeed = noiseSeed;
    }

    public void resetAccessedCheck() {
        for (Chunk chunk : loadedChunks.values()) {
            chunk.resetAccessedCheck();
        }
    }

    public World() {
        this(System.currentTimeMillis());
    }

    public int getHeight() {
        return chunkHeight;
    }

    public Chunk loadChunk(BigInteger chunkPos) {
        if (!loadedChunks.containsKey(chunkPos)) {
            Chunk newChunk = new Chunk(chunkWidth, chunkHeight, biomeSeed);
//            newChunk.generate(chunkPos * chunkWidth, noise);
            new Chunk.TerrainGenerator(newChunk, chunkPos.multiply(BigInteger.valueOf(chunkWidth)).intValue(), noiseSeed).start();
            loadedChunks.put(chunkPos, newChunk);
            return newChunk;
        }
        return loadedChunks.get(chunkPos);
    }

    public void unloadChunk(BigInteger chunkPos) {
        loadedChunks.remove(chunkPos);
    }

    public Chunk loadNecessaryChunk(BigInteger blockPos) {
        BigInteger tryLoadChunkPos = blockPos.divide(BigInteger.valueOf(chunkWidth));
        return loadChunk(tryLoadChunkPos);
    }

    public int getBlock(BigInteger blockX, long blockY) {
        return loadNecessaryChunk(blockX).getBlock(blockX.mod(BigInteger.valueOf(chunkWidth)).intValue(), (int) (chunkHeight - blockY));
    }

    public void setBlock(BigInteger blockX, int blockY, int blockType) {
        loadNecessaryChunk(blockX).setBlock(blockX.mod(BigInteger.valueOf(chunkWidth)).intValue(), blockY, blockType);
    }

    public BlockPos[] getAllPositionsInAABB(AABB aabb) {
        BigInteger startX = aabb.x1().toBigInteger();
        BigInteger endX = aabb.x2().toBigInteger();
        long startY = (long) aabb.y1();
        long endY = (long) aabb.y2();
        List<BlockPos> positions = new ArrayList<>();
        for (BigInteger x = startX; x.compareTo(endX) <= 0; x=x.add(BigInteger.ONE)) {
            for (long y = startY; y <= endY; y++) {
                positions.add(new BlockPos(x, y));
            }
        }
        return positions.toArray(new BlockPos[0]);
    }

    public void reloadAllChunks() {
        loadedChunks.clear();
    }

    public int getChunkWidth() {
        return chunkWidth;
    }
}
