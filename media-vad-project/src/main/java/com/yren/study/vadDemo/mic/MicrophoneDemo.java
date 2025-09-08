package com.yren.study.vadDemo.mic;

import javax.sound.sampled.LineUnavailableException;

/**
 * 麦克风音频捕获演示程序
 * 展示如何使用RealTimeAudioCapture类获取麦克风数据
 * 
 * @author ChenYu ren
 * @date 2025/1/27
 */
public class MicrophoneDemo {
    
    public static void main(String[] args) {
        // 列出可用的音频输入设备
        RealTimeAudioCapture.listAudioInputDevices();
        
        RealTimeAudioCapture audioCapture = new RealTimeAudioCapture();
        
        try {
            // 初始化音频捕获设备
            audioCapture.initialize();
            
            // 开始捕获音频
            audioCapture.startCapture();
            
            System.out.println("\n开始监听麦克风数据，按Enter键停止...");
            
            // 创建音频处理线程
            Thread processingThread = new Thread(() -> processAudioData(audioCapture));
            processingThread.setName("AudioProcessingThread");
            processingThread.start();
            
            // 等待用户按Enter键
            System.in.read();
            
            // 停止音频捕获
            audioCapture.stopCapture();
            
            // 等待处理线程结束
            processingThread.interrupt();
            try {
                processingThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("程序结束");
            
        } catch (LineUnavailableException e) {
            System.err.println("无法初始化音频设备: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("程序运行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理音频数据的方法
     * @param audioCapture 音频捕获对象
     */
    private static void processAudioData(RealTimeAudioCapture audioCapture) {
        int frameCount = 0;
        long startTime = System.currentTimeMillis();
        
        while (!Thread.currentThread().isInterrupted() && audioCapture.isCapturing()) {
            try {
                // 获取音频数据（阻塞方式）
                float[] audioData = audioCapture.getAudioDataBlocking();
                
                if (audioData != null) {
                    frameCount++;
                    
                    // 计算音频数据的统计信息
                    AudioStats stats = calculateAudioStats(audioData);
                    
                    // 每秒输出一次统计信息
                    if (frameCount % 10 == 0) { // 假设每100ms一帧，10帧=1秒
                        long currentTime = System.currentTimeMillis();
                        double elapsedSeconds = (currentTime - startTime) / 1000.0;
                        
                        System.out.printf("[%.1fs] 帧#%d - 样本数:%d, 音量:%.3f, 峰值:%.3f, 队列大小:%d\n",
                            elapsedSeconds, frameCount, audioData.length, 
                            stats.rms, stats.peak, audioCapture.getQueueSize());
                    }
                    
                    // 这里可以添加更多的音频处理逻辑
                    // 例如：VAD检测、音频保存、实时分析等
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("音频处理错误: " + e.getMessage());
            }
        }
        
        System.out.println("音频处理线程结束，总共处理了 " + frameCount + " 帧音频数据");
    }
    
    /**
     * 计算音频数据的统计信息
     * @param audioData 音频数据
     * @return 音频统计信息
     */
    private static AudioStats calculateAudioStats(float[] audioData) {
        float sum = 0;
        float peak = 0;
        
        for (float sample : audioData) {
            float abs = Math.abs(sample);
            sum += abs * abs;
            if (abs > peak) {
                peak = abs;
            }
        }
        
        float rms = (float) Math.sqrt(sum / audioData.length);
        
        return new AudioStats(rms, peak);
    }
    
    /**
     * 音频统计信息类
     */
    private static class AudioStats {
        final float rms;  // 均方根值（音量）
        final float peak; // 峰值
        
        AudioStats(float rms, float peak) {
            this.rms = rms;
            this.peak = peak;
        }
    }
}