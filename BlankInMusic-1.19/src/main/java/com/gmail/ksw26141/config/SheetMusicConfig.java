package com.gmail.ksw26141.config;

import java.util.ArrayList;
import java.util.HashMap;

public class SheetMusicConfig {

    public final static HashMap<String, Boolean> InstrumentMutePlayers = new HashMap<>();

    public final static HashMap<String, ArrayList<String>> PlayerSheet = new HashMap<>();

    public final static HashMap<String, Integer> PlayerSheetIndex = new HashMap<>();

    // Key 는 Value 를 지휘자로 본다.
    public final static HashMap<String, String> PlayerFollowing = new HashMap<>();

    public static void clearConfig(){
        InstrumentMutePlayers.clear();
        PlayerSheet.clear();
        PlayerSheetIndex.clear();
        PlayerFollowing.clear();
    }

}
