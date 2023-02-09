package com.gmail.ksw26141.util;

import static com.gmail.ksw26141.Constants.GREEN_PREFIX;
import static com.gmail.ksw26141.Constants.RED_PREFIX;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheet;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheetIndex;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import static org.bukkit.ChatColor.RED;

import com.gmail.ksw26141.BlankInMusic;
import com.gmail.ksw26141.model.InstrumentPitch;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitScheduler;

public class SheetMusicUtil {

  private static final BukkitScheduler scheduler = getServer().getScheduler();

  public static boolean sheetEncode(Player player, @Nullable ItemStack item, List<String> encodedPage) {
    if (item != null && (Material.WRITABLE_BOOK.equals(item.getType()) || Material.WRITTEN_BOOK.equals(item.getType()))) {
      var sheet = (BookMeta) item.getItemMeta();
      var book = sheet.getPages();
      var playerUUID = player.getUniqueId();
      for (var page : book) {
        var line = page.replace("§0", "")
            .replace("\n\n", " ")
            .replace("\n", " ")
            .replace("  ", " ")
            .replace(" \n", " ")
            .replace("\n ", " ")
            .split(" ");
        for (var word : line) {
          if (word.contains("//")) { // 주석
            break;
          }
          encodedPage.add(word);
        }
      }
      PlayerSheet.put(playerUUID, encodedPage);
      PlayerSheetIndex.put(playerUUID, 0);
      player.sendRawMessage(GREEN_PREFIX + "악보가 등록되었습니다.");
      return true;
    } else {
      player.sendMessage(RED_PREFIX + "악보를 손에 들어주세요.");
      return false;
    }
  }

  public static void playSheet(Player player, FileConfiguration config, BlankInMusic plugin) {//악보 연주 재귀 함수
    var playerUUID = player.getUniqueId();
    var sheet = PlayerSheet.get(playerUUID);
    var index = PlayerSheetIndex.get(playerUUID);

    try {
      var syllable = sheet.get(index);
      var tick = index > 1 ? Integer.parseInt(sheet.get(index - 1)) : 0;
      scheduler.scheduleSyncDelayedTask(plugin, () -> {
        try {
          var pitchList = new ArrayList<InstrumentPitch>();
          var translatePitch = new InstrumentPitch();

          for (var syllableIndex = 0; syllableIndex < syllable.length(); ++syllableIndex) {
            char input = syllable.charAt(syllableIndex);

            switch (input) {
              case '+' -> { // 동시에 연주할 음 추가
                pitchList.add(translatePitch);
                translatePitch = new InstrumentPitch();
              }
              case '-' -> translatePitch.setPitchLevel(translatePitch.getPitchLevel() - 20); // 쉼
              case '#' -> translatePitch.setSemitone(1); // 샤프 처리
              case 'b' -> translatePitch.setSemitone(2); // 플랫 처리
              case 'F' -> { // F버튼 (양손 교체)
                PlayerInventory inventory = player.getInventory();
                ItemStack mainHand = inventory.getItemInMainHand();
                inventory.setItemInMainHand(inventory.getItemInOffHand());
                inventory.setItemInOffHand(mainHand);
              }
              case 'C' -> { // 슬롯 Change
                ++syllableIndex;
                var slotNumber = syllable.charAt(syllableIndex) - '1';
                player.getInventory().setHeldItemSlot(slotNumber);
              }
              case 'L' -> { // 악보 Link
                ++syllableIndex;
                var slotNumber = syllable.charAt(syllableIndex) - '1';
                var nextSheet = player.getInventory().getItem(slotNumber);
                if (sheetEncode(player, nextSheet, new ArrayList<>())) {
                  playSheet(player, config, plugin);
                  return; // 현재 음절을 연주하게 되면 PlayerSheetIndex 가 꼬이게 됨.
                }
              }
              default -> { //숫자 처리
                translatePitch.setPitchLevel(translatePitch.getPitchLevel() * 10);//10의 자리 처리
                translatePitch.setPitchLevel(translatePitch.getPitchLevel() + Integer.parseInt(input + ""));
              }
            }
          }
          pitchList.add(translatePitch);

          // 번역된 음절 연주
          var isPlaySucceed = true;
          for (InstrumentPitch playingPitch : pitchList) {
            if (playingPitch.getPitchLevel() < 0) {
              player.sendRawMessage(RED + "쉼표");
            } else if (isPlaySucceed) {
              isPlaySucceed = InstrumentUtil.playInstrumentItem(player, playingPitch,
                  player.getInventory().getItemInMainHand(), config);
            }
          }

          // 연주 결과 분기
          if (isPlaySucceed) {
            if (index + 2 < sheet.size()) {
              PlayerSheetIndex.put(playerUUID, index + 2);
              playSheet(player, config, plugin);
            } else {
              PlayerSheetIndex.put(playerUUID, 0);
              player.sendRawMessage(GREEN_PREFIX + "악보 연주가 종료되었습니다.");
            }
          } else {
            PlayerSheetIndex.put(playerUUID, 0);
            player.sendRawMessage(RED_PREFIX + "악기를 손에 들어주세요.");
          }

        } catch (Exception e) {
          PlayerSheetIndex.put(playerUUID, 0);
          player.sendRawMessage(RED_PREFIX + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
          getLogger().warning(player.getName() + "님의 악보에서 오류가 발생했습니다. " + e);
        }
      }, tick);
    } catch (Exception e) {
      PlayerSheetIndex.put(playerUUID, 0);
      player.sendRawMessage(RED_PREFIX + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
      getLogger().warning(player.getName() + "님의 악보에서 오류가 발생했습니다. " + e);
    }
  }

}
