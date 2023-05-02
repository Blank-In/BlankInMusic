package blankin.music.util;

import blankin.music.model.InstrumentPitch;
import blankin.music.model.InstrumentSound;
import org.bukkit.configuration.file.FileConfiguration;

public class MusicTagUtil {

  static public InstrumentSound musicTagToItemSound(String musicTag, InstrumentPitch instrumentPitch, FileConfiguration config) {
    var itemSound = "";

    if ("드럼".equals(musicTag)) {
      var pitchLevel = instrumentPitch.getPitchLevel();
      itemSound = switch (pitchLevel) {
        case 0 -> "block.note_block.snare";
        case 1 -> "block.note_block.hat";
        case 2 -> "block.note_block.basedrum";
        default -> config.isSet("tag.drum." + pitchLevel) ? config.getString("tag.drum." + pitchLevel) : "";
      };
      instrumentPitch.setPitchLevel(4); // switch(pitchLevel) 를 먼저 체크해야 하기 때문에 순서 변경에 주의
    } else {
      itemSound = switch (musicTag) {
        case "베이스" -> "block.note_block.bass";
        case "종" -> "block.note_block.bell";
        case "플루트" -> "block.note_block.flute";
        case "차임벨" -> "block.note_block.chime";
        case "기타" -> "block.note_block.guitar";
        case "실로폰" -> "block.note_block.xylophone";
        case "철 실로폰" -> "block.note_block.iron_xylophone";
        case "카우벨" -> "block.note_block.cow_bell";
        case "디저리두" -> "block.note_block.didgeridoo";
        case "비트" -> "block.note_block.bit";
        case "밴조" -> "block.note_block.banjo";
        case "플링" -> "block.note_block.pling";
        case "하프" -> "block.note_block.harp";
        default -> config.isSet("tag." + musicTag) ? config.getString("tag." + musicTag) : "";
      };
    }

    return new InstrumentSound(musicTag, itemSound, instrumentPitch);
  }

}
