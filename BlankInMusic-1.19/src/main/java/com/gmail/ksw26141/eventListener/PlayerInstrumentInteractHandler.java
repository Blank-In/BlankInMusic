package com.gmail.ksw26141.eventListener;

import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

import com.gmail.ksw26141.util.InstrumentUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInstrumentInteractHandler implements Listener {

  private final FileConfiguration config;

  private boolean playCheck = true;


  public PlayerInstrumentInteractHandler(FileConfiguration config) {
    this.config = config;
  }


  @EventHandler
  private void onPlayerInteract(PlayerInteractEvent event) {//악기 들고 우클릭시 처리
    var player = event.getPlayer();
    var action = event.getAction();

    if (RIGHT_CLICK_BLOCK.equals(action)) {
      playCheck = !playCheck;
      if (playCheck) {
        return;
      }
    }

    if (RIGHT_CLICK_AIR.equals(action) || RIGHT_CLICK_BLOCK.equals(action)) {
      var handItem = player.getInventory().getItemInMainHand();
      if (InstrumentUtil.playInstrumentItem(player, handItem, config)) {
        event.setCancelled(true);
      }
    } else if (LEFT_CLICK_AIR.equals(action) || LEFT_CLICK_BLOCK.equals(action)) {
      var handItem = player.getInventory().getItemInOffHand();
      if (InstrumentUtil.playInstrumentItem(player, handItem, config)) {
        event.setCancelled(true);
      }
    }
  }

}
