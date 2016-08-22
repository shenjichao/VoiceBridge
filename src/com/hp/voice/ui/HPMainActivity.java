package com.hp.voice.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.hp.voice.R;
import com.hp.voice.ui.card.CardView;
import com.hp.voice.ui.util.JsonParser;
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
 * IatDemo.java
 *
 * @author shenjichao@untech.com.cn
 *
 *         TODO
 *
 * @create 2016年8月15日 下午3:43:55
 */
public class HPMainActivity extends Activity implements OnClickListener {

	private static String TAG = HPMainActivity.class.getSimpleName();

	private Context m_ctx;

	// 语音听写对象
	private SpeechRecognizer m_speechRecg;

	// 语音听写UI
	private RecognizerDialog m_recgDialog;

	// 用HashMap存储听写结果
	private HashMap<String, String> m_recgResults = new LinkedHashMap<String, String>();

	// 引擎类型
	private String m_engineType = SpeechConstant.TYPE_CLOUD;

	private String m_result = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hp_activity_main);

		m_ctx = this;

		// 初始化Layout
		initLayout();

		// 初始化识别无UI识别对象
		m_speechRecg = SpeechRecognizer.createRecognizer(m_ctx, mInitListener);

		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		m_recgDialog = new RecognizerDialog(m_ctx, mInitListener);
	}

	/**
	 * 初始化Layout
	 */
	private void initLayout() {
		findViewById(R.id.hp_btn_start).setOnClickListener(this);
		findViewById(R.id.hp_btn_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 开始听写
		// 如何判断一次听写结束：OnResult isLast=true 或者 onError
		case R.id.hp_btn_start:
			CardView.m_cn = 2;
			CardView.m_repaint = true;
			m_recgResults.clear();
			// 参数设置
			setParam();

			// 显示听写对话框
			m_recgDialog.setListener(m_recognizerDialogListener);
			m_recgDialog.show();
			Toast.makeText(m_ctx, R.string.text_begin, Toast.LENGTH_SHORT).show();
			break;
		// 取消听写
		case R.id.hp_btn_cancel:
			m_speechRecg.cancel();
			Toast.makeText(m_ctx, "取消听写", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
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
			Log.d(TAG, "isLast >> " + isLast);
			printResult(results);
		}

		public void onError(SpeechError error) {
			Log.d(TAG, "识别回调错误 >> " + error.getPlainDescription(true));
			Toast.makeText(m_ctx, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
		}

	};

	private void printResult(RecognizerResult results) {
		// m_datas.clear();
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		m_recgResults.put(sn, text);

		StringBuffer sb = new StringBuffer();
		for (String key : m_recgResults.keySet()) {
			sb.append(m_recgResults.get(key));
		}

		if (!TextUtils.isEmpty(m_result) && m_result.equals(sb.toString())) {
			Log.d(TAG, "数据重复");
			return;
		}

		m_result = sb.toString();

		// ArrayList<Token> tokens =
		// HanziToPinyin.getInstance().get(sb.toString());
		// StringBuilder sbToken = new StringBuilder();
		// if (null != tokens && !tokens.isEmpty()) {
		// for (Token token : tokens) {
		// Log.d(TAG, token.source + " , " + token.target + " , " + token.type);
		// sbToken.append(token.target);
		// }
		// }

		if (!TextUtils.isEmpty(sb.toString())) {
			for (int i = 0; i < sb.toString().length();) {
				if (i + 2 <= sb.toString().length()) {
					String result = sb.toString().substring(i, i + 2);
					Log.d(TAG, "result >> " + result);
					i += 2;

					if (result.contains("小一") || result.contains("小姨") || result.contains("小1") || result.contains("1")) {
						Toast.makeText(m_ctx, "一", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大一") || result.contains("大衣") || result.contains("大姨")
							|| result.contains("大1")) {
						Toast.makeText(m_ctx, "壹", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小二") || result.contains("小2") || result.contains("2")) {
						Toast.makeText(m_ctx, "二", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大二") || result.contains("大2")) {
						Toast.makeText(m_ctx, "贰", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小三") || result.contains("小3") || result.contains("3")) {
						Toast.makeText(m_ctx, "三", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大三") || result.contains("大3")) {
						Toast.makeText(m_ctx, "叁", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小四") || result.contains("小事") || result.contains("小4")
							|| result.contains("4")) {
						Toast.makeText(m_ctx, "四", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大四") || result.contains("大事") || result.contains("大4")) {
						Toast.makeText(m_ctx, "肆", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小五") || result.contains("小5") || result.contains("5")) {
						Toast.makeText(m_ctx, "五", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大五") || result.contains("大5")) {
						Toast.makeText(m_ctx, "伍", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小六") || result.contains("小6") || result.contains("6")) {
						Toast.makeText(m_ctx, "六", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大六") || result.contains("大6")) {
						Toast.makeText(m_ctx, "陆", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小七") || result.contains("小7") || result.contains("7")) {
						Toast.makeText(m_ctx, "七", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大七") || result.contains("大7")) {
						Toast.makeText(m_ctx, "柒", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小八") || result.contains("小巴") || result.contains("小8")
							|| result.contains("8")) {
						Toast.makeText(m_ctx, "八", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大八") || result.contains("大巴") || result.contains("大8")) {
						Toast.makeText(m_ctx, "捌", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小九") || result.contains("小舅") || result.contains("小酒")
							|| result.contains("小9") || result.contains("9")) {
						Toast.makeText(m_ctx, "九", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大九") || result.contains("大舅") || result.contains("大酒")
							|| result.contains("大9")) {
						Toast.makeText(m_ctx, "玖", Toast.LENGTH_SHORT).show();
					} else if (result.contains("小十") || result.contains("小时") || result.contains("小事")
							|| result.contains("小10") || result.contains("10")) {
						Toast.makeText(m_ctx, "十", Toast.LENGTH_SHORT).show();
					} else if (result.contains("大十") || result.contains("大师") || result.contains("大事")
							|| result.contains("大10")) {
						Toast.makeText(m_ctx, "拾", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	/**
	 * 参数设置
	 */
	private void setParam() {
		// 清空参数
		m_speechRecg.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		m_speechRecg.setParameter(SpeechConstant.ENGINE_TYPE, m_engineType);
		// 设置返回结果格式
		m_speechRecg.setParameter(SpeechConstant.RESULT_TYPE, "json");

		// 设置语言
		m_speechRecg.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		m_speechRecg.setParameter(SpeechConstant.ACCENT, "mandarin");

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		m_speechRecg.setParameter(SpeechConstant.VAD_BOS, "4000");

		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入，自动停止录音
		m_speechRecg.setParameter(SpeechConstant.VAD_EOS, "1000");

		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		m_speechRecg.setParameter(SpeechConstant.ASR_PTT, "0");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		m_speechRecg.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		m_speechRecg.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()
				+ "/msc/iat.wav");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时释放连接
		if (null != m_speechRecg) {
			m_speechRecg.cancel();
			m_speechRecg.destroy();
		}
	}

}
