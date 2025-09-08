package com.yren.study.vadDemo.mic;

import com.yren.study.vadDemo.audio.AudioPreprocessor;
import com.yren.study.vadDemo.result.VadResult;
import com.yren.study.vadDemo.vad.SileroVAD;

import javax.sound.sampled.LineUnavailableException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 快速麦克风测试程序
 * 用于验证麦克风功能是否正常工作
 * 
 * @author ChenYu ren
 * @date 2025/1/27
 */
public class QuickMicTest {

    public static final String wavFilePath = "/Users/yren/Documents/studyProject/yren-java-project-general-study/media-vad-project/src/main/resources/audio/元数据.wav";
    public static final String vadModelPath = "/Users/yren/Documents/studyProject/yren-java-project-general-study/media-vad-project/src/main/resources/model/silero_vad.onnx";
    
    public static void main(String[] args) {
        System.out.println("=== Java麦克风快速测试 ===");
        
        // 1. 检查可用的音频设备
        System.out.println("\n1. 检查音频输入设备:");
        RealTimeAudioCapture.listAudioInputDevices();
        
        // 2. 测试麦克风捕获
        System.out.println("\n2. 测试麦克风捕获功能:");
        testMicrophoneCapture();
    }
    
    private static void testMicrophoneCapture() {
        RealTimeAudioCapture audioCapture = new RealTimeAudioCapture();
        
        try {
            // 初始化
            System.out.println("正在初始化音频设备...");
            audioCapture.initialize();
            System.out.println("✓ 音频设备初始化成功");
            
            // 开始捕获
            System.out.println("\n开始捕获音频数据...");
            audioCapture.startCapture();
            System.out.println("✓ 音频捕获已启动");

            //模型加载
            SileroVAD vad = new SileroVAD();
            vad.loadModel(vadModelPath);

            // 测试数据获取
            System.out.println("\n测试音频数据获取");
            long startTime = System.currentTimeMillis();

            while (true) {
                float[] audioData = audioCapture.getAudioData();

                if (audioData != null) {
                    // 2. 初始化VAD模型

                    System.out.println("VAD model loaded");

                    // 3. 执行VAD检测
                    List<VadResult> vadResults = vad.detectVoiceActivity(audioData);
                    vadResults.forEach(System.out::println);
                    System.out.println("VAD detection completed: " + vadResults.size() + " windows");
                }
                
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                }catch (Exception ignored){}
            }

        } catch (LineUnavailableException e) {
            System.err.println("❌ 音频设备不可用: " + e.getMessage());
            System.err.println("\n可能的解决方案:");
            System.err.println("1. 检查麦克风是否正确连接");
            System.err.println("2. 检查系统音频设置");
            System.err.println("3. 确认应用有麦克风访问权限");
            System.err.println("4. 重启音频服务或重新插拔麦克风");
            
        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}