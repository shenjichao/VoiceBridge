package com.hp.voice;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hp.voice.util.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

/**
 * 
 * HPMainActivity.java
 *
 * @author shenjichao@untech.com.cn
 *
 *         TODO 桥牌语音识别
 *
 * @create 2016年8月15日 上午10:01:21
 */
public class HPMainActivity extends Activity {

	private final static String TAG = HPMainActivity.class.getName();

	private Context m_ctx;

	// 语音听写对象
	private SpeechRecognizer m_speechRecg;

	// 语音听写UI
	private RecognizerDialog m_recgDialog;

	// 用HashMap存储听写结果
	private HashMap<String, String> m_iatResults = new LinkedHashMap<String, String>();

	private SharedPreferences m_sharedPreferences;

	/**
	 * 初始化监听回调
	 */
	private InitListener m_initListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "语音识别 code >> " + code);
			if (code != ErrorCode.SUCCESS) {
				Toast.makeText(m_ctx, "初始化失败，错误码 >> " + code, Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener m_recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, "听写UI监听器 isLast >> " + isLast);
			printResult(results);
		}

		public void onError(SpeechError error) {
			Toast.makeText(m_ctx, "回调错误 >> " + error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hp_activity_main);

		m_ctx = this;

		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		m_speechRecg = SpeechRecognizer.createRecognizer(m_ctx, m_initListener);

		// 初始化听写Dialog
		m_recgDialog = new RecognizerDialog(m_ctx, m_initListener);

		m_sharedPreferences = getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
	}

	/**
	 * 开始说话
	 * 
	 * @param v
	 */
	public void startVoice(View v) {
		Log.d(TAG, "开始说话");
		m_iatResults.clear();

		// 设置参数
		setParam();

		// 显示听写对话框
		m_recgDialog.setListener(m_recognizerDialogListener);
		m_recgDialog.show();
		Toast.makeText(m_ctx, "请开始说话…", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 设置参数
	 */
	public void setParam() {
		// 清空参数
		m_speechRecg.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		m_speechRecg.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		m_speechRecg.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		m_speechRecg.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		m_speechRecg.setParameter(SpeechConstant.ACCENT,
				m_sharedPreferences.getString("iat_language_preference", "mandarin"));
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		m_speechRecg.setParameter(SpeechConstant.VAD_BOS,
				m_sharedPreferences.getString("iat_vadbos_preference", "4000"));
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		m_speechRecg.setParameter(SpeechConstant.VAD_EOS,
				m_sharedPreferences.getString("iat_vadeos_preference", "1000"));
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		m_speechRecg.setParameter(SpeechConstant.ASR_PTT, m_sharedPreferences.getString("iat_punc_preference", "1"));
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		m_speechRecg.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		m_speechRecg.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()
				+ "/msc/iat.wav");
	}

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		m_iatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : m_iatResults.keySet()) {
			resultBuffer.append(m_iatResults.get(key));
		}

		Log.d(TAG, "语音识别的内容 >> "+resultBuffer.toString());
	}

}
