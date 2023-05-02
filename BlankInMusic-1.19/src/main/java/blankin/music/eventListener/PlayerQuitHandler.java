package blankin.music.eventListener;

import blankin.music.config.SheetMusicConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitHandler implements Listener {

  @EventHandler
  private void onPlayerQuit(PlayerQuitEvent event) { //메모리 절약
    var playerUUID = event.getPlayer().getUniqueId();
    SheetMusicConfig.InstrumentMutePlayers.remove(playerUUID);
    SheetMusicConfig.PlayerSheet.remove(playerUUID);
    SheetMusicConfig.PlayerSheetIndex.remove(playerUUID);
    SheetMusicConfig.PlayerFollowing.remove(playerUUID);
  }

}
