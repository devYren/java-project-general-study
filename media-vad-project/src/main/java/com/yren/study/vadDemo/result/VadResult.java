package com.yren.study.vadDemo.result;

/**
 * @author ChenYu ren
 * @date 2025/9/8
 */
public class VadResult {
    private final float startTime;
    private final float endTime;
    private final float vadScore;
    private final boolean isVoice;

    public VadResult(float startTime, float endTime, float vadScore, boolean isVoice) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.vadScore = vadScore;
        this.isVoice = isVoice;
    }

    // Getters
    public float getStartTime() { return startTime; }
    public float getEndTime() { return endTime; }
    public float getVadScore() { return vadScore; }
    public boolean isVoice() { return isVoice; }

    @Override
    public String toString() {
        return String.format("Time: %.2f-%.2fs, Score: %.3f, Voice: %s",
                startTime, endTime, vadScore, isVoice ? "YES" : "NO");
    }
}
