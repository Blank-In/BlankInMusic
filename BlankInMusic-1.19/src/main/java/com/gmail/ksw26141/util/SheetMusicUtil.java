package com.gmail.ksw26141.util;

import com.gmail.ksw26141.BlankInMusic;
import com.gmail.ksw26141.model.InstrumentPitch;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;

import static com.gmail.ksw26141.Constants.GREEN_PREFIX;
import static com.gmail.ksw26141.Constants.RED_PREFIX;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheet;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheetIndex;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class SheetMusicUtil {

    private static final BukkitScheduler scheduler = getServer().getScheduler();

    public static void sheetEncode(CommandSender sender, ItemStack item, ArrayList<String> encodedPage) {
        var itemType = item.getType();
        if (Material.WRITABLE_BOOK.equals(itemType) || Material.WRITTEN_BOOK.equals(itemType)) {
            var sheet = (BookMeta) item.getItemMeta();
            var book = sheet.getPages();
            for (var page : book) {
                var line = page.replace("§0", "").replace("\n", " ").replace("  ", " ").replace("\n\n", "\n").replace(" \n", " ").replace("\n ", " ").split(" ");
                for (var word : line) {
                    if (word.contains("//")) {//주석처리
                        break;
                    }
                    encodedPage.add(word);
                }
            }
            PlayerSheet.put(sender.getName(), encodedPage);
            PlayerSheetIndex.put(sender.getName(), 0);
            sender.sendMessage(GREEN_PREFIX + "악보가 등록되었습니다.");
        } else {
            sender.sendMessage(RED_PREFIX + "악보를 손에 들어주세요.");
        }
    }

    public static void playSheet(Player user, FileConfiguration config, BlankInMusic plugin) {//악보 연주 재귀 함수
        try {
            var sheet = PlayerSheet.get(user.getName());
            var index = PlayerSheetIndex.get(user.getName());
            var tick = 0;
            var syllable = sheet.get(index);
            if (index > 1) {
                tick = Integer.parseInt(sheet.get(index - 1));
            }

            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                try {
                    var pitchList = new ArrayList<InstrumentPitch>();
                    var translatePitch = new InstrumentPitch();

                    // 악보 한 음절 번역 <이곳을 수정하여 기능 수정>
                    for (var syllableIndex = 0; syllableIndex < syllable.length(); ++syllableIndex) {
                        char input = syllable.charAt(syllableIndex);
                        switch (input) {
                            case '+' -> { //동시에 연주할 음 추가
                                pitchList.add(translatePitch);
                                translatePitch = new InstrumentPitch();
                            }
                            case '#' -> translatePitch.setSemitone(1); //반음 처리
                            case '-' -> translatePitch.setPitchLevel(translatePitch.getPitchLevel() - 20);
                            case 'F' -> {
                                PlayerInventory inventory = user.getInventory();
                                ItemStack mainHand = inventory.getItemInMainHand();
                                inventory.setItemInMainHand(inventory.getItemInOffHand());
                                inventory.setItemInOffHand(mainHand);
                            }
                            case 'C' -> {
                                ++syllableIndex;
                                int slotNumber = syllable.charAt(syllableIndex) - '1';
                                user.getInventory().setHeldItemSlot(slotNumber);
                            }
                            default -> { //숫자 처리
                                translatePitch.setPitchLevel(translatePitch.getPitchLevel() * 10);//10의 자리 처리
                                translatePitch.setPitchLevel(translatePitch.getPitchLevel() + Integer.parseInt(input + ""));
                            }
                        }
                    }
                    pitchList.add(translatePitch);

                    // 번역된 음절 연주
                    var playState = true;
                    for (InstrumentPitch playingPitch : pitchList) {
                        if (playingPitch.getPitchLevel() < 0) {
                            user.sendRawMessage(ChatColor.RED + "쉼표");
                        } else {
                            playState = InstrumentUtil.playInstrumentItem(user, playingPitch, user.getInventory().getItemInMainHand(), config);
                        }
                    }

                    // 연주 결과 분기
                    if (playState) {
                        if (index + 2 < sheet.size()) {
                            PlayerSheetIndex.put(user.getName(), index + 2);
                            playSheet(user, config, plugin);
                        } else {
                            PlayerSheetIndex.put(user.getName(), 0);
                            user.sendRawMessage(GREEN_PREFIX + "악보 연주가 종료되었습니다.");
                        }
                    } else {
                        PlayerSheetIndex.put(user.getName(), 0);
                        user.sendRawMessage(RED_PREFIX + "악기를 손에 들어주세요.");
                    }

                } catch (Exception e) {
                    PlayerSheetIndex.put(user.getName(), 0);
                    user.sendRawMessage(RED_PREFIX + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
                    getLogger().warning(user.getName() + "님의 악보에서 오류가 발생했습니다. " + e);
                }
            }, tick);
        } catch (Exception e) {
            PlayerSheetIndex.put(user.getName(), 0);
            user.sendRawMessage(RED_PREFIX + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
            getLogger().warning(user.getName() + "님의 악보에서 오류가 발생했습니다. " + e);
        }
    }

}
