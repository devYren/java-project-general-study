package com.yren.study.vadDemo.result;

/**
 * @author ChenYu ren
 * @date 2025/9/8
 */
public class VoiceSegment {
    private final float startTime;
    private final float endTime;
    private final float duration;

    public VoiceSegment(float startTime, float endTime, float duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public float getStartTime() { return startTime; }
    public float getEndTime() { return endTime; }
    public float getDuration() { return duration; }

    @Override
    public String toString() {
        return String.format("Voice Segment: %.2f-%.2fs (%.2fs)",
                startTime, endTime, duration);
    }
}
