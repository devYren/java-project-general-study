package com.yren.study.vadDemo.audio;

import com.yren.study.vadDemo.result.VoiceSegment;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频合成工具类
 * 用于将VAD检测到的人声片段合成为一个完整的wav文件
 * 
 * @author ChenYu ren
 * @date 2025/9/8
 */
public class AudioComposer {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    // 音频片段前后的缓冲区时间（秒），用于避免截断
    private static final float PADDING_SECONDS = 0.1f; // 100ms缓冲区
    
    /**
     * 将人声片段合成为wav文件
     * 
     * @param originalAudioData 原始音频数据
     * @param voiceSegments 人声片段列表
     * @param outputPath 输出文件路径
     * @throws Exception 处理异常
     */
    public void composeVoiceSegments(float[] originalAudioData, 
                                   List<VoiceSegment> voiceSegments,
                                   String outputPath) throws Exception {
        composeVoiceSegments(originalAudioData, voiceSegments, outputPath, PADDING_SECONDS);
    }
    
    /**
     * 将人声片段合成为wav文件（可配置缓冲区）
     * 
     * @param originalAudioData 原始音频数据
     * @param voiceSegments 人声片段列表
     * @param outputPath 输出文件路径
     * @param paddingSeconds 前后缓冲区时间（秒）
     * @throws Exception 处理异常
     */
    public void composeVoiceSegments(float[] originalAudioData, 
                                   List<VoiceSegment> voiceSegments,
                                   String outputPath,
                                   float paddingSeconds) throws Exception {
        
        // 提取所有人声片段的音频数据
        List<float[]> voiceAudioSegments = extractVoiceAudioSegments(originalAudioData, voiceSegments, paddingSeconds);
        
        // 合并所有人声片段
        float[] mergedAudio = mergeAudioSegments(voiceAudioSegments);
        
        // 写入wav文件
        writeToWavFile(mergedAudio, outputPath);
        
        System.out.println("人声片段已合成并保存到: " + outputPath);
        System.out.println("合成音频时长: " + (mergedAudio.length / (float)SAMPLE_RATE) + "秒");
        System.out.println("使用缓冲区: " + (paddingSeconds * 1000) + "ms");
    }
    
    /**
     * 从原始音频数据中提取人声片段（带缓冲区以避免截断）
     */
    private List<float[]> extractVoiceAudioSegments(float[] originalAudioData, List<VoiceSegment> voiceSegments) {
        return extractVoiceAudioSegments(originalAudioData, voiceSegments, PADDING_SECONDS);
    }
    
    /**
     * 从原始音频数据中提取人声片段（可配置缓冲区，自动处理重叠）
     */
    private List<float[]> extractVoiceAudioSegments(float[] originalAudioData, List<VoiceSegment> voiceSegments, float paddingSeconds) {
        if (voiceSegments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 预处理：合并重叠的片段
        List<VoiceSegment> mergedSegments = mergeOverlappingSegments(voiceSegments, paddingSeconds);
        
        List<float[]> segments = new ArrayList<>();
        int paddingSamples = (int)(paddingSeconds * SAMPLE_RATE);
        
        for (VoiceSegment segment : mergedSegments) {
            // 计算样本索引（添加缓冲区）
            int originalStartSample = (int)(segment.getStartTime() * SAMPLE_RATE);
            int originalEndSample = (int)(segment.getEndTime() * SAMPLE_RATE);
            
            // 添加前后缓冲区
            int startSample = originalStartSample - paddingSamples;
            int endSample = originalEndSample + paddingSamples;
            
            // 确保索引在有效范围内
            startSample = Math.max(0, startSample);
            endSample = Math.min(originalAudioData.length, endSample);
            
            if (startSample < endSample) {
                // 提取音频片段
                float[] segmentAudio = new float[endSample - startSample];
                System.arraycopy(originalAudioData, startSample, segmentAudio, 0, segmentAudio.length);
                segments.add(segmentAudio);
                
                float actualStartTime = startSample / (float)SAMPLE_RATE;
                float actualEndTime = endSample / (float)SAMPLE_RATE;
                System.out.println(String.format("提取人声片段: %.2f-%.2fs -> %.2f-%.2fs (含%.1fms缓冲区, %d samples)", 
                    segment.getStartTime(), segment.getEndTime(), 
                    actualStartTime, actualEndTime,
                    paddingSeconds * 1000, segmentAudio.length));
            }
        }
        
        return segments;
    }
    
    /**
     * 合并重叠的人声片段（考虑缓冲区）
     */
    private List<VoiceSegment> mergeOverlappingSegments(List<VoiceSegment> segments, float paddingSeconds) {
        if (segments.size() <= 1) {
            return new ArrayList<>(segments);
        }
        
        // 按开始时间排序
        List<VoiceSegment> sortedSegments = new ArrayList<>(segments);
        sortedSegments.sort((a, b) -> Float.compare(a.getStartTime(), b.getStartTime()));
        
        List<VoiceSegment> mergedSegments = new ArrayList<>();
        VoiceSegment current = sortedSegments.get(0);
        
        for (int i = 1; i < sortedSegments.size(); i++) {
            VoiceSegment next = sortedSegments.get(i);
            
            // 检查是否重叠（考虑缓冲区）
            float currentEndWithPadding = current.getEndTime() + paddingSeconds;
            float nextStartWithPadding = next.getStartTime() - paddingSeconds;
            
            if (currentEndWithPadding >= nextStartWithPadding) {
                // 重叠，合并片段
                float mergedStart = Math.min(current.getStartTime(), next.getStartTime());
                float mergedEnd = Math.max(current.getEndTime(), next.getEndTime());
                float mergedDuration = mergedEnd - mergedStart;
                
                current = new VoiceSegment(mergedStart, mergedEnd, mergedDuration);
                System.out.println(String.format("合并重叠片段: %.2f-%.2fs + %.2f-%.2fs -> %.2f-%.2fs", 
                    segments.get(i-1).getStartTime(), segments.get(i-1).getEndTime(),
                    next.getStartTime(), next.getEndTime(),
                    mergedStart, mergedEnd));
            } else {
                // 不重叠，保存当前片段
                mergedSegments.add(current);
                current = next;
            }
        }
        
        // 添加最后一个片段
        mergedSegments.add(current);
        
        return mergedSegments;
    }
    
    /**
     * 合并多个音频片段
     */
    private float[] mergeAudioSegments(List<float[]> segments) {
        if (segments.isEmpty()) {
            return new float[0];
        }
        
        // 计算总长度
        int totalLength = 0;
        for (float[] segment : segments) {
            totalLength += segment.length;
        }
        
        // 合并音频数据
        float[] mergedAudio = new float[totalLength];
        int currentPos = 0;
        
        for (float[] segment : segments) {
            System.arraycopy(segment, 0, mergedAudio, currentPos, segment.length);
            currentPos += segment.length;
        }
        
        return mergedAudio;
    }
    
    /**
     * 将音频数据写入wav文件
     */
    private void writeToWavFile(float[] audioData, String outputPath) throws Exception {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, CHANNELS);
        
        try {
            // 设置音频参数
            recorder.setAudioChannels(CHANNELS);
            recorder.setSampleRate(SAMPLE_RATE);
            recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_PCM_S16LE);
            recorder.setFormat("wav");
            
            // 开始录制
            recorder.start();
            
            // 转换float数据为short数据
            short[] shortData = new short[audioData.length];
            for (int i = 0; i < audioData.length; i++) {
                // 将[-1, 1]范围的float转换为[-32768, 32767]范围的short
                shortData[i] = (short)(audioData[i] * 32767.0f);
            }
            
            // 创建音频帧
            ShortBuffer buffer = ShortBuffer.wrap(shortData);
            Frame audioFrame = new Frame();
            audioFrame.sampleRate = SAMPLE_RATE;
            audioFrame.audioChannels = CHANNELS;
            audioFrame.samples = new ShortBuffer[]{buffer};
            
            // 录制音频帧
            recorder.record(audioFrame);
            
        } finally {
            // 停止录制
            recorder.stop();
            recorder.release();
        }
    }
    
    /**
     * 将人声片段合成为wav文件（带间隔）
     * 
     * @param originalAudioData 原始音频数据
     * @param voiceSegments 人声片段列表
     * @param outputPath 输出文件路径
     * @param silenceGapSeconds 片段间的静音间隔（秒）
     * @throws Exception 处理异常
     */
    public void composeVoiceSegmentsWithGap(float[] originalAudioData, 
                                           List<VoiceSegment> voiceSegments, 
                                           String outputPath,
                                           float silenceGapSeconds) throws Exception {
        
        // 提取所有人声片段的音频数据
        List<float[]> voiceAudioSegments = extractVoiceAudioSegments(originalAudioData, voiceSegments);
        
        // 合并所有人声片段（带间隔）
        float[] mergedAudio = mergeAudioSegmentsWithGap(voiceAudioSegments, silenceGapSeconds);
        
        // 写入wav文件
        writeToWavFile(mergedAudio, outputPath);
        
        System.out.println("人声片段已合成并保存到: " + outputPath);
        System.out.println("合成音频时长: " + (mergedAudio.length / (float)SAMPLE_RATE) + "秒");
    }
    
    /**
     * 合并多个音频片段（带静音间隔）
     */
    private float[] mergeAudioSegmentsWithGap(List<float[]> segments, float silenceGapSeconds) {
        if (segments.isEmpty()) {
            return new float[0];
        }
        
        int silenceGapSamples = (int)(silenceGapSeconds * SAMPLE_RATE);
        
        // 计算总长度（包括间隔）
        int totalLength = 0;
        for (float[] segment : segments) {
            totalLength += segment.length;
        }
        // 添加间隔长度（最后一个片段后不加间隔）
        totalLength += (segments.size() - 1) * silenceGapSamples;
        
        // 合并音频数据
        float[] mergedAudio = new float[totalLength];
        int currentPos = 0;
        
        for (int i = 0; i < segments.size(); i++) {
            float[] segment = segments.get(i);
            
            // 复制音频片段
            System.arraycopy(segment, 0, mergedAudio, currentPos, segment.length);
            currentPos += segment.length;
            
            // 添加静音间隔（除了最后一个片段）
            if (i < segments.size() - 1) {
                // 静音数据已经是0，不需要额外设置
                currentPos += silenceGapSamples;
            }
        }
        
        return mergedAudio;
    }
}