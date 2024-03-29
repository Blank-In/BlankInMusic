package blankin.music.util;

import static blankin.music.Constants.ITEM_NAME_FIRST;
import static blankin.music.Constants.RED_PREFIX;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.RED;

import blankin.music.config.SheetMusicConfig;
import blankin.music.model.InstrumentPitch;
import blankin.music.model.InstrumentSound;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InstrumentUtil {

  public static boolean playInstrumentItem(Player player, ItemStack handItem, FileConfiguration config) {
    var semitone = player.isSneaking() ? 1 : 0;
    var pitchLevel = (int) ((player.getLocation().getPitch() * -1 + 90) / 12.857142857142857142857142857143);
    var instrumentPitch = new InstrumentPitch(semitone, pitchLevel);
    return playInstrumentItem(player, instrumentPitch, handItem, config);
  }

  public static boolean playInstrumentItem(Player player, InstrumentPitch instrumentPitch, ItemStack handItem, FileConfiguration config) {
    var musicTag = "";

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

  private static boolean playInstrumentSound(Player player, InstrumentSound instrumentSound, ItemStack handItem) {
    if (SheetMusicConfig.InstrumentMutePlayers.contains(player.getUniqueId())) {
      player.sendRawMessage(RED_PREFIX + "현재 연주 차단 상태입니다.");
      return false;
    }

    var itemMeta = handItem.getItemMeta();
    var itemSound = instrumentSound.getItemSound();
    var musicTag = instrumentSound.getMusicTag();
    var instrumentPitch = instrumentSound.getInstrumentPitch();

    var location = player.getLocation().add(0, 1, 0);
    var vector = location.getDirection().multiply(1.5);

    var itemDisplayName = ITEM_NAME_FIRST + musicTag + RED + ' ';
    if ("드럼".equals(musicTag)) { // 드럼은 모든 음이 다른 소리를 내도록 되어 있어 예외 처리
      itemDisplayName += itemSound;
    } else {
      itemDisplayName += instrumentPitch.getPitchLevel() + " " + BLUE + instrumentPitch.getPitchName();
    }

    itemMeta.setDisplayName(itemDisplayName);
    handItem.setItemMeta(itemMeta);
    player.getWorld().playSound(location.add(vector), itemSound, SoundCategory.RECORDS, 2, instrumentPitch.getMinecraftPitch());

    for (var mutedPlayerUUID : SheetMusicConfig.InstrumentMutePlayers) {
      // TODO: 대형 서버에서의 심각한 성능저하가 우려된다.
      //  플레이어가 소리 설정에서 직접 조절 할 수 있도록 연주에 사운드 카테고리 추가 후 기능 제거가 베스트 일 것 같다.
      var mutedPlayer = player.getServer().getPlayer(mutedPlayerUUID);
      if (mutedPlayer != null) {
        mutedPlayer.stopSound(itemSound);
      }
    }
    return true;
  }

}
