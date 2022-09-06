package com.gmail.ksw26141;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("deprecation")
public class BlankInMusic extends JavaPlugin implements Listener{
	String PluginTitle=ChatColor.AQUA+"["+ChatColor.WHITE+"BlankInMusic"+ChatColor.AQUA+"] "+ChatColor.GRAY;
	String GreenTitle=ChatColor.GRAY+"["+ChatColor.GREEN+"+"+ChatColor.GRAY+"] "+ChatColor.YELLOW;
	String RedTitle=ChatColor.GRAY+"["+ChatColor.RED+"+"+ChatColor.GRAY+"] "+ChatColor.YELLOW;
	String NameFirst=ChatColor.WHITE+""+ChatColor.BOLD;
	String[][] oct= {
	/*서있기*/{"F# 파#","G 솔","A 라","B 시","C 도","D 레","E 미","F 파","G 솔","A 라","B 시","C 도","D 레","E 미","F 파"},
	/*웅크림*/{"F# 파#","G# 솔#","A# 라#","B 시","C# 도#","D# 레#","E 미","F# 파#","G# 솔#","A# 라#","B 시","C# 도#","D# 레#","E 미","F# 파#"}
	};
	Float[][] octData={
	/*서있기*/{0.5f,0.529732f,0.594604f,0.667420f,0.707107f,0.793701f,0.890899f,0.943874f,1.059463f,1.189207f,1.334840f,1.414214f,1.587401f,1.781797f,1.887749f},
	/*웅크림*/{0.5f,0.561231f,0.629961f,0.667420f,0.749154f,0.840896f,0.890899f,1f,1.122462f,1.259921f,1.334840f,1.498307f,1.681793f,1.781797f,2f}
	};
	HashMap<String,Boolean> muteList=new HashMap<String,Boolean>();
	HashMap<String,ArrayList<String>> sheetList=new HashMap<String, ArrayList<String>>();
	HashMap<String,Integer> sheetIter=new HashMap<String, Integer>();
	HashMap<String,String> followList=new HashMap<String, String>();
	BukkitScheduler scheduler = getServer().getScheduler();
	boolean flg=false;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info(PluginTitle+"작동 시작");
	}
	
	@Override
	public void onDisable() {
		saveConfig();
		getLogger().info(PluginTitle+"작동 중지");
	}
	
	public void sendMessage(CommandSender sender,String Message) {
		sender.sendMessage(Message);
		getLogger().info(Message);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] player){
		if(cmd.getName().equalsIgnoreCase("blankinmusic")) {//플러그인 재설정 명령어
			muteList.clear();
			sheetList.clear();
			sheetIter.clear();
			followList.clear();
			sendMessage(sender, PluginTitle+"현재 플러그인 버전 15.20210302 | 변수들이 초기화 되었습니다!");
			return true;//명령어 처리에서 true 발생시 최종적으로 true 반환
		}
		else if(smartMusic(sender, cmd, player)) {
			return true;
		}
		else if(sheetMusic(sender,cmd)) {
			return true;
		}
		else if(tagCommands(sender, cmd, player)) {
			return true;
		}
		return false;//모든 명령어 처리에서 false 발생시 최종적으로 false 반환
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {//악기 들고 우클릭시 처리
		Player user = event.getPlayer();
		Action action = event.getAction();
		int octNumber=(int)((user.getLocation().getPitch()*-1+90)/12.857142857142857142857142857143);
		int halfSound=0;
		if(event.getPlayer().isSneaking()) {
			halfSound=1;
		}
	    if(action.equals(Action.RIGHT_CLICK_AIR)||action.equals(Action.RIGHT_CLICK_BLOCK)) {
	    	if(action.equals(Action.RIGHT_CLICK_BLOCK)) {
				flg=!flg;
				if(flg) {
					return;
				}
			}
	    	ItemStack handItem=user.getInventory().getItemInMainHand();
	    	if(handItemCheck(user,octNumber,halfSound,handItem)) {
	    		event.setCancelled(true);
	    	}
	    }
	    else if(action.equals(Action.LEFT_CLICK_AIR)||action.equals(Action.LEFT_CLICK_BLOCK)) {
	    	ItemStack handItem=user.getInventory().getItemInOffHand();
	    	if(handItemCheck(user,octNumber,halfSound,handItem)) {
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
	
	public boolean tagCommands(CommandSender sender,Command cmd,String[] player) {
		if(cmd.getName().equalsIgnoreCase("musictag")) {//악기태그 명령어
			if(player.length>0) {
				String tag="";
				for(int a=0;a<player.length;++a) {
					tag+=player[a];
					if(a+1!=player.length) {
						tag+=" ";
					}
				}
				Player user=getServer().getPlayer(sender.getName());
				ItemStack handItem=user.getInventory().getItemInMainHand();
				if(handItem.getType().equals(Material.AIR)) {
					sender.sendMessage(RedTitle+"손에 아이템을 들어주세요.");
					return true;
				}
				ItemMeta itemMeta=handItem.getItemMeta();
				List<String> list=new ArrayList<String>();
				list.add(ChatColor.BLACK+tag);
				itemMeta.setLore(list);
				handItem.setItemMeta(itemMeta);
				sender.sendMessage(PluginTitle+"악기 태그로 "+ChatColor.RED+tag+ChatColor.GRAY+" 등록완료");
				return true;
			}
			return false;
		}
		else if(cmd.getName().equalsIgnoreCase("tagadd")) {
			if(player.length<2) {
				return false;
			}
			String tag="",sound=player[0];
			for(int a=1;a<player.length;++a) {
				tag+=player[a];
				if(a+1!=player.length) {
					tag+=" ";
				}
			}
			getConfig().set("tag."+tag, sound);
			sendMessage(sender, PluginTitle+sound+" 소리가 "+tag+"로 등록되었습니다.");
			return true;
		}
		return false;
	}

	public boolean smartMusic(CommandSender sender,Command cmd,String[] player) {//유저 편의기능 명령어 모음
		if(cmd.getName().equalsIgnoreCase("연주차단")) {
			String playerName=sender.getName();
			if(muteList.containsKey(playerName)) {
				muteList.remove(playerName);
				sender.sendMessage(GreenTitle+"연주 소리 차단이 해제되었습니다.");
			}
			else {
				muteList.put(playerName, true);
				sender.sendMessage(RedTitle+"연주 소리가 차단 되었습니다.");
			}
			return true; 
		}
		else if(cmd.getName().equalsIgnoreCase("지휘자")) {
			if(player.length>1) {
				sender.sendMessage(RedTitle+"동시에 연주를 시작할 지휘자의 닉네임을 적어주세요. /지휘자 <닉네임>");
			}
			else if(player.length==1){
				if(player[0].equals(sender.getName())) {
					sender.sendMessage(RedTitle+"자기자신을 지휘자로 등록할 수 없습니다.");
					return true;
				}
				if(!sheetList.containsKey(sender.getName())) {
					sender.sendMessage(RedTitle+"악보를 등록해주세요.");
					return true;
				}
				followList.put(sender.getName(),player[0]);
				sender.sendMessage(GreenTitle+player[0]+"님이 지휘자로 등록되었습니다.");
			}
			else {
				followList.remove(sender.getName());
				sender.sendMessage(RedTitle+"지휘자 등록이 취소되었습니다.");
			}
			return true;
		}
		return false;
	}
	
	public boolean sheetMusic(CommandSender sender,Command cmd) { //악보 명령어 모음
		ItemStack item=getServer().getPlayer(sender.getName()).getItemInHand();
		if(cmd.getName().equalsIgnoreCase("악보등록")) {
			sheetIncode(sender, item, new ArrayList<String>());
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("악보연결")) {
			try {
				ArrayList<String> list=sheetList.get(sender.getName());
				if(list!=null) {
					sheetIncode(sender, item, list);
				}
			}
			catch(Exception e) {
				sender.sendMessage(RedTitle+"악보등록을 먼저 해주세요.");
			}
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("악보연주")) {
			if(!sheetList.containsKey(sender.getName())) {
				sender.sendMessage(RedTitle+"악보를 등록해주세요.");
			}
			else if(sheetIter.get(sender.getName())!=0) {
				sender.sendMessage(RedTitle+"이미 악보를 연주하고 있습니다.");
			}
			else {
				playSheet(getServer().getPlayer(sender.getName()));
				//악보 체크하고 연주자들 악보 연주 시작
				for(String follower:followList.keySet()) {//follower=연주할 사람, follwing=지휘하는 사람
					String following=followList.get(follower);
					if(following.toUpperCase().equals(sender.getName().toUpperCase())) {
						Player user=getServer().getPlayer(follower);
						if(sheetIter.get(follower)!=0) {
							user.sendRawMessage(RedTitle+"이미 악보를 연주하고 있습니다.");
						}
						else {
							playSheet(user);
						}
					}
				}
			}
			return true;
		}
		return false;
	}
	
	public void sheetIncode(CommandSender sender,ItemStack item,ArrayList<String> incodePage) {
		int itemId=item.getData().getItemType().getId();
		if(itemId==386||itemId==387) {
			BookMeta sheet=(BookMeta)item.getItemMeta();
			List<String> pages=sheet.getPages();
			for(int a=0;a<pages.size();++a) {
				String[] page=pages.get(a).replace("§0", "").replace("\n", " ").replace("  ", " ").replace("\n\n", "\n").replace(" \n", " ").replace("\n ", " ").split(" ");
				for(int b=0;b<page.length;++b) {
					if(page[b].contains("//")) {//주석처리
						break;
					}
					incodePage.add(page[b]);
				}
			}
			sheetList.put(sender.getName(), incodePage);
			sheetIter.put(sender.getName(), 0);
			sender.sendMessage(GreenTitle+"악보가 등록되었습니다.");
		}
		else {
			sender.sendMessage(RedTitle+"악보를 손에 들어주세요.");
		}
	}
	
	public void playSheet(Player user) {//악보 연주 재귀 함수 
		try {
			ArrayList<String> sheet=sheetList.get(user.getName());
			int index=sheetIter.get(user.getName()), tick=0;
			String oct=sheet.get(index);
			if(index>1) {
				tick=Integer.parseInt(sheet.get(index-1));
			}
			scheduler.scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					try {
						ArrayList<OctClass> octList=new ArrayList<OctClass>();
						OctClass tempOct=new OctClass();
						//추출부분 <이곳을 수정하여 기능 수정>
						for(int a=0;a<oct.length();++a) {
							char octPick=oct.charAt(a);
							switch(octPick) {
								case '+'://동시에 연주할 음 추가
									octList.add(tempOct);
									tempOct=new OctClass();
									break;
								case '#'://반음 처리
									tempOct.halfSound=1;
									break;
								case '-':
									tempOct.octNumber-=20;
									break;
								case 'F':{
									PlayerInventory inven=user.getInventory();
									ItemStack mainHand=inven.getItemInMainHand();
									inven.setItemInMainHand(inven.getItemInOffHand());
									inven.setItemInOffHand(mainHand);
									break;
								}
								case 'C':{
									++a;
									int slotNumber=oct.charAt(a)-'1';
									user.getInventory().setHeldItemSlot(slotNumber);
									break;
								}
								default://숫자 처리
									tempOct.octNumber*=10;//10의 자리 처리
									tempOct.octNumber+=Integer.parseInt(octPick+"");
									break;
							}
						}
						octList.add(tempOct);
						//연주부분
						int playState=0;//상태
						for(int a=0;a<octList.size();++a) {
							tempOct=octList.get(a);
							int octNumber=tempOct.octNumber,halfSound=tempOct.halfSound;
							if(octNumber<0) {
								user.sendRawMessage(ChatColor.DARK_RED+"쉼표");
							}
			            	else if(!handItemCheck(user,octNumber,halfSound,user.getItemInHand())) {//연주가 실패 했는지 체크(연주차단이 켜져있는지 체크)
			            		playState=1;
			            	}
		            	}
						switch(playState) {
							case 0:
								if(index+2<sheet.size()) {
									sheetIter.put(user.getName(), index+2);
									playSheet(user);
								}
								else {
					           		sheetIter.put(user.getName(), 0);
					           		user.sendRawMessage(GreenTitle+"악보 연주가 종료되었습니다.");
					           	}
								break;
							case 1:
								sheetIter.put(user.getName(), 0);
			            		user.sendRawMessage(RedTitle+"악기를 손에 들어주세요.");
								break;
						}
	            	}
	            	catch(Exception e) {
	            		sheetIter.put(user.getName(), 0);
	            		user.sendRawMessage(RedTitle+"악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
	            		System.out.println(user.getName()+"님의 악보에서 오류가 발생했습니다.");
	            	}
	            }
	        },tick);
		}
		catch(Exception e) {
			sheetIter.put(user.getName(), 0);
			user.sendRawMessage(RedTitle+"악보에서 오류가 발생했습니다! 작성양식을 확인해주세요.");
			System.out.println(user.getName()+"님의 악보에서 오류가 발생했습니다.");
		}
	}
	
	public Boolean handItemCheck(Player user,int octNumber,int halfSound,ItemStack handItem) {//손에 있는 아이템을 확인해 악기일 시 소리 재생
		String tag="";
		try {
			tag=handItem.getItemMeta().getLore().get(0).substring(2);
			if(tag.isEmpty()) {
				return false;
			}
		}
		catch(Exception e){
			return false;
		}
		String itemSound=null;
		switch(tag) {
			case "기타":
				itemSound="block.note.guitar";
				break;
			case "종":
				itemSound="block.note.bell";
				break;
			case "차임벨":
				itemSound="block.note.chime";
				break;
			case "드럼":
				switch(octNumber){
					case 0:
						itemSound="block.note.snare";
						break;
					case 1:
						itemSound="block.note.hat";
						break;
					case 2:
						itemSound="block.note.basedrum";
						break;
					default:
						if(getConfig().isSet("tag.drum."+octNumber)) {
							itemSound=getConfig().getString("tag.drum."+octNumber);
							break;
						}
				}
				octNumber=3;
				break;
			case "하프":
				itemSound="block.note.harp";
				break;
			case "플루트":
				itemSound="block.note.flute";
				break;
			case "플링":
				itemSound="block.note.pling";
				break;
			case "실로폰":
				itemSound="block.note.xylophone";
				break;
			case "베이스":
				itemSound="block.note.bass";
				break;
			default:
				if(getConfig().isSet("tag."+tag)) {
					itemSound=getConfig().getString("tag."+tag);
					break;
				}
				return false;
		}
		if(playSound(user,itemSound,octNumber,halfSound,handItem)) {
			return true;
		}
		return false;
	}
	
	public boolean playSound(Player user, String itemSound,int octNumber,int halfSound,ItemStack handItem) {//받아온 정보로 악기 소리를 재생함
		if(muteList.containsKey(user.getName())){
			user.sendRawMessage(RedTitle+"현재 연주 차단 상태입니다.");
			return false;
		}
		
		ItemMeta itemMeta=handItem.getItemMeta();
		itemMeta.setDisplayName(NameFirst+itemMeta.getLore().get(0).substring(2)+" "+ChatColor.RED+""+octNumber+ChatColor.BLUE+" "+oct[halfSound][octNumber]);
		handItem.setItemMeta(itemMeta);
		
		Location location=user.getLocation();
		Server tempServer=getServer();
		user.getWorld().playSound(location, itemSound, 2,octData[halfSound][octNumber]);
		for(String mapkey:muteList.keySet()) {
			tempServer.getPlayer(mapkey).stopSound(itemSound);
		}
		return true;
	}
}