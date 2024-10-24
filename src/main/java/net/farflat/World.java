package net.farflat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class World {
    private final int chunkWidth = 16;
    private final int chunkHeight = 1024;
    private final long noiseSeed;
    Map<BigInteger, Chunk> loadedChunks;

    public World(long noiseSeed) {
        this.loadedChunks = new TreeMap<>();
        this.noiseSeed = noiseSeed;
    }

    public void resetAccessedCheck() {
        for (Chunk chunk : loadedChunks.values()) {
            chunk.resetAccessedCheck();
        }
    }

    public int getHeight() {
        return chunkHeight;
    }

    public Chunk loadChunk(BigInteger chunkPos) {
        if (!loadedChunks.containsKey(chunkPos)) {
            BigInteger firstBlockPos = chunkPos.multiply(BigInteger.valueOf(chunkWidth));
            Chunk newChunk = new Chunk(chunkWidth, chunkHeight, firstBlockPos);
//            newChunk.generate(chunkPos * chunkWidth, noise);
            Chunk.TerrainGenerator tg = new Chunk.TerrainGenerator(newChunk, noiseSeed);
            tg.start();
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

    public int getChunkWidth() {
        return chunkWidth;
    }
}
