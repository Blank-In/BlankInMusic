package com.gmail.ksw26141.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class SheetMusicConfig {

    public final static HashSet<UUID> InstrumentMutePlayers = new HashSet<>();

    public final static HashMap<UUID, List<String>> PlayerSheet = new HashMap<>();

    public final static HashMap<UUID, Integer> PlayerSheetIndex = new HashMap<>();

    // Key 는 Value 를 지휘자로 본다.
    public final static HashMap<UUID, String> PlayerFollowing = new HashMap<>();

    public static void clearConfig() {
        InstrumentMutePlayers.clear();
        PlayerSheet.clear();
        PlayerSheetIndex.clear();
        PlayerFollowing.clear();
    }

}
