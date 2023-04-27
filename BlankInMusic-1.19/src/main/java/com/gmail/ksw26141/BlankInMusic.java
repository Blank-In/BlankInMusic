package com.gmail.ksw26141;

import static com.gmail.ksw26141.Constants.GREEN_PREFIX;
import static com.gmail.ksw26141.Constants.PLUGIN_TITLE;
import static com.gmail.ksw26141.Constants.RED_PREFIX;
import static com.gmail.ksw26141.config.SheetMusicConfig.InstrumentMutePlayers;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerFollowing;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheet;
import static com.gmail.ksw26141.config.SheetMusicConfig.PlayerSheetIndex;
import static com.gmail.ksw26141.util.SheetMusicUtil.playSheet;
import static com.gmail.ksw26141.util.SheetMusicUtil.sheetEncode;
import static org.bukkit.ChatColor.BLACK;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RED;

import com.gmail.ksw26141.config.SheetMusicConfig;
import com.gmail.ksw26141.eventListener.PlayerInstrumentInteractHandler;
import com.gmail.ksw26141.eventListener.PlayerQuitHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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
    if ("blankinmusic".equalsIgnoreCase(command.getName())) { //플러그인 재설정
      SheetMusicConfig.clearConfig();
      sendMessage(sender, PLUGIN_TITLE + "현재 플러그인 버전 21.20230427 | 변수들이 초기화 되었습니다!");
      return true;
    }

    var player = getServer().getPlayer(sender.getName());
    if (player == null) {
      return false;
    }

    if (smartMusicCommands(player, command, args)) {
      return true;
    } else if (sheetMusicCommands(player, command)) {
      return true;
    } else if (tagCommands(player, command, args)) {
      return true;
    }

    return false;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    // TODO: 명령어 별 작업 필요.
    return Collections.emptyList();
  }


  private boolean tagCommands(Player player, Command command, String[] args) {
    switch (command.getName().toLowerCase()) {
      case "musictag" -> {
        if (args.length == 0) {
          return false;
        }

        var tag = new StringBuilder();
        for (String arg : args) {
          tag.append(arg).append(" ");
        }

        var handItem = player.getInventory().getItemInMainHand();
        if (Material.AIR.equals(handItem.getType())) {
          player.sendRawMessage(RED_PREFIX + "아이템을 들어주세요.");
          return true;
        }
        var itemMeta = handItem.getItemMeta();
        var list = new ArrayList<String>();
        list.add(BLACK + tag.toString().trim());
        itemMeta.setLore(list);
        handItem.setItemMeta(itemMeta);

        player.sendMessage(PLUGIN_TITLE + "아이템의 악기태그를 " + RED + tag + GRAY + "로 등록완료");
        return true;
      }
      case "tagadd" -> {
        if (args.length < 2) {
          return false;
        }

        var tag = new StringBuilder();
        var sound = args[0];
        for (var index = 1; index < args.length; ++index) {
          tag.append(args[index]).append(" ");
        }
        getConfig().set("tag." + tag.toString().trim(), sound);
        player.sendRawMessage(PLUGIN_TITLE + RED + sound + GRAY + " 소리가 " + RED + tag + GRAY + "로 등록되었습니다.");
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  private boolean smartMusicCommands(Player player, Command command, String[] args) {
    var playerUUID = player.getUniqueId();

    switch (command.getName()) {
      case "연주차단" -> {
        if (InstrumentMutePlayers.contains(playerUUID)) {
          InstrumentMutePlayers.remove(playerUUID);
          player.sendRawMessage(GREEN_PREFIX + "연주 소리 차단이 해제되었습니다.");
        } else {
          InstrumentMutePlayers.add(playerUUID);
          player.sendRawMessage(RED_PREFIX + "연주 소리가 차단 되었습니다.");
        }
        return true;
      }
      case "지휘자" -> {
        if (args.length == 0) {
          PlayerFollowing.remove(playerUUID);
          player.sendRawMessage(RED_PREFIX + "지휘자 등록이 취소되었습니다.");
          return true;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
          player.sendRawMessage(RED_PREFIX + "자기자신을 지휘자로 등록할 수 없습니다.");
          return true;
        } else if (!PlayerSheet.containsKey(playerUUID)) {
          player.sendRawMessage(RED_PREFIX + "악보를 등록해주세요.");
          return true;
        }

        PlayerFollowing.put(playerUUID, args[0]);
        player.sendRawMessage(GREEN_PREFIX + args[0] + " 님이 지휘자로 등록되었습니다.");
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  private boolean sheetMusicCommands(Player player, Command command) {
    var handItem = player.getInventory().getItemInMainHand();
    var playerUUID = player.getUniqueId();

    switch (command.getName()) {
      case "악보등록" -> {
        sheetEncode(player, handItem, new ArrayList<>());
        return true;
      }
      case "악보연결" -> {
        if (PlayerSheet.containsKey(playerUUID)) {
          var sheet = PlayerSheet.get(playerUUID);
          if (sheet != null) {
            sheetEncode(player, handItem, sheet);
          }
        } else {
          player.sendRawMessage(RED_PREFIX + "악보등록을 먼저 해주세요.");
        }
        return true;
      }
      case "악보연주" -> {
        if (!PlayerSheet.containsKey(playerUUID)) {
          player.sendRawMessage(RED_PREFIX + "악보를 등록해주세요.");
        } else if (PlayerSheetIndex.get(playerUUID) != 0) {
          player.sendRawMessage(RED_PREFIX + "이미 악보를 연주하고 있습니다.");
        } else {
          playSheet(player, getConfig(), this);
          // 악보 체크하고 연주자들 악보 연주 시작
          for (var followerUUID : PlayerFollowing.keySet()) { // follower=따르는 사람, following=지휘하는 사람
            var following = PlayerFollowing.get(followerUUID);
            if (player.getName().equalsIgnoreCase(following)) {
              var followerPlayer = getServer().getPlayer(followerUUID);
              if (followerPlayer != null) {
                if (PlayerSheetIndex.get(followerUUID) != 0) {
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