package com.gmail.ksw26141.eventListener;

import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

import com.gmail.ksw26141.util.InstrumentUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInstrumentInteractHandler implements Listener {

  private final FileConfiguration config;

  private boolean playCheck = true;


  public PlayerInstrumentInteractHandler(FileConfiguration config) {
    this.config = config;
  }


  @EventHandler
  private void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();
    var action = event.getAction();

    if (RIGHT_CLICK_BLOCK.equals(action)) {
      playCheck = !playCheck;
      if (playCheck) {
        return;
      }
    }

    if (instrumentAction(player, action)) {
      event.setCancelled(true);
    }
  }

  private boolean instrumentAction(Player player, Action action) {
    var handItem = new ItemStack(Material.AIR);

    if (RIGHT_CLICK_AIR.equals(action) || RIGHT_CLICK_BLOCK.equals(action)) {
      handItem = player.getInventory().getItemInMainHand();
    } else if (LEFT_CLICK_AIR.equals(action) || LEFT_CLICK_BLOCK.equals(action)) {
      handItem = player.getInventory().getItemInOffHand();
    } else {
      return false;
    }

    return InstrumentUtil.playInstrumentItem(player, handItem, config);
  }

}
