package com.gmail.ksw26141.util;

import com.gmail.ksw26141.model.InstrumentPitch;
import com.gmail.ksw26141.model.InstrumentSound;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.gmail.ksw26141.Constants.ITEM_NAME_FIRST;
import static com.gmail.ksw26141.Constants.RED_PREFIX;
import static com.gmail.ksw26141.config.SheetMusicConfig.InstrumentMutePlayers;
import static com.gmail.ksw26141.util.MusicTagUtil.musicTagToItemSound;
import static org.bukkit.Bukkit.getServer;

public class InstrumentUtil {

    public static boolean playInstrumentItem(Player user, InstrumentPitch instrumentPitch, ItemStack handItem, FileConfiguration config) {
        if (handItem == null) {
            return false;
        }

        var tag = "";
        try {
            var itemLore = handItem.getItemMeta().getLore();
            if (itemLore != null && !itemLore.isEmpty()) {
                tag = itemLore.get(0).substring(2);
            }
        } catch (Exception e) {
            return false;
        }

        var instrumentSound = musicTagToItemSound(tag, instrumentPitch, config);
        if (StringUtils.isEmpty(instrumentSound.getItemSound())) {
            return false;
        }

        return playInstrumentSound(user, instrumentSound, handItem);
    }

    public static boolean playInstrumentSound(Player user, InstrumentSound instrumentSound, ItemStack handItem) {//받아온 정보로 악기 소리를 재생함
        if (InstrumentMutePlayers.containsKey(user.getName())) {
            user.sendRawMessage(RED_PREFIX + "현재 연주 차단 상태입니다.");
            return false;
        }

        var itemMeta = handItem.getItemMeta();
        var itemSound = instrumentSound.getItemSound();
        var instrumentPitch = instrumentSound.getInstrumentPitch();

        var location = user.getLocation().add(0, 1, 0);
        var vector = location.getDirection().multiply(1.5);

        itemMeta.setDisplayName(ITEM_NAME_FIRST + instrumentSound.getMusicTag() + ' ' +
                ChatColor.RED + instrumentPitch.getPitchLevel() +
                ChatColor.BLUE + ' ' + instrumentPitch.getPitchName());
        handItem.setItemMeta(itemMeta);
        user.getWorld().playSound(location.add(vector), itemSound, 2, instrumentPitch.getMinecraftPitch());

        for (var playerName : InstrumentMutePlayers.keySet()) {
            var player = getServer().getPlayer(playerName);
            if (player != null) {
                player.stopSound(itemSound);
            }
        }
        return true;
    }

}
