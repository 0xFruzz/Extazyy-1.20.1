package ru.fruzz.extazyy.misc.util.text;

import java.util.ArrayList;
import java.util.List;

public class AnimationText {
    private List<String> texts = new ArrayList<>();


    public String done = "";

    private int delay = 0;

    public AnimationText(int delay, List<String> texts) {
        this.delay = delay;
        this.texts = texts;
        start();
    }

    public void start() {
        new Thread(() -> {
            try {
                int index = 0;
                while (true) {
                    for (int i = 0; i < texts.get(index).length(); i++) {
                        done += texts.get(index).charAt(i);
                        Thread.sleep(100);
                    }
                    Thread.sleep(delay);
                    for (int i = done.length(); i >= 0; i--) {
                        done = done.substring(0, i);
                        Thread.sleep(60);
                    }
                    if (index >= texts.size() - 1) {
                        index = 0;
                    }
                    index += 1;
                    Thread.sleep(400);
                }
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
