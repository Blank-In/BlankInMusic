package com.gmail.ksw26141.model;

import lombok.Getter;

@Getter
public class InstrumentSound {

    private final String musicTag;
    private final String itemSound;
    private final InstrumentPitch instrumentPitch;

    public InstrumentSound(String musicTag, String itemSound, int pitchLevel, int semitone) {
        this.musicTag = musicTag;
        this.itemSound = itemSound;
        this.instrumentPitch = new InstrumentPitch(semitone, pitchLevel);
    }

}
