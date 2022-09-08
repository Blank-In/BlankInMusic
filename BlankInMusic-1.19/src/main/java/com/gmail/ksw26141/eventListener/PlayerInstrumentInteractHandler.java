package com.gmail.ksw26141.eventListener;

import com.gmail.ksw26141.util.InstrumentUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static org.bukkit.event.block.Action.*;

public class PlayerInstrumentInteractHandler implements Listener {

    private final FileConfiguration config;

    private boolean playCheck = true;


    public PlayerInstrumentInteractHandler(FileConfiguration config) {
        this.config = config;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {//악기 들고 우클릭시 처리
        var player = event.getPlayer();
        var action = event.getAction();

        if (action.equals(RIGHT_CLICK_AIR) || action.equals(RIGHT_CLICK_BLOCK)) {
            if (action.equals(RIGHT_CLICK_BLOCK)) {
                playCheck = !playCheck;
                if (playCheck) {
                    return;
                }
            }
            var handItem = player.getInventory().getItemInMainHand();
            if (InstrumentUtil.playInstrumentItem(player, handItem, config)) {
                event.setCancelled(true);
            }
        } else if (action.equals(LEFT_CLICK_AIR) || action.equals(LEFT_CLICK_BLOCK)) {
            var handItem = player.getInventory().getItemInOffHand();
            if (InstrumentUtil.playInstrumentItem(player, handItem, config)) {
                event.setCancelled(true);
            }
        }
    }

}
