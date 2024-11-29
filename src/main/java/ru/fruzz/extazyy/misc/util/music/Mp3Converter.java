package ru.fruzz.extazyy.misc.util.music;


import javazoom.jl.decoder.*;

import javax.sound.sampled.*;
import java.io.*;


//ОСНОВНОЙ АЛГОРИТМ ДАННОГО КОНВЕРТОРА НАПИСАНА С ИСПОЛЬЗОВАНИЕМ ЧАТА ГПТ, ИБО Я В ОЧКО ЕБАЛ ПИСАТЬ ЕГО САМ
public class Mp3Converter {

    public static void convert(String musdir) {
        File filedirs = new File(musdir);

        File[] mp3lst = filedirs.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if(mp3lst.length < 0) return;
        for (File current : mp3lst) {
            File wav = new File(current.getAbsolutePath().replace(".mp3", ".wav").replace("-", "_"));
            try {
                InputStream inputStream = new BufferedInputStream(new FileInputStream(current));
                AudioInputStream audioInputStream = chatgpttech(inputStream);
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wav);
            } catch (Exception e) {
            }
            current.delete();
        }
    }

    private static AudioInputStream chatgpttech(InputStream mp3InputStream) throws JavaLayerException {
        Bitstream bitstream = new Bitstream(mp3InputStream);
        Decoder decoder = new Decoder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        boolean done = false;
        while (!done) {
            Header frameHeader = bitstream.readFrame();
            if (frameHeader == null) {
                done = true;
                break;
            }

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
            short[] pcm = output.getBuffer();
            for (int i = 0; i < pcm.length; i++) {
                outputStream.write((byte) (pcm[i] & 0xff));
                outputStream.write((byte) ((pcm[i] >> 8) & 0xff));
            }
            bitstream.closeFrame();
        }

        byte[] pcmData = outputStream.toByteArray();
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);
        return new AudioInputStream(new ByteArrayInputStream(pcmData), audioFormat, pcmData.length / audioFormat.getFrameSize());
    }
}
