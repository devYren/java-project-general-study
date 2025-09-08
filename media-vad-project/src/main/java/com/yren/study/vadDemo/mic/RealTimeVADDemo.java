package com.yren.study.vadDemo.mic;

import com.yren.study.vadDemo.result.VadResult;
import com.yren.study.vadDemo.result.VoiceSegment;
import com.yren.study.vadDemo.vad.SileroVAD;
import com.yren.study.vadDemo.vad.VoiceSegmentExtractor;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

/**
 * 实时VAD演示程序
 * 结合麦克风音频捕获和VAD检测，实现实时语音活动检测
 * 
 * @author ChenYu ren
 * @date 2025/1/27
 */
public class RealTimeVADDemo {
    
    private static final String VAD_MODEL_PATH = "/Users/yren/Desktop/silero_vad.onnx";
    private static final int AUDIO_BUFFER_SIZE = 1600; // 100ms at 16kHz
    
    public static void main(String[] args) {
        if (args.length > 0) {
            System.setProperty("vad.model.path", args[0]);
        }
        
        String modelPath = System.getProperty("vad.model.path", VAD_MODEL_PATH);
        
        RealTimeAudioCapture audioCapture = new RealTimeAudioCapture();
        SileroVAD vad = new SileroVAD();
        VoiceSegmentExtractor extractor = new VoiceSegmentExtractor();
        
        try {
            // 初始化组件
            System.out.println("初始化音频捕获设备...");
            audioCapture.initialize();
            
            System.out.println("加载VAD模型: " + modelPath);
            vad.loadModel(modelPath);
            
            System.out.println("\n=== 实时VAD系统已启动 ===");
            System.out.println("开始监听麦克风，实时检测语音活动...");
            System.out.println("按Enter键停止\n");
            
            // 开始音频捕获
            audioCapture.startCapture();
            
            // 创建VAD处理线程
            Thread vadThread = new Thread(() -> processRealTimeVAD(audioCapture, vad, extractor));
            vadThread.setName("RealTimeVADThread");
            vadThread.start();
            
            // 等待用户按Enter键
            System.in.read();
            
            // 停止处理
            audioCapture.stopCapture();
            vadThread.interrupt();
            
            try {
                vadThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("\n实时VAD系统已停止");
            
        } catch (LineUnavailableException e) {
            System.err.println("无法初始化音频设备: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("程序运行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 实时VAD处理方法
     */
    private static void processRealTimeVAD(RealTimeAudioCapture audioCapture, 
                                          SileroVAD vad, 
                                          VoiceSegmentExtractor extractor) {
        
        List<Float> audioBuffer = new ArrayList<>();
        List<VadResult> vadResults = new ArrayList<>();
        int frameCount = 0;
        long startTime = System.currentTimeMillis();
        
        // 语音状态跟踪
        boolean wasVoiceActive = false;
        long voiceStartTime = 0;
        
        while (!Thread.currentThread().isInterrupted() && audioCapture.isCapturing()) {
            try {
                // 获取音频数据
                float[] audioData = audioCapture.getAudioDataBlocking();
                
                if (audioData != null) {
                    frameCount++;
                    
                    // 将音频数据添加到缓冲区
                    for (float sample : audioData) {
                        audioBuffer.add(sample);
                    }
                    
                    // 当缓冲区达到指定大小时进行VAD检测
                    if (audioBuffer.size() >= AUDIO_BUFFER_SIZE) {
                        // 转换为数组
                        float[] vadInput = new float[AUDIO_BUFFER_SIZE];
                        for (int i = 0; i < AUDIO_BUFFER_SIZE; i++) {
                            vadInput[i] = audioBuffer.get(i);
                        }
                        
                        // 执行VAD检测
                        List<VadResult> currentResults = vad.detectVoiceActivity(vadInput);
                        vadResults.addAll(currentResults);
                        
                        // 分析VAD结果
                        boolean isVoiceActive = false;
                        for (VadResult result : currentResults) {
                            if (result.isVoice()) {
                                isVoiceActive = true;
                                break;
                            }
                        }
                        
                        // 检测语音状态变化
                        long currentTime = System.currentTimeMillis();
                        if (isVoiceActive && !wasVoiceActive) {
                            // 语音开始
                            voiceStartTime = currentTime;
                            System.out.printf("[%.1fs] 🎤 语音开始\n", 
                                (currentTime - startTime) / 1000.0);
                        } else if (!isVoiceActive && wasVoiceActive) {
                            // 语音结束
                            long voiceDuration = currentTime - voiceStartTime;
                            System.out.printf("[%.1fs] 🔇 语音结束 (持续时间: %dms)\n", 
                                (currentTime - startTime) / 1000.0, voiceDuration);
                        }
                        
                        wasVoiceActive = isVoiceActive;
                        
                        // 每10帧输出一次状态
                        if (frameCount % 10 == 0) {
                            double elapsedSeconds = (currentTime - startTime) / 1000.0;
                            String status = isVoiceActive ? "🎤 语音" : "🔇 静音";
                            
                            System.out.printf("[%.1fs] %s - 帧#%d, 队列:%d\n",
                                elapsedSeconds, status, frameCount, audioCapture.getQueueSize());
                        }
                        
                        // 移除已处理的数据，保留一些重叠
                        int removeCount = AUDIO_BUFFER_SIZE / 2;
                        audioBuffer.subList(0, removeCount).clear();
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("VAD处理错误: " + e.getMessage());
            }
        }
        
        // 输出最终统计
        System.out.println("\n=== VAD处理统计 ===");
        System.out.println("总处理帧数: " + frameCount);
        System.out.println("VAD检测次数: " + vadResults.size());
        
        // 提取语音片段
        if (!vadResults.isEmpty()) {
            List<VoiceSegment> voiceSegments = extractor.extractVoiceSegments(vadResults, 0.5f);
            System.out.println("检测到语音片段数: " + voiceSegments.size());
            
            for (int i = 0; i < voiceSegments.size(); i++) {
                VoiceSegment segment = voiceSegments.get(i);
                System.out.printf("片段%d: %.2fs - %.2fs (时长: %.2fs)\n", 
                    i + 1, segment.getStartTime(), segment.getEndTime(), segment.getDuration());
            }
        }
    }
}