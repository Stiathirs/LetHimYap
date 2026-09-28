package com.lethimyap.client.config;

public class VoicePreviewController {

    public static final String PREVIEW_TEXT = "This is how I sound.";

    private String voiceId;
    private int index;
    private String visibleText = "";
    private boolean playing;

    public void start(String voiceId) {
        this.voiceId = voiceId;
        index = 0;
        visibleText = "";
        playing = true;
    }

    public void stop() {
        voiceId = null;
        index = 0;
        visibleText = "";
        playing = false;
    }

    public void tick() {
        if (!playing) return;

        if (index >= PREVIEW_TEXT.length()) {
            playing = false;
            return;
        }

        char character = PREVIEW_TEXT.charAt(index++);
        visibleText = PREVIEW_TEXT.substring(0, index);

        if (!Character.isWhitespace(character)) {
            VoicePreviewPlayer.play(voiceId);
        }
    }

    public String getVisibleText() {
        return visibleText;
    }

    public boolean isPlaying() {
        return playing;
    }
}