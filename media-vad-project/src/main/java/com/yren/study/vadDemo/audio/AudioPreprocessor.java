package com.yren.study.vadDemo.audio;

/**
 * @author ChenYu ren
 * @date 2025/9/8
 */

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import java.io.ByteArrayOutputStream;

public class AudioPreprocessor {
    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int TARGET_CHANNELS = 1;

    public float[] loadAndPreprocessWav(String wavFilePath) throws Exception {
        // 使用JavaCV读取音频文件
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(wavFilePath);
        grabber.start();

        // 设置目标参数
        grabber.setSampleRate(TARGET_SAMPLE_RATE);
        grabber.setAudioChannels(TARGET_CHANNELS);

        // 读取所有音频帧
        Frame frame;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while ((frame = grabber.grab()) != null) {
            if (frame.samples != null) {
                // 获取音频数据
                java.nio.ShortBuffer buffer = (java.nio.ShortBuffer) frame.samples[0];
                short[] audioData = new short[buffer.remaining()];
                buffer.get(audioData);

                // 转换为字节数组
                for (short sample : audioData) {
                    baos.write(sample & 0xFF);
                    baos.write((sample >> 8) & 0xFF);
                }
            }
        }

        grabber.stop();

        // 转换为float数组并归一化
        byte[] audioBytes = baos.toByteArray();
        return bytesToNormalizedFloat(audioBytes);
    }

    public float[] bytesToNormalizedFloat(byte[] audioBytes) {
        float[] floatArray = new float[audioBytes.length / 2];

        for (int i = 0; i < floatArray.length; i++) {
            // 将两个字节组合成short
            short sample = (short) ((audioBytes[i * 2 + 1] << 8) | (audioBytes[i * 2] & 0xFF));
            // 归一化到[-1, 1]
            floatArray[i] = sample / 32768.0f;
        }

        return floatArray;
    }
}
