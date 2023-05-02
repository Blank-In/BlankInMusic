package blankin.music.util;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import static org.bukkit.ChatColor.RED;

import blankin.music.BlankInMusic;
import blankin.music.Constants;
import blankin.music.config.SheetMusicConfig;
import blankin.music.model.InstrumentPitch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
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
    if (item == null || (!Material.WRITABLE_BOOK.equals(item.getType()) && !Material.WRITTEN_BOOK.equals(item.getType()))) {
      player.sendMessage(Constants.RED_PREFIX + "악보를 손에 들어주세요.");
      return false;
    }

    var sheet = (BookMeta) item.getItemMeta();
    var book = sheet.getPages();
    var playerUUID = player.getUniqueId();
    for (var page : book) {
      var line = page.replaceAll("§0", "").replaceAll("\n", " ").split(" ");
      for (var word : line) {
        if (StringUtils.isBlank(word)) {
          continue;
        } else if (word.contains("//")) { // 악보의 주석처리
          break;
        }
        encodedPage.add(word);
      }
    }
    SheetMusicConfig.PlayerSheet.put(playerUUID, encodedPage);
    SheetMusicConfig.PlayerSheetIndex.put(playerUUID, 0);
    getLogger().info(Constants.PLUGIN_TITLE + player.getName() + " 님이 악보를 등록했습니다. 페이지 수 " + book.size());
    player.sendRawMessage(Constants.GREEN_PREFIX + "악보가 등록되었습니다.");
    return true;
  }

  // TODO: 리팩토링 우선순위 높음
  public static void playSheet(Player player, FileConfiguration config, BlankInMusic plugin) { //악보 연주 재귀
    final var playerUUID = player.getUniqueId();
    final var sheet = SheetMusicConfig.PlayerSheet.get(playerUUID);
    final var index = SheetMusicConfig.PlayerSheetIndex.get(playerUUID);

    var syllable = sheet.get(index);
    var tick = new AtomicInteger(0);
    try {
      if (index > 1) {
        tick.set(Integer.parseInt(sheet.get(index - 1)));
      }
    } catch (Exception e) {
      sheetErrorHandle(player, e);
      return;
    }

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
              if (tick.get() > 0) {
                var slotNumber = syllable.charAt(syllableIndex) - '1';
                var nextSheet = player.getInventory().getItem(slotNumber);
                if (sheetEncode(player, nextSheet, new ArrayList<>())) {
                  playSheet(player, config, plugin);
                  return; // 현재 음절을 연주하게 되면 PlayerSheetIndex 가 꼬이게 됨.
                }
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
            isPlaySucceed = InstrumentUtil.playInstrumentItem(player, playingPitch, player.getInventory().getItemInMainHand(), config);
          }
        }

        // 연주 결과 분기
        if (isPlaySucceed) {
          if (index + 2 < sheet.size()) {
            SheetMusicConfig.PlayerSheetIndex.put(playerUUID, index + 2);
            playSheet(player, config, plugin);
          } else {
            SheetMusicConfig.PlayerSheetIndex.put(playerUUID, 0);
            player.sendRawMessage(Constants.GREEN_PREFIX + "악보 연주가 종료되었습니다.");
          }
        } else {
          SheetMusicConfig.PlayerSheetIndex.put(playerUUID, 0);
          player.sendRawMessage(Constants.RED_PREFIX + "악기를 손에 들어주세요.");
        }
      } catch (Exception e) {
        sheetErrorHandle(player, e);
      }
    }, tick.get());
  }

  private static void sheetErrorHandle(Player player, Exception e) {
    var playerUUID = player.getUniqueId();
    SheetMusicConfig.PlayerSheetIndex.put(playerUUID, 0);
    player.sendRawMessage(Constants.RED_PREFIX + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
    getLogger().warning(Constants.PLUGIN_TITLE + player.getName() + " 님의 악보에서 오류가 발생했습니다. " + e);
  }

}
