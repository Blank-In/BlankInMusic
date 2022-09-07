package com.gmail.ksw26141.eventListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.gmail.ksw26141.config.SheetMusicConfig.*;

public class PlayerQuitHandler implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {//에러방지 메모리 절약 플레이어 나갈 시 데이터 제거
        InstrumentMutePlayers.remove(event.getPlayer().getName());
        PlayerSheet.remove(event.getPlayer().getName());
        PlayerSheetIndex.remove(event.getPlayer().getName());
        PlayerFollowing.remove(event.getPlayer().getName());
    }

}
