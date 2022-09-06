package com.gmail.ksw26141;

import com.gmail.ksw26141.model.InstrumentPitch;
import com.gmail.ksw26141.model.InstrumentSound;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gmail.ksw26141.util.MusicTagUtil.musicTagToItemSound;

public class BlankInMusic extends JavaPlugin implements Listener {
    String PluginTitle = ChatColor.AQUA + "[" + ChatColor.WHITE + "BlankInMusic" + ChatColor.AQUA + "] " + ChatColor.GRAY;
    String GreenTitle = ChatColor.GRAY + "[" + ChatColor.GREEN + " ! " + ChatColor.GRAY + "] " + ChatColor.YELLOW;
    String RedTitle = ChatColor.GRAY + "[" + ChatColor.RED + " ! " + ChatColor.GRAY + "] " + ChatColor.YELLOW;
    String NameFirst = ChatColor.WHITE + "" + ChatColor.BOLD;

    HashMap<String, Boolean> muteList = new HashMap<>();
    HashMap<String, ArrayList<String>> sheetList = new HashMap<>();
    HashMap<String, Integer> sheetIter = new HashMap<>();
    HashMap<String, String> followList = new HashMap<>();
    BukkitScheduler scheduler = getServer().getScheduler();
    boolean flg = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(PluginTitle + "작동 시작");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info(PluginTitle + "작동 중지");
    }

    public void sendMessage(CommandSender sender, String Message) {
        sender.sendMessage(Message);
        getLogger().info(Message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] player) {
        if (cmd.getName().equalsIgnoreCase("blankinmusic")) {//플러그인 재설정 명령어
            muteList.clear();
            sheetList.clear();
            sheetIter.clear();
            followList.clear();
            sendMessage(sender, PluginTitle + "현재 플러그인 버전 15.20210302 | 변수들이 초기화 되었습니다!");
            return true;
        } else if (smartMusic(sender, cmd, player)) {
            return true;
        } else if (sheetMusic(sender, cmd)) {
            return true;
        } else if (tagCommands(sender, cmd, player)) {
            return true;
        }

        return false; // 모든 명령어 처리에서 false 발생시 최종적으로 false 반환
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
                flg = !flg;
                if (flg) {
                    return;
                }
            }
            var handItem = user.getInventory().getItemInMainHand();
            if (handItemCheck(user, instrumentPitch, handItem)) {
                event.setCancelled(true);
            }
        } else if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            var handItem = user.getInventory().getItemInOffHand();
            if (handItemCheck(user, instrumentPitch, handItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {//에러방지 메모리 절약 플레이어 나갈 시 데이터 제거
        muteList.remove(event.getPlayer().getName());
        sheetList.remove(event.getPlayer().getName());
        sheetIter.remove(event.getPlayer().getName());
        followList.remove(event.getPlayer().getName());
    }

    public boolean tagCommands(CommandSender sender, Command cmd, String[] player) {
        if (cmd.getName().equalsIgnoreCase("musictag")) {//악기태그 명령어
            if (player.length > 0) {
                var tag = new StringBuilder();
                for (int a = 0; a < player.length; ++a) {
                    tag.append(player[a]);
                    if (a + 1 != player.length) {
                        tag.append(" ");
                    }
                }
                var user = getServer().getPlayer(sender.getName());
                var handItem = user.getInventory().getItemInMainHand();
                if (handItem.getType().equals(Material.AIR)) {
                    sender.sendMessage(RedTitle + "손에 아이템을 들어주세요.");
                    return true;
                }
                var itemMeta = handItem.getItemMeta();
                var list = new ArrayList<String>();
                list.add(ChatColor.BLACK + tag.toString());
                itemMeta.setLore(list);
                handItem.setItemMeta(itemMeta);
                sender.sendMessage(PluginTitle + "악기 태그로 " + ChatColor.RED + tag + ChatColor.GRAY + " 등록완료");
                return true;
            }
            return false;
        } else if (cmd.getName().equalsIgnoreCase("tagadd")) {
            if (player.length < 2) {
                return false;
            }
            var tag = new StringBuilder();
            var sound = player[0];
            for (int a = 1; a < player.length; ++a) {
                tag.append(player[a]);
                if (a + 1 != player.length) {
                    tag.append(" ");
                }
            }
            getConfig().set("tag." + tag, sound);
            sendMessage(sender, PluginTitle + sound + " 소리가 " + tag + "로 등록되었습니다.");
            return true;
        }
        return false;
    }

    public boolean smartMusic(CommandSender sender, Command cmd, String[] player) {//유저 편의기능 명령어 모음
        if (cmd.getName().equalsIgnoreCase("연주차단")) {
            var playerName = sender.getName();
            if (muteList.containsKey(playerName)) {
                muteList.remove(playerName);
                sender.sendMessage(GreenTitle + "연주 소리 차단이 해제되었습니다.");
            } else {
                muteList.put(playerName, true);
                sender.sendMessage(RedTitle + "연주 소리가 차단 되었습니다.");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("지휘자")) {
            if (player.length > 1) {
                sender.sendMessage(RedTitle + "동시에 연주를 시작할 지휘자의 닉네임을 적어주세요. /지휘자 <닉네임>");
            } else if (player.length == 1) {
                if (player[0].equals(sender.getName())) {
                    sender.sendMessage(RedTitle + "자기자신을 지휘자로 등록할 수 없습니다.");
                    return true;
                }
                if (!sheetList.containsKey(sender.getName())) {
                    sender.sendMessage(RedTitle + "악보를 등록해주세요.");
                    return true;
                }
                followList.put(sender.getName(), player[0]);
                sender.sendMessage(GreenTitle + player[0] + "님이 지휘자로 등록되었습니다.");
            } else {
                followList.remove(sender.getName());
                sender.sendMessage(RedTitle + "지휘자 등록이 취소되었습니다.");
            }
            return true;
        }
        return false;
    }

    public boolean sheetMusic(CommandSender sender, Command cmd) { //악보 명령어 모음
        var item = getServer().getPlayer(sender.getName()).getInventory().getItemInMainHand();
        if (cmd.getName().equalsIgnoreCase("악보등록")) {
            sheetEncode(sender, item, new ArrayList<>());
            return true;
        } else if (cmd.getName().equalsIgnoreCase("악보연결")) {
            try {
                var list = sheetList.get(sender.getName());
                if (list != null) {
                    sheetEncode(sender, item, list);
                }
            } catch (Exception e) {
                sender.sendMessage(RedTitle + "악보등록을 먼저 해주세요.");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("악보연주")) {
            if (!sheetList.containsKey(sender.getName())) {
                sender.sendMessage(RedTitle + "악보를 등록해주세요.");
            } else if (sheetIter.get(sender.getName()) != 0) {
                sender.sendMessage(RedTitle + "이미 악보를 연주하고 있습니다.");
            } else {
                playSheet(getServer().getPlayer(sender.getName()));
                // 악보 체크하고 연주자들 악보 연주 시작
                for (var follower : followList.keySet()) {// follower=연주할 사람, following=지휘하는 사람
                    var following = followList.get(follower);
                    if (following.equalsIgnoreCase(sender.getName())) {
                        var user = getServer().getPlayer(follower);
                        if (sheetIter.get(follower) != 0) {
                            user.sendRawMessage(RedTitle + "이미 악보를 연주하고 있습니다.");
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

    public void sheetEncode(CommandSender sender, ItemStack item, ArrayList<String> encodedPage) {
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
            sheetList.put(sender.getName(), encodedPage);
            sheetIter.put(sender.getName(), 0);
            sender.sendMessage(GreenTitle + "악보가 등록되었습니다.");
        } else {
            sender.sendMessage(RedTitle + "악보를 손에 들어주세요.");
        }
    }

    public void playSheet(Player user) {//악보 연주 재귀 함수
        try {
            var sheet = sheetList.get(user.getName());
            var index = sheetIter.get(user.getName());
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
                            playState = handItemCheck(user, playingPitch, user.getInventory().getItemInMainHand());
                        }
                    }

                    // 연주 결과 분기
                    if (playState) {
                        if (index + 2 < sheet.size()) {
                            sheetIter.put(user.getName(), index + 2);
                            playSheet(user);
                        } else {
                            sheetIter.put(user.getName(), 0);
                            user.sendRawMessage(GreenTitle + "악보 연주가 종료되었습니다.");
                        }
                    } else {
                        sheetIter.put(user.getName(), 0);
                        user.sendRawMessage(RedTitle + "악기를 손에 들어주세요.");
                    }

                } catch (Exception e) {
                    sheetIter.put(user.getName(), 0);
                    user.sendRawMessage(RedTitle + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
                    getLogger().warning(user.getName() + "님의 악보에서 오류가 발생했습니다. " + e);
                }
            }, tick);
        } catch (Exception e) {
            sheetIter.put(user.getName(), 0);
            user.sendRawMessage(RedTitle + "악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
            getLogger().warning(user.getName() + "님의 악보에서 오류가 발생했습니다. " + e);
        }
    }

    public Boolean handItemCheck(Player user, InstrumentPitch instrumentPitch, ItemStack handItem) {
        if (handItem == null) {
            return false;
        }

        var tag = "";
        try {
            tag = handItem.getItemMeta().getLore().get(0).substring(2);
        } catch (Exception e) {
            return false;
        }

        var instrumentSound = musicTagToItemSound(tag, instrumentPitch, getConfig());
        if (StringUtils.isEmpty(instrumentSound.getItemSound())) {
            return false;
        }

        return playSound(user, instrumentSound, handItem);
    }

    public boolean playSound(Player user, InstrumentSound instrumentSound, ItemStack handItem) {//받아온 정보로 악기 소리를 재생함
        if (muteList.containsKey(user.getName())) {
            user.sendRawMessage(RedTitle + "현재 연주 차단 상태입니다.");
            return false;
        }

        var itemMeta = handItem.getItemMeta();
        var itemSound = instrumentSound.getItemSound();
        var instrumentPitch = instrumentSound.getInstrumentPitch();
        var pitchLevel = instrumentPitch.getPitchLevel();
        var location = user.getLocation().add(0, 1, 0);
        var vector = location.getDirection().multiply(1.5);

        itemMeta.setDisplayName(NameFirst + itemMeta.getLore().get(0).substring(2) + " " + ChatColor.RED + pitchLevel + ChatColor.BLUE + " " + instrumentPitch.getPitchName());
        handItem.setItemMeta(itemMeta);
        user.getWorld().playSound(location.add(vector), itemSound, 2, instrumentPitch.getMinecraftPitch());

        for (var playerName : muteList.keySet()) {
            var player = getServer().getPlayer(playerName);
            if (player != null) {
                player.stopSound(itemSound);
            }
        }
        return true;
    }

}