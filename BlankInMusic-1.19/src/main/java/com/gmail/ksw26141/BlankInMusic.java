package com.gmail.ksw26141;

import com.gmail.ksw26141.config.SheetMusicConfig;
import com.gmail.ksw26141.eventListener.PlayerInstrumentInteractHandler;
import com.gmail.ksw26141.eventListener.PlayerQuitHandler;
import com.gmail.ksw26141.model.InstrumentPitch;
import com.gmail.ksw26141.util.InstrumentUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gmail.ksw26141.Constants.*;
import static com.gmail.ksw26141.config.SheetMusicConfig.*;

public class BlankInMusic extends JavaPlugin {

    private final BukkitScheduler scheduler = getServer().getScheduler();


    public void sendMessage(CommandSender sender, String Message) {
        sender.sendMessage(Message);
        getLogger().info(Message);
    }


    @Override
    public void onEnable() {
        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerInstrumentInteractHandler(getConfig()), this);
        pluginManager.registerEvents(new PlayerQuitHandler(), this);
        getLogger().warning(PLUGIN_TITLE + "작동 시작");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().warning(PLUGIN_TITLE + "작동 중지");
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if ("blankinmusic".equalsIgnoreCase(command.getName())) {//플러그인 재설정 명령어
            SheetMusicConfig.clearConfig();
            sendMessage(sender, PLUGIN_TITLE + "현재 플러그인 버전 18.20220909 | 변수들이 초기화 되었습니다!");
            return true;
        } else if (smartMusic(sender, command, args)) {
            return true;
        } else if (sheetMusic(sender, command)) {
            return true;
        } else if (tagCommands(sender, command, args)) {
            return true;
        }

        return false; // 모든 명령어 처리에서 false 발생시 최종적으로 false 반환
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // TODO: 명령어 별 작업 필요.
        return Collections.emptyList();
    }

    private boolean tagCommands(CommandSender sender, Command command, String[] args) {
        if (command.getName().equalsIgnoreCase("musictag")) {//악기태그 명령어
            if (args.length > 0) {
                var tag = new StringBuilder();
                for (int index = 0; index < args.length; ++index) {
                    tag.append(args[index]);
                    if (index + 1 != args.length) {
                        tag.append(" ");
                    }
                }
                var player = getServer().getPlayer(sender.getName());
                var handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType().equals(Material.AIR)) {
                    sender.sendMessage(RED_PREFIX + "손에 아이템을 들어주세요.");
                    return true;
                }
                var itemMeta = handItem.getItemMeta();
                var list = new ArrayList<String>();
                list.add(ChatColor.BLACK + tag.toString());
                itemMeta.setLore(list);
                handItem.setItemMeta(itemMeta);
                sender.sendMessage(PLUGIN_TITLE + "악기 태그로 " + ChatColor.RED + tag + ChatColor.GRAY + " 등록완료");
                return true;
            }
            return false;
        } else if (command.getName().equalsIgnoreCase("tagadd")) {
            if (args.length < 2) {
                return false;
            }
            var tag = new StringBuilder();
            var sound = args[0];
            for (int index = 1; index < args.length; ++index) {
                tag.append(args[index]);
                if (index + 1 != args.length) {
                    tag.append(" ");
                }
            }
            getConfig().set("tag." + tag, sound);
            sendMessage(sender, PLUGIN_TITLE + sound + " 소리가 " + tag + "로 등록되었습니다.");
            return true;
        }
        return false;
    }

    private boolean smartMusic(CommandSender sender, Command command, String[] args) {//유저 편의기능 명령어 모음
        if (command.getName().equalsIgnoreCase("연주차단")) {
            var player = getServer().getPlayer(sender.getName());
            if (InstrumentMutePlayers.contains(player)) {
                InstrumentMutePlayers.remove(player);
                sender.sendMessage(GREEN_PREFIX + "연주 소리 차단이 해제되었습니다.");
            } else {
                InstrumentMutePlayers.add(player);
                sender.sendMessage(RED_PREFIX + "연주 소리가 차단 되었습니다.");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("지휘자")) {
            if (args.length > 1) {
                sender.sendMessage(RED_PREFIX + "동시에 연주를 시작할 지휘자의 닉네임을 적어주세요. /지휘자 <닉네임>");
            } else if (args.length == 1) {
                if (args[0].equals(sender.getName())) {
                    sender.sendMessage(RED_PREFIX + "자기자신을 지휘자로 등록할 수 없습니다.");
                    return true;
                }
                if (!PlayerSheet.containsKey(sender.getName())) {
                    sender.sendMessage(RED_PREFIX + "악보를 등록해주세요.");
                    return true;
                }
                PlayerFollowing.put(sender.getName(), args[0]);
                sender.sendMessage(GREEN_PREFIX + args[0] + "님이 지휘자로 등록되었습니다.");
            } else {
                PlayerFollowing.remove(sender.getName());
                sender.sendMessage(RED_PREFIX + "지휘자 등록이 취소되었습니다.");
            }
            return true;
        }
        return false;
    }

    private boolean sheetMusic(CommandSender sender, Command command) { //악보 명령어 모음
        var item = getServer().getPlayer(sender.getName()).getInventory().getItemInMainHand();
        if (command.getName().equalsIgnoreCase("악보등록")) {
            sheetEncode(sender, item, new ArrayList<>());
            return true;
        } else if (command.getName().equalsIgnoreCase("악보연결")) {
            try {
                var list = PlayerSheet.get(sender.getName());
                if (list != null) {
                    sheetEncode(sender, item, list);
                }
            } catch (Exception e) {
                sender.sendMessage(RED_PREFIX + "악보등록을 먼저 해주세요.");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("악보연주")) {
            if (!PlayerSheet.containsKey(sender.getName())) {
                sender.sendMessage(RED_PREFIX + "악보를 등록해주세요.");
            } else if (PlayerSheetIndex.get(sender.getName()) != 0) {
                sender.sendMessage(RED_PREFIX + "이미 악보를 연주하고 있습니다.");
            } else {
                playSheet(getServer().getPlayer(sender.getName()));
                // 악보 체크하고 연주자들 악보 연주 시작
                for (var follower : PlayerFollowing.keySet()) {// follower=연주할 사람, following=지휘하는 사람
                    var following = PlayerFollowing.get(follower);
                    if (following.equalsIgnoreCase(sender.getName())) {
                        var user = getServer().getPlayer(follower);
                        if (PlayerSheetIndex.get(follower) != 0) {
                            user.sendRawMessage(RED_PREFIX + "이미 악보를 연주하고 있습니다.");
                        } else {
                            playSheet(user);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void sheetEncode(CommandSender sender, ItemStack item, ArrayList<String> encodedPage) {
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

    private void playSheet(Player user) {//악보 연주 재귀 함수
        try {
            var sheet = PlayerSheet.get(user.getName());
            var index = PlayerSheetIndex.get(user.getName());
            var tick = 0;
            var syllable = sheet.get(index);
            if (index > 1) {
                tick = Integer.parseInt(sheet.get(index - 1));
            }

            scheduler.scheduleSyncDelayedTask(this, () -> {
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
                            playState = InstrumentUtil.playInstrumentItem(user, playingPitch, user.getInventory().getItemInMainHand(), getConfig());
                        }
                    }

                    // 연주 결과 분기
                    if (playState) {
                        if (index + 2 < sheet.size()) {
                            PlayerSheetIndex.put(user.getName(), index + 2);
                            playSheet(user);
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