package com.hp.voice.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.voice.R;
import com.hp.voice.ui.util.JsonParser;
import com.hp.voice.widget.UTGridView;
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

	private TextView m_tvContent;

	private UTGridView m_gv;

	private List<Map<String, Object>> m_datas = new ArrayList<Map<String, Object>>();

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

		m_tvContent = (TextView) findViewById(R.id.hp_tv_content);
		m_gv = (UTGridView) findViewById(R.id.hp_gv);

		// m_datas.clear();
		// // 1. 小一
		// Map<String, Object> x1Map = new HashMap<String, Object>();
		// x1Map.put("name", R.drawable.xy);
		// m_datas.add(x1Map);
		// // 2. 大一
		// Map<String, Object> d1Map = new HashMap<String, Object>();
		// d1Map.put("name", R.drawable.dy);
		// m_datas.add(d1Map);
		// // 3. 小二
		// Map<String, Object> x2Map = new HashMap<String, Object>();
		// x2Map.put("name", R.drawable.xe);
		// m_datas.add(x2Map);
		// // 4. 大二
		// Map<String, Object> d2Map = new HashMap<String, Object>();
		// d2Map.put("name", R.drawable.de);
		// m_datas.add(d2Map);
		// // 5. 小三
		// Map<String, Object> x3Map = new HashMap<String, Object>();
		// x3Map.put("name", R.drawable.xs);
		// m_datas.add(x3Map);
		// // 6. 大三
		// Map<String, Object> d3Map = new HashMap<String, Object>();
		// d3Map.put("name", R.drawable.ds);
		// m_datas.add(d3Map);
		// // 7. 小四
		// Map<String, Object> x4Map = new HashMap<String, Object>();
		// x4Map.put("name", R.drawable.xs);
		// m_datas.add(x4Map);
		// // 8. 大四
		// Map<String, Object> d4Map = new HashMap<String, Object>();
		// d4Map.put("name", R.drawable.ds);
		// m_datas.add(d4Map);
		// // 9. 小五
		// Map<String, Object> x5Map = new HashMap<String, Object>();
		// x5Map.put("name", R.drawable.xw);
		// m_datas.add(x5Map);
		// // 10. 大五
		// Map<String, Object> d5Map = new HashMap<String, Object>();
		// d5Map.put("name", R.drawable.dw);
		// m_datas.add(d5Map);
		// // 11. 小六
		// Map<String, Object> x6Map = new HashMap<String, Object>();
		// x6Map.put("name", R.drawable.xl);
		// m_datas.add(x6Map);
		//
		// SimpleAdapter simpleAdapter = new SimpleAdapter(m_ctx, m_datas,
		// R.layout.hp_view_gridview,
		// new String[] { "name" }, new int[] { R.id.hp_iv_gridview_icon });
		// m_gv.setAdapter(simpleAdapter);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 开始听写
		// 如何判断一次听写结束：OnResult isLast=true 或者 onError
		case R.id.hp_btn_start:
			m_tvContent.setText("");// 清空显示内容
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

		m_tvContent.setText(sb.toString());
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

		if (m_datas.size() > 80) {
			m_datas.clear();
		}

		if (!TextUtils.isEmpty(sb.toString())) {
			for (int i = 0; i < sb.toString().length();) {
				if (i + 2 <= sb.toString().length()) {
					String result = sb.toString().substring(i, i + 2);
					Log.d(TAG, "result >> " + result);
					i += 2;

					if (result.contains("小一") || result.contains("小姨") || result.contains("小1") || result.contains("1")) {
						Map<String, Object> x1Map = new HashMap<String, Object>();
						x1Map.put("name", R.drawable.xy);
						m_datas.add(x1Map);
					} else if (result.contains("大一") || result.contains("大衣") || result.contains("大姨")
							|| result.contains("大1")) {
						Map<String, Object> d1Map = new HashMap<String, Object>();
						d1Map.put("name", R.drawable.dy);
						m_datas.add(d1Map);
					} else if (result.contains("小二") || result.contains("小2") || result.contains("2")) {
						Map<String, Object> x2Map = new HashMap<String, Object>();
						x2Map.put("name", R.drawable.xe);
						m_datas.add(x2Map);
					} else if (result.contains("大二") || result.contains("大2")) {
						Map<String, Object> d2Map = new HashMap<String, Object>();
						d2Map.put("name", R.drawable.de);
						m_datas.add(d2Map);
					} else if (result.contains("小三") || result.contains("小3") || result.contains("3")) {
						Map<String, Object> x3Map = new HashMap<String, Object>();
						x3Map.put("name", R.drawable.xs);
						m_datas.add(x3Map);
					} else if (result.contains("大三") || result.contains("大3")) {
						Map<String, Object> d3Map = new HashMap<String, Object>();
						d3Map.put("name", R.drawable.ds);
						m_datas.add(d3Map);
					} else if (result.contains("小四") || result.contains("小事")|| result.contains("小4") || result.contains("4")) {
						Map<String, Object> x4Map = new HashMap<String, Object>();
						x4Map.put("name", R.drawable.xsi);
						m_datas.add(x4Map);
					} else if (result.contains("大四") || result.contains("大事") || result.contains("大4")) {
						Map<String, Object> d4Map = new HashMap<String, Object>();
						d4Map.put("name", R.drawable.dsi);
						m_datas.add(d4Map);
					} else if (result.contains("小五") || result.contains("小5") || result.contains("5")) {
						Map<String, Object> x5Map = new HashMap<String, Object>();
						x5Map.put("name", R.drawable.xw);
						m_datas.add(x5Map);
					} else if (result.contains("大五") || result.contains("大5")) {
						Map<String, Object> d5Map = new HashMap<String, Object>();
						d5Map.put("name", R.drawable.dw);
						m_datas.add(d5Map);
					} else if (result.contains("小六") || result.contains("小6") || result.contains("6")) {
						Map<String, Object> x6Map = new HashMap<String, Object>();
						x6Map.put("name", R.drawable.xl);
						m_datas.add(x6Map);
					} else if (result.contains("大六") || result.contains("大6")) {
						Map<String, Object> d6Map = new HashMap<String, Object>();
						d6Map.put("name", R.drawable.dl);
						m_datas.add(d6Map);
					} else if (result.contains("小七") || result.contains("小7") || result.contains("7")) {
						Map<String, Object> x7Map = new HashMap<String, Object>();
						x7Map.put("name", R.drawable.xq);
						m_datas.add(x7Map);
					} else if (result.contains("大七") || result.contains("大7")) {
						Map<String, Object> d7Map = new HashMap<String, Object>();
						d7Map.put("name", R.drawable.dq);
						m_datas.add(d7Map);
					} else if (result.contains("小八") || result.contains("小巴") || result.contains("小8")
							|| result.contains("8")) {
						Map<String, Object> x8Map = new HashMap<String, Object>();
						x8Map.put("name", R.drawable.xb);
						m_datas.add(x8Map);
					} else if (result.contains("大八") || result.contains("大巴") || result.contains("大8")) {
						Map<String, Object> d8Map = new HashMap<String, Object>();
						d8Map.put("name", R.drawable.db);
						m_datas.add(d8Map);
					} else if (result.contains("小九") || result.contains("小舅") || result.contains("小酒")
							|| result.contains("小9") || result.contains("9")) {
						Map<String, Object> x9Map = new HashMap<String, Object>();
						x9Map.put("name", R.drawable.xj);
						m_datas.add(x9Map);
					} else if (result.contains("大九") || result.contains("大舅") || result.contains("大酒")
							|| result.contains("大9")) {
						Map<String, Object> d9Map = new HashMap<String, Object>();
						d9Map.put("name", R.drawable.dj);
						m_datas.add(d9Map);
					} else if (result.contains("小十") || result.contains("小时") || result.contains("小事")
							|| result.contains("小10") || result.contains("10")) {
						Map<String, Object> x10Map = new HashMap<String, Object>();
						x10Map.put("name", R.drawable.xshi);
						m_datas.add(x10Map);
					} else if (result.contains("大十") || result.contains("大师") || result.contains("大事")
							|| result.contains("大10")) {
						Map<String, Object> d10Map = new HashMap<String, Object>();
						d10Map.put("name", R.drawable.dshi);
						m_datas.add(d10Map);
					}
				}
			}

			if (null != m_datas && !m_datas.isEmpty()) {
				SimpleAdapter simpleAdapter = new SimpleAdapter(m_ctx, m_datas, R.layout.hp_view_gridview,
						new String[] { "name" }, new int[] { R.id.hp_iv_gridview_icon });
				m_gv.setAdapter(simpleAdapter);
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
