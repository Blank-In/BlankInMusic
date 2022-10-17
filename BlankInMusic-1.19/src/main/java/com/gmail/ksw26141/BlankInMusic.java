package com.gmail.ksw26141;

import com.gmail.ksw26141.config.SheetMusicConfig;
import com.gmail.ksw26141.eventListener.PlayerInstrumentInteractHandler;
import com.gmail.ksw26141.eventListener.PlayerQuitHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gmail.ksw26141.Constants.*;
import static com.gmail.ksw26141.config.SheetMusicConfig.*;
import static com.gmail.ksw26141.util.SheetMusicUtil.playSheet;
import static com.gmail.ksw26141.util.SheetMusicUtil.sheetEncode;

public class BlankInMusic extends JavaPlugin {

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
            sendMessage(sender, PLUGIN_TITLE + "현재 플러그인 버전 19.20221018 | 변수들이 초기화 되었습니다!");
            return true;
        }

        var user = getServer().getPlayer(sender.getName());
        if (user == null) {
            return false;
        }

        if (smartMusicCommands(user, command, args)) {
            return true;
        } else if (sheetMusicCommands(user, command)) {
            return true;
        } else if (tagCommands(user, command, args)) {
            return true;
        }

        return false; // 모든 명령어 처리에서 false 발생시 최종적으로 false 반환
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // TODO: 명령어 별 작업 필요.
        return Collections.emptyList();
    }


    private boolean tagCommands(Player player, Command command, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "musictag" -> {
                if (args.length > 0) {
                    var tag = new StringBuilder();
                    for (var index = 0; index < args.length; ++index) {
                        tag.append(args[index]);
                        if (index + 1 != args.length) {
                            tag.append(" ");
                        }
                    }

                    var handItem = player.getInventory().getItemInMainHand();
                    if (Material.AIR.equals(handItem.getType())) {
                        player.sendRawMessage(RED_PREFIX + "손에 아이템을 들어주세요.");
                        return true;
                    }
                    var itemMeta = handItem.getItemMeta();
                    var list = new ArrayList<String>();
                    list.add(ChatColor.BLACK + tag.toString());
                    itemMeta.setLore(list);
                    handItem.setItemMeta(itemMeta);

                    player.sendMessage(PLUGIN_TITLE + "악기 태그로 " + ChatColor.RED + tag + ChatColor.GRAY + " 등록완료");
                    return true;
                }
                return false;
            }
            case "tagadd" -> {
                if (args.length < 2) {
                    return false;
                }
                var tag = new StringBuilder();
                var sound = args[0];
                for (var index = 1; index < args.length; ++index) {
                    tag.append(args[index]);
                    if (index + 1 != args.length) {
                        tag.append(" ");
                    }
                }
                getConfig().set("tag." + tag, sound);
                player.sendRawMessage(PLUGIN_TITLE + sound + " 소리가 " + tag + "로 등록되었습니다.");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean smartMusicCommands(Player player, Command command, String[] args) {//유저 편의기능 명령어 모음
        var uuid = player.getUniqueId();

        switch (command.getName()) {
            case "연주차단" -> {
                if (InstrumentMutePlayers.contains(uuid)) {
                    InstrumentMutePlayers.remove(uuid);
                    player.sendRawMessage(GREEN_PREFIX + "연주 소리 차단이 해제되었습니다.");
                } else {
                    InstrumentMutePlayers.add(uuid);
                    player.sendRawMessage(RED_PREFIX + "연주 소리가 차단 되었습니다.");
                }
                return true;
            }
            case "지휘자" -> {
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase(player.getName())) {
                        player.sendRawMessage(RED_PREFIX + "자기자신을 지휘자로 등록할 수 없습니다.");
                        return true;
                    } else if (!PlayerSheet.containsKey(uuid)) {
                        player.sendRawMessage(RED_PREFIX + "악보를 등록해주세요.");
                        return true;
                    }
                    PlayerFollowing.put(uuid, args[0]);
                    player.sendRawMessage(GREEN_PREFIX + args[0] + "님이 지휘자로 등록되었습니다.");
                } else {
                    PlayerFollowing.remove(uuid);
                    player.sendRawMessage(RED_PREFIX + "지휘자 등록이 취소되었습니다.");
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean sheetMusicCommands(Player player, Command command) { //악보 명령어 모음
        var handItem = player.getInventory().getItemInMainHand();
        var uuid = player.getUniqueId();

        switch (command.getName()) {
            case "악보등록" -> {
                sheetEncode(player, handItem, new ArrayList<>());
                return true;
            }
            case "악보연결" -> {
                if (PlayerSheet.containsKey(uuid)) {
                    var sheet = PlayerSheet.get(uuid);
                    if (sheet != null) {
                        sheetEncode(player, handItem, sheet);
                    }
                } else {
                    player.sendRawMessage(RED_PREFIX + "악보등록을 먼저 해주세요.");
                }
                return true;
            }
            case "악보연주" -> {
                if (!PlayerSheet.containsKey(uuid)) {
                    player.sendRawMessage(RED_PREFIX + "악보를 등록해주세요.");
                } else if (PlayerSheetIndex.get(uuid) != 0) {
                    player.sendRawMessage(RED_PREFIX + "이미 악보를 연주하고 있습니다.");
                } else {
                    playSheet(player, getConfig(), this);
                    // 악보 체크하고 연주자들 악보 연주 시작
                    for (var follower : PlayerFollowing.keySet()) { // follower=따르는 사람, following=지휘하는 사람
                        var following = PlayerFollowing.get(follower);
                        if (player.getName().equalsIgnoreCase(following)) {
                            var followerPlayer = getServer().getPlayer(follower);
                            if (followerPlayer != null) {
                                if (PlayerSheetIndex.get(follower) != 0) {
                                    followerPlayer.sendRawMessage(RED_PREFIX + "이미 악보를 연주하고 있습니다.");
                                } else {
                                    playSheet(followerPlayer, getConfig(), this);
                                }
                            }
                        }
                    }
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

}