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

public class InstrumentUtil {

    public static boolean playInstrumentItem(Player player, ItemStack handItem, FileConfiguration config) {
        var instrumentPitch = new InstrumentPitch(player.isSneaking() ? 1 : 0, (int) ((player.getLocation().getPitch() * -1 + 90) / 12.857142857142857142857142857143));
        return playInstrumentItem(player, instrumentPitch, handItem, config);
    }

    public static boolean playInstrumentItem(Player player, InstrumentPitch instrumentPitch, ItemStack handItem, FileConfiguration config) {
        String musicTag;
        try {
            var itemLore = handItem.getItemMeta().getLore();
            if (itemLore != null && !itemLore.isEmpty()) {
                musicTag = itemLore.get(0).substring(2);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        var instrumentSound = MusicTagUtil.musicTagToItemSound(musicTag, instrumentPitch, config);
        if (StringUtils.isAnyBlank(musicTag, instrumentSound.getItemSound())) {
            return false;
        }

        return playInstrumentSound(player, instrumentSound, handItem);
    }

    /**
     *
     */
    public static boolean playInstrumentSound(Player player, InstrumentSound instrumentSound, ItemStack handItem) {//받아온 정보로 악기 소리를 재생함
        if (InstrumentMutePlayers.contains(player)) {
            player.sendRawMessage(RED_PREFIX + "현재 연주 차단 상태입니다.");
            return false;
        }

        var itemMeta = handItem.getItemMeta();
        var itemSound = instrumentSound.getItemSound();
        var musicTag = instrumentSound.getMusicTag();
        var instrumentPitch = instrumentSound.getInstrumentPitch();

        var location = player.getLocation().add(0, 1, 0);
        var vector = location.getDirection().multiply(1.5);

        var itemDisplayName = ITEM_NAME_FIRST + musicTag + ChatColor.RED + ' ';
        if ("드럼".equals(musicTag)) { // 드럼은 모든 음이 다른 소리를 내도록 되어 있어 예외 처리
            itemDisplayName += itemSound;
        } else {
            itemDisplayName += instrumentPitch.getPitchLevel() + " " + ChatColor.BLUE + instrumentPitch.getPitchName();
        }

        itemMeta.setDisplayName(itemDisplayName);
        handItem.setItemMeta(itemMeta);
        player.getWorld().playSound(location.add(vector), itemSound, 2, instrumentPitch.getMinecraftPitch());

        for (var mutedPlayer : InstrumentMutePlayers) {
            if (mutedPlayer != null) {
                mutedPlayer.stopSound(itemSound);
            }
        }
        return true;
    }

}
