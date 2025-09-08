package com.yren.study.vadDemo.vad;

import java.util.*;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.yren.study.vadDemo.result.VadResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ChenYu ren
 * @date 2025/9/8
 */


public class SileroVAD {
    private OrtEnvironment env;
    private OrtSession session;
    private static final int WINDOW_SIZE_SAMPLES = 512;  // 32ms at 16kHz
    private static final float DEFAULT_THRESHOLD = 0.3f;  // 降低阈值以获得更敏感的检测
    
    // VAD状态变量 (2x1x128的状态tensor)
    private float[][][] state = new float[2][1][128];

    public void loadModel(String modelPath) throws OrtException {
        env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        session = env.createSession(modelPath, options);
    }

    public List<VadResult> detectVoiceActivity(float[] audioData) throws OrtException {
        List<VadResult> results = new ArrayList<>();

        // 按窗口大小处理音频
        for (int i = 0; i < audioData.length; i += WINDOW_SIZE_SAMPLES) {
            int endIdx = Math.min(i + WINDOW_SIZE_SAMPLES, audioData.length);
            float[] window = Arrays.copyOfRange(audioData, i, endIdx);

            // 如果窗口不足，用零填充
            if (window.length < WINDOW_SIZE_SAMPLES) {
                float[] paddedWindow = new float[WINDOW_SIZE_SAMPLES];
                System.arraycopy(window, 0, paddedWindow, 0, window.length);
                window = paddedWindow;
            }

            // 运行VAD推理
            float vadScore = runInference(window);

            // 计算时间戳（秒）
            float startTime = i / 16000.0f;
            float endTime = endIdx / 16000.0f;

            results.add(new VadResult(startTime, endTime, vadScore, vadScore > DEFAULT_THRESHOLD));
        }

        return results;
    }

    /**
     * 推理核心代码
     * @param audioWindow
     * @return
     * @throws OrtException
     */
    private float runInference(float[] audioWindow) throws OrtException {
        // 将一维数组重塑为二维数组
        float[][] input2D = new float[1][audioWindow.length];
        System.arraycopy(audioWindow, 0, input2D[0], 0, audioWindow.length);

        // 创建音频输入tensor
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, input2D);
        
        // 创建采样率输入tensor (16000 Hz)
        long[][] srInput = {{16000L}};
        OnnxTensor srTensor = OnnxTensor.createTensor(env, srInput);
        
        // 创建状态tensor
        OnnxTensor stateTensor = OnnxTensor.createTensor(env, state);

        // 准备输入映射
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input", inputTensor);
        inputs.put("sr", srTensor);
        inputs.put("state", stateTensor);

        // 运行推理
        try (OrtSession.Result result = session.run(inputs)) {
            // 获取输出
            float[][] output = (float[][]) result.get(0).getValue();
            
            // 更新状态 (如果模型返回新的状态)
            if (result.size() > 1) {
                state = (float[][][]) result.get(1).getValue();
            }
            
            return output[0][0]; // VAD概率值
        } finally {
            inputTensor.close();
            srTensor.close();
            stateTensor.close();
        }
    }

    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
