package com.yren.study.vadDemo.vad;

/**
 * @author ChenYu ren
 * @date 2025/9/8
 */
import com.yren.study.vadDemo.result.VadResult;
import com.yren.study.vadDemo.result.VoiceSegment;

import java.util.*;

public class VoiceSegmentExtractor {

    public List<VoiceSegment> extractVoiceSegments(List<VadResult> vadResults,
                                                   float minSegmentDuration) {
        List<VoiceSegment> segments = new ArrayList<>();

        float segmentStart = -1;
        float segmentEnd = -1;

        for (VadResult result : vadResults) {
            if (result.isVoice()) {
                if (segmentStart == -1) {
                    // 开始新的语音段
                    segmentStart = result.getStartTime();
                }
                segmentEnd = result.getEndTime();
            } else {
                if (segmentStart != -1) {
                    // 结束当前语音段
                    float duration = segmentEnd - segmentStart;
                    if (duration >= minSegmentDuration) {
                        segments.add(new VoiceSegment(segmentStart, segmentEnd, duration));
                    }
                    segmentStart = -1;
                }
            }
        }

        // 处理最后一个片段
        if (segmentStart != -1) {
            float duration = segmentEnd - segmentStart;
            if (duration >= minSegmentDuration) {
                segments.add(new VoiceSegment(segmentStart, segmentEnd, duration));
            }
        }

        return segments;
    }
}


