package ru.fruzz.extazyy.misc.util.music;

import lombok.Getter;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.modules.impl.render.Hud;
import ru.fruzz.extazyy.misc.util.music.information.MusicInfo;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Music {

    private List<File> trackList = new ArrayList<>();
    private int currentTrackIndex = 0;
    private SourceDataLine sourceLine;
    private long bytesPlayed = 0;
    private volatile boolean stopPlayback = false;
    @Getter
    private final String musicDir = "C:/Extazyy/music";

    public void init() {
        Mp3Converter.convert(getMusicDir());
        loadMusicFiles(getMusicDir());
        if (!trackList.isEmpty()) {
            MusicInfo.setMusicInfoFromFile(trackList.get(currentTrackIndex));
        }
    }

    public void update() {
        if (Hud.musicstate) {
            Extazyy.getModuleManager().hud.time = MusicInfo.getCurrentTrackTime(sourceLine, bytesPlayed);
        }
    }

    public void playCallback() {
        if (Hud.musicstate) {
            pauseCurrentTrack();
        } else {
            playCurrentTrack();
        }
    }

    public void previousCallback() {
        bytesPlayed = 0;
        stopCurrentTrack();
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
        } else {
            currentTrackIndex = trackList.size() - 1;
        }
        playCurrentTrack();
    }

    public void nextCallback() {
        bytesPlayed = 0;
        stopCurrentTrack();
        if (currentTrackIndex < trackList.size() - 1) {
            currentTrackIndex++;
        } else {
            currentTrackIndex = 0;
        }
        playCurrentTrack();
    }


    public void setState(boolean bool) {
        Hud.musicstate = bool;
    }

    private void loadMusicFiles(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".wav"));
        if (files != null) {
            trackList.addAll(Arrays.asList(files));
        }
    }

    private void playCurrentTrack() {
        File currentTrack = trackList.get(currentTrackIndex);
        MusicInfo.setMusicInfoFromFile(currentTrack);
        setState(true);

        Thread playThread = new Thread(() -> {
                if (currentTrack.getName().endsWith(".wav")) {
                    playWavFile(currentTrack);
                }
        });
        playThread.start();
    }

    private void pauseCurrentTrack() {
        setState(false);
        if (sourceLine != null && sourceLine.isRunning()) {
            stopPlayback = true;
            sourceLine.stop();
        }
    }


    private void stopCurrentTrack() {
        if (sourceLine != null && sourceLine.isRunning()) {
            stopPlayback = true;
            sourceLine.stop();
            sourceLine.close();
        }
    }


    private void playWavFile(File file)  {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(format);
            volume(sourceLine);
            sourceLine.start();

            audioStream.skip(bytesPlayed);
            byte[] buffer = new byte[4096];
            int bytesRead;
            stopPlayback = false;

            while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
                if (stopPlayback) {
                    bytesPlayed += bytesRead;
                    break;
                }
                sourceLine.write(buffer, 0, bytesRead);
                bytesPlayed += bytesRead;
            }

            sourceLine.drain();
            sourceLine.stop();
            sourceLine.close();

            if (!stopPlayback) {
                nextCallback();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void volume(SourceDataLine sourceLine) {
        FloatControl gainControl = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log10(Extazyy.moduleManager.hud.numberTools.getValue().floatValue()) * 25.0);
        gainControl.setValue(dB);
    }

}
