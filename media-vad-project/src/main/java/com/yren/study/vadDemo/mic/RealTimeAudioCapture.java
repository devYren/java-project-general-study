package com.yren.study.vadDemo.mic;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 实时音频捕获类
 * 使用Java Sound API从麦克风获取音频数据
 * 
 * @author ChenYu ren
 * @date 2025/1/27
 */
public class RealTimeAudioCapture {
    // 音频格式参数
    private static final int SAMPLE_RATE = 16000;  // 采样率
    private static final int SAMPLE_SIZE_IN_BITS = 16;  // 采样位数
    private static final int CHANNELS = 1;  // 单声道
    private static final boolean SIGNED = true;  // 有符号
    private static final boolean BIG_ENDIAN = false;  // 小端序
    
    // 缓冲区大小（毫秒）
    private static final int BUFFER_SIZE_MS = 100;
    private static final int BUFFER_SIZE_BYTES = (SAMPLE_RATE * SAMPLE_SIZE_IN_BITS * CHANNELS / 8) * BUFFER_SIZE_MS / 1000;
    
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private boolean isCapturing = false;
    private Thread captureThread;
    
    // 音频数据队列，用于在不同线程间传递数据
    private BlockingQueue<float[]> audioQueue;
    
    public RealTimeAudioCapture() {
        // 初始化音频格式
        audioFormat = new AudioFormat(
            SAMPLE_RATE,
            SAMPLE_SIZE_IN_BITS,
            CHANNELS,
            SIGNED,
            BIG_ENDIAN
        );
        
        audioQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * 初始化音频捕获设备
     * @throws LineUnavailableException 如果音频设备不可用
     */
    public void initialize() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("不支持指定的音频格式");
        }
        
        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(audioFormat, BUFFER_SIZE_BYTES);
        
        System.out.println("音频捕获设备初始化成功");
        System.out.println("采样率: " + SAMPLE_RATE + " Hz");
        System.out.println("采样位数: " + SAMPLE_SIZE_IN_BITS + " bits");
        System.out.println("声道数: " + CHANNELS);
        System.out.println("缓冲区大小: " + BUFFER_SIZE_BYTES + " bytes");
    }
    
    /**
     * 开始音频捕获
     */
    public void startCapture() {
        if (isCapturing) {
            System.out.println("音频捕获已经在运行中");
            return;
        }
        
        isCapturing = true;
        targetDataLine.start();
        
        captureThread = new Thread(this::captureAudio);
        captureThread.setName("AudioCaptureThread");
        captureThread.start();
        
        System.out.println("开始音频捕获...");
    }
    
    /**
     * 停止音频捕获
     */
    public void stopCapture() {
        if (!isCapturing) {
            return;
        }
        
        isCapturing = false;
        
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
        }
        
        if (captureThread != null) {
            try {
                captureThread.join(1000); // 等待最多1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("音频捕获已停止");
    }
    
    /**
     * 音频捕获的核心方法
     */
    private void captureAudio() {
        byte[] buffer = new byte[BUFFER_SIZE_BYTES];
        
        while (isCapturing) {
            try {
                // 从麦克风读取音频数据
                int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                
                if (bytesRead > 0) {
                    // 将字节数据转换为float数组
                    float[] audioData = bytesToNormalizedFloat(buffer, bytesRead);
                    
                    // 将音频数据放入队列
                    if (!audioQueue.offer(audioData)) {
                        System.out.println("警告: 音频队列已满，丢弃数据");
                    }
                }
            } catch (Exception e) {
                System.err.println("音频捕获错误: " + e.getMessage());
                break;
            }
        }
    }
    
    /**
     * 将字节数组转换为归一化的float数组
     * @param audioBytes 音频字节数据
     * @param length 有效数据长度
     * @return 归一化的float数组
     */
    private float[] bytesToNormalizedFloat(byte[] audioBytes, int length) {
        float[] floatArray = new float[length / 2];
        
        for (int i = 0; i < floatArray.length; i++) {
            // 将两个字节组合成short（小端序）
            short sample = (short) ((audioBytes[i * 2 + 1] << 8) | (audioBytes[i * 2] & 0xFF));
            // 归一化到[-1, 1]
            floatArray[i] = sample / 32768.0f;
        }
        
        return floatArray;
    }
    
    /**
     * 获取音频数据（非阻塞）
     * @return 音频数据，如果没有数据则返回null
     */
    public float[] getAudioData() {
        return audioQueue.poll();
    }
    
    /**
     * 获取音频数据（阻塞）
     * @return 音频数据
     * @throws InterruptedException 如果线程被中断
     */
    public float[] getAudioDataBlocking() throws InterruptedException {
        return audioQueue.take();
    }
    
    /**
     * 获取队列中等待处理的音频块数量
     * @return 队列大小
     */
    public int getQueueSize() {
        return audioQueue.size();
    }
    
    /**
     * 检查是否正在捕获音频
     * @return true如果正在捕获
     */
    public boolean isCapturing() {
        return isCapturing;
    }
    
    /**
     * 获取音频格式信息
     * @return AudioFormat对象
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
    
    /**
     * 列出系统中可用的音频输入设备
     */
    public static void listAudioInputDevices() {
        System.out.println("=== 可用的音频输入设备 ===");
        
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
            
            if (targetLineInfos.length > 0) {
                System.out.println("设备: " + mixerInfo.getName());
                System.out.println("描述: " + mixerInfo.getDescription());
                System.out.println("厂商: " + mixerInfo.getVendor());
                System.out.println("版本: " + mixerInfo.getVersion());
                System.out.println("---");
            }
        }
    }
}