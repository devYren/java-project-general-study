package com.yren.study.vadDemo;

import com.yren.study.vadDemo.audio.AudioComposer;
import com.yren.study.vadDemo.audio.AudioPreprocessor;
import com.yren.study.vadDemo.result.VadResult;
import com.yren.study.vadDemo.result.VoiceSegment;
import com.yren.study.vadDemo.vad.SileroVAD;
import com.yren.study.vadDemo.vad.VoiceSegmentExtractor;

import java.util.List;

/**
 * @author ChenYu ren
 * @date 2025/9/8
 */
public class VADMain {
    public static void main(String[] args) {
        String wavFilePath = "/Users/yren/Documents/studyProject/yren-java-project-general-study/media-vad-project/src/main/resources/audio/元数据.wav";
        String vadModelPath = "/Users/yren/Documents/studyProject/yren-java-project-general-study/media-vad-project/src/main/resources/model/silero_vad.onnx";

        try {
            // 1. 加载和预处理音频
            AudioPreprocessor preprocessor = new AudioPreprocessor();
            float[] audioData = preprocessor.loadAndPreprocessWav(wavFilePath);
            System.out.println("Audio loaded: " + audioData.length + " samples");

            // 2. 初始化VAD模型
            SileroVAD vad = new SileroVAD();
            vad.loadModel(vadModelPath);
            System.out.println("VAD model loaded");

            // 3. 执行VAD检测
            List<VadResult> vadResults = vad.detectVoiceActivity(audioData);
            System.out.println("VAD detection completed: " + vadResults.size() + " windows");

            // 4. 提取人声片段
            VoiceSegmentExtractor extractor = new VoiceSegmentExtractor();
            List<VoiceSegment> voiceSegments = extractor.extractVoiceSegments(vadResults, 0.5f);

            // 5. 输出结果
            System.out.println("\n=== VAD Results ===");
            for (VadResult result : vadResults) {
                if (result.isVoice()) {
                    System.out.println(result);
                }
            }

            System.out.println("\n=== Voice Segments ===");
            for (VoiceSegment segment : voiceSegments) {
                System.out.println(segment);
            }

            // 6. 合成人声片段为wav文件
            if (!voiceSegments.isEmpty()) {
                AudioComposer composer = new AudioComposer();
                String outputPath = "/Users/yren/Desktop/voice_segments_merged.wav";
                
                // 方式1：直接合并所有人声片段（使用200ms缓冲区以避免截断）
                composer.composeVoiceSegments(audioData, voiceSegments, outputPath, 0.2f);
                
                // 方式2：合并人声片段并在片段间添加0.5秒静音间隔
                String outputPathWithGap = "/Users/yren/Desktop/voice_segments_with_gap.wav";
                composer.composeVoiceSegmentsWithGap(audioData, voiceSegments, outputPathWithGap, 0.5f);
            } else {
                System.out.println("未检测到人声片段，无法生成合成音频");
            }

            // 7. 清理资源
            vad.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
