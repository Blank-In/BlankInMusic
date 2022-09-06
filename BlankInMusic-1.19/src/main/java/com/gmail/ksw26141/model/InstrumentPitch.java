package com.gmail.ksw26141.model;

public class InstrumentPitch {
    final static String[][] pitchName = {
            /*서있기*/{"F# 파#", "G 솔", "A 라", "B 시", "C 도", "D 레", "E 미", "F 파", "G 솔", "A 라", "B 시", "C 도", "D 레", "E 미", "F 파"},
            /*웅크림*/{"F# 파#", "G# 솔#", "A# 라#", "B 시", "C# 도#", "D# 레#", "E 미", "F# 파#", "G# 솔#", "A# 라#", "B 시", "C# 도#", "D# 레#", "E 미", "F# 파#"}
    };
    final static Float[][] minecraftPitches = {
            /*서있기*/{0.5f, 0.529732f, 0.594604f, 0.667420f, 0.707107f, 0.793701f, 0.890899f, 0.943874f, 1.059463f, 1.189207f, 1.334840f, 1.414214f, 1.587401f, 1.781797f, 1.887749f},
            /*웅크림*/{0.5f, 0.561231f, 0.629961f, 0.667420f, 0.749154f, 0.840896f, 0.890899f, 1f, 1.122462f, 1.259921f, 1.334840f, 1.498307f, 1.681793f, 1.781797f, 2f}
    };


    private int semitone = 0;
    private int pitchLevel = 0;


    public InstrumentPitch() {
    }

    public InstrumentPitch(int semitone, int pitchLevel) {
        this.semitone = semitone;
        this.pitchLevel = pitchLevel;
    }


    public float getMinecraftPitch() {
        return minecraftPitches[this.semitone][this.pitchLevel];
    }

    public String getPitchName() {
        return pitchName[this.semitone][this.pitchLevel];
    }


    public int getSemitone() {
        return semitone;
    }

    public void setSemitone(int semitone) {
        this.semitone = semitone;
    }


    public int getPitchLevel() {
        return pitchLevel;
    }

    public void setPitchLevel(int pitchLevel) {
        this.pitchLevel = pitchLevel;
    }
}
