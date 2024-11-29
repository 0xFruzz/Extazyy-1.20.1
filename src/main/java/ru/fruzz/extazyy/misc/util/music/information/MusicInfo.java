package ru.fruzz.extazyy.misc.util.music.information;

import ru.fruzz.extazyy.main.modules.impl.render.Hud;
import ru.fruzz.extazyy.misc.util.text.AnimationText;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.util.Arrays;

public class MusicInfo {

    public static void setMusicInfoFromFile(File track) {
        String fileName = track.getName();
        String[] parts = fileName.split("_");
        if (parts.length >= 2) {
            String author = parts[0];
            String composition = parts[1].replace(".wav", "");
            setMusicInfo(author, composition);
        }
    }

    public static String getCurrentTrackTime(SourceDataLine sourceLine, long bytesPlayed) {
        if (sourceLine != null && sourceLine.isRunning()) {
            AudioFormat format = sourceLine.getFormat();
            float bytesPerSecond = bytesPlayed / (format.getFrameRate() * format.getFrameSize());
            long minutes = (long) (bytesPerSecond / 60);
            long seconds = (long) (bytesPerSecond % 60);
            return String.format("%02d:%02d", minutes, seconds);
        }
        return "00:00";
    }

    public static void setMusicInfo(String author, String composition) {
        Hud.musictext = new AnimationText(1500, Arrays.asList("", author, composition));
    }


}
