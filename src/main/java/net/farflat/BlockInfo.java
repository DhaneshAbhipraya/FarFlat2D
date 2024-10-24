package net.farflat;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class BlockInfo {
    private static final Map<Integer, BlockInfoEntry> BLOCK_ENTRIES = new HashMap<>();
    private static final BlockInfoEntry DEFAULT_BLOCK_INFO_ENTRY = new BlockInfoEntry("unknown", true, false, new Color(0xFF0000));
    static {
        BLOCK_ENTRIES.put(0, new BlockInfoEntry("Air", false, false, new Color(0xFFFFFF)));
        BLOCK_ENTRIES.put(1, new BlockInfoEntry("Stone", true, false, new Color(0x7E7E7E)));
        BLOCK_ENTRIES.put(2, new BlockInfoEntry("Dirt", true, false, new Color(0x8D4A11)));
        BLOCK_ENTRIES.put(3, new BlockInfoEntry("Grass", true, false, new Color(0x149B10)));
        BLOCK_ENTRIES.put(4, new BlockInfoEntry("Bedrock", true, false, new Color(0x313131)));
        BLOCK_ENTRIES.put(5, new BlockInfoEntry("Water", false, true, new Color(0x1B52FA)));
    }

    public record BlockInfoEntry(String name, boolean solid, boolean liquid, Color color) {
    }

    public static BlockInfoEntry getBlockInfo(int id) {
        return BLOCK_ENTRIES.getOrDefault(id, DEFAULT_BLOCK_INFO_ENTRY);
    }
}
