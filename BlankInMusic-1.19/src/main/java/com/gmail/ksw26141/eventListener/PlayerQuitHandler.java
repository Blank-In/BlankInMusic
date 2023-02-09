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
  private void onPlayerQuit(PlayerQuitEvent event) { //메모리 절약
    var playerUUID = event.getPlayer().getUniqueId();
    InstrumentMutePlayers.remove(playerUUID);
    PlayerSheet.remove(playerUUID);
    PlayerSheetIndex.remove(playerUUID);
    PlayerFollowing.remove(playerUUID);
  }

}
