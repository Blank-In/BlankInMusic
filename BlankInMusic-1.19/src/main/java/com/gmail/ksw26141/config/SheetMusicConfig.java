package com.gmail.ksw26141.config;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SheetMusicConfig {

    public final static HashSet<Player> InstrumentMutePlayers = new HashSet<>();

    // TODO: UUID 혹은 Player 로 바꾸는 것이 좋겠다.
    public final static HashMap<String, ArrayList<String>> PlayerSheet = new HashMap<>();

    // TODO: UUID 혹은 Player 로 바꾸는 것이 좋겠다.
    public final static HashMap<String, Integer> PlayerSheetIndex = new HashMap<>();

    // Key 는 Value 를 지휘자로 본다.
    // TODO: UUID 혹은 Player 로 바꾸는 것이 좋겠다.
    public final static HashMap<String, String> PlayerFollowing = new HashMap<>();

    public static void clearConfig(){
        InstrumentMutePlayers.clear();
        PlayerSheet.clear();
        PlayerSheetIndex.clear();
        PlayerFollowing.clear();
    }

}
