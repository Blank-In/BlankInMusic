package blankin.music.model;

import lombok.Getter;

@Getter
public class InstrumentSound {

  private final String musicTag;
  private final String itemSound;
  private final InstrumentPitch instrumentPitch;

  public InstrumentSound(String musicTag, String itemSound, InstrumentPitch instrumentPitch) {
    this.musicTag = musicTag;
    this.itemSound = itemSound;
    this.instrumentPitch = instrumentPitch;
  }

}
