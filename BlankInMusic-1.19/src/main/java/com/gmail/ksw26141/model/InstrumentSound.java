package com.gmail.ksw26141.model;

public class InstrumentSound {

    private final String itemSound;
    private final InstrumentPitch instrumentPitch;

    public InstrumentSound(String itemSound, int pitchLevel, int semitone) {
        this.itemSound = itemSound;
        this.instrumentPitch = new InstrumentPitch(semitone, pitchLevel);
    }


    public String getItemSound() {
        return itemSound;
    }

    public InstrumentPitch getInstrumentPitch() {
        return instrumentPitch;
    }

}
