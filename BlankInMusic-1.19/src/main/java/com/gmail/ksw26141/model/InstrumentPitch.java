package com.gmail.ksw26141.model;

import lombok.Getter;
import lombok.Setter;

public class InstrumentPitch {

    private static final String[][] pitchName = {
        /*서서*/{"F♯ 파♯", "G 솔", "A 라", "B 시", "C 도", "D 레", "E 미", "F 파", "G 솔", "A 라", "B 시", "C 도", "D 레", "E 미", "F 파"},
        /*샤프*/{"F♯ 파♯", "G♯ 솔♯", "A♯ 라♯", "B♯ 시♯", "C♯ 도♯", "D♯ 레♯", "E♯ 미♯", "F♯ 파♯", "G♯ 솔♯", "A♯ 라♯", "B♯ 시♯", "C♯ 도♯", "D♯ 레♯", "E♯ 미♯", "F♯ 파♯"},
        /*플랫*/{"F♯ 파♯", "G♭ 솔♭", "A♭ 라♭", "B♭ 시♭", "C♭ 도♭", "D♭ 레♭", "E♭ 미♭", "F♭ 파♭", "G♭ 솔♭", "A♭ 라♭", "B♭ 시♭", "C♭ 도♭", "D♭ 레♭", "E♭ 미♭", "F♭ 파♭"}
    };
    private static final Float[][] minecraftPitches = {
        /*서서*/{0.5f, 0.529732f, 0.594604f, 0.667420f, 0.707107f, 0.793701f, 0.890899f, 0.943874f, 1.059463f, 1.189207f, 1.334840f, 1.414214f, 1.587401f, 1.781797f, 1.887749f},
        /*샤프*/{0.5f, 0.561231f, 0.629961f, 0.707107f, 0.749154f, 0.840896f, 0.943874f, 1f, 1.122462f, 1.259921f, 1.414214f, 1.498307f, 1.681793f, 1.887749f, 2f},
        /*플랫*/{0.5f, 0.5f, 0.561231f, 0.629961f, 0.667420f, 0.749154f, 0.840896f, 0.890899f, 1f, 1.122462f, 1.259921f, 1.334840f, 1.498307f, 1.681793f, 1.781797f}
    };


    @Getter
    @Setter
    private int semitone = 0;

    @Getter
    @Setter
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

}
