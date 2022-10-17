package com.gmail.ksw26141.eventListener;

import static com.gmail.ksw26141.config.SheetMusicConfig.InstrumentMutePlayers;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerFollowing;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheet;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheetIndex;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitHandler implements Listener {

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {//에러방지 메모리 절약 플레이어 나갈 시 데이터 제거
    var player = event.getPlayer();
    var uuid = player.getUniqueId();
    InstrumentMutePlayers.remove(uuid);
    PlayerSheet.remove(uuid);
    PlayerSheetIndex.remove(uuid);
    PlayerFollowing.remove(uuid);
  }

}
