package com.gmail.ksw26141.eventListener;

import com.gmail.ksw26141.model.InstrumentPitch;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.gmail.ksw26141.util.InstrumentUtil.playInstrumentItem;

public class PlayerInstrumentInteractHandler implements Listener {

    private final FileConfiguration config;

    private boolean playCheck = true;


    public PlayerInstrumentInteractHandler(FileConfiguration config) {
        this.config = config;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {//악기 들고 우클릭시 처리
        var user = event.getPlayer();
        var action = event.getAction();
        var instrumentPitch = new InstrumentPitch(0, (int) ((user.getLocation().getPitch() * -1 + 90) / 12.857142857142857142857142857143));

        if (event.getPlayer().isSneaking()) {
            instrumentPitch.setSemitone(1);
        }

        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                playCheck = !playCheck;
                if (playCheck) {
                    return;
                }
            }
            var handItem = user.getInventory().getItemInMainHand();
            if (playInstrumentItem(user, instrumentPitch, handItem, config)) {
                event.setCancelled(true);
            }
        } else if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            var handItem = user.getInventory().getItemInOffHand();
            if (playInstrumentItem(user, instrumentPitch, handItem, config)) {
                event.setCancelled(true);
            }
        }
    }

}
