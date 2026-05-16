package com.lethimyap.messages;

import com.lethimyap.api.YapPriority;

public class ActiveThought {

    public enum ThoughtType {
        NORMAL,
        PAIN
    }

    public final String fullMessage;
    public final String garbledFullMessage;
    public final boolean important;
    public final ThoughtType type;
    public final DialogueColor color;
    public final YapPriority priority;

    public String visibleMessage = "";

    public int index = 0;
    public int nextCharDelay = 0;
    public int remainingVisibleTicks;
    public boolean finished = false;

    public ActiveThought(
            String fullMessage,
            String garbledFullMessage,
            int visibleTicks,
            boolean important,
            ThoughtType type,
            DialogueColor color,
            YapPriority priority
    ) {
        this.fullMessage = fullMessage;
        this.garbledFullMessage = garbledFullMessage;
        this.remainingVisibleTicks = visibleTicks;
        this.important = important;
        this.type = type;
        this.color = color;
        this.priority = priority;
    }

    public void appendNextCharacter(boolean muffled) {
        if (index >= fullMessage.length()) return;

        char next = muffled
                ? garbledFullMessage.charAt(index)
                : fullMessage.charAt(index);

        visibleMessage += next;
        index++;
    }
}