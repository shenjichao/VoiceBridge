package com.hp.voice.ui.card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.view.View;
import com.hp.voice.R;

/*

 * 转载此程序须保留版权,未经作者允许不能用作商业用途!
 * */
public class CardView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

	private String TAG = CardView.class.getName();

	private int PADDING_TOP = 10;
	private int PADDING_LEFT = 5;
	private int PADDING_RIGHT = 5;
	private int PADDING_BOTTOM = 10;

	public final static int CELL_NUM = 10;// 横向格子数目 要求双数 单数会有问题
	public final static int CELL_NUM_RED = 4;// 红色竖向数
	public final static int CELL_NUM_GREEN = 1;// 绿色竖向数
	public final static int CELL_NUM_BLUE = 4;// 蓝色竖向数
	public final static int CELL_NUM_YELLOW = 2;// 黄色竖向数


	private Handler m_handler;
	private SurfaceHolder m_surfaceHolder;

	// 屏幕宽度和高度
	public int m_screenWidth;
	public int m_screenHeight;

	// 卡牌宽度和高度
	public int m_cardWidth;
	public int m_cardHeight;

	// 每个格子宽高
	public int m_cellWidth;
	public int m_cellHeight;

	// 图片缩小倍数
	private int m_inSampleSize = 5;

	// 格子和牌的间隔
	private int CELL_CARD_PADDING = 4;

	// 图片资源，总共80张牌
	private Bitmap m_cardBitmap[] = new Bitmap[80];
	// 牌对象
	private Card m_card[] = new Card[80];

	//上次点击的牌位置
	private int m_old_card_xy[] = null;

	public static int m_count = 0;
	public static String m_voiceText;

	// 布局背景
	private Bitmap m_bgBitmap;

	// 画笔
	private Paint m_paint;
	// 画布
	private Canvas m_canvas;

	// 游戏线程
	private Thread m_gameThread;
	// 绘图线程
	private Thread drawThread;



	// 选手列表
	@SuppressWarnings("unchecked")
	public List<Card> playerList[] = new Vector[3];

	// 底牌
	private List<Card> m_diCards = new Vector<Card>();
	private List<Card> m_middleCards = new Vector<Card>();
	private List<Card> m_leftCards = new Vector<Card>();
	private List<Card> m_rightCards = new Vector<Card>();


	private Card[][] m_arr_diCards = new Card[CELL_NUM][CELL_NUM_YELLOW];
	private Card[][] m_arr_midCards = new Card[CELL_NUM][CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE];
	private Card[][] m_arr_leftCards = new Card[CELL_NUM][CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE];
	private Card[][] m_arr_rightCards = new Card[CELL_NUM][CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE];



	// 是否开始绘制
	private boolean m_start = false;
	// 是否重新绘制
	public static boolean m_repaint = false;

	// 判断当前是否要牌
	int[] flag = new int[3];

	// 已出牌表
	@SuppressWarnings("unchecked")
	List<Card> outList[] = new Vector[3];

	public static int m_cn = 1;



	/**
	 * 构造函数
	 * 
	 * @param context
	 * @param handler
	 */
	public CardView(Context context, Handler handler) {
		super(context);
		this.m_handler = handler;
		this.m_surfaceHolder = this.getHolder();
		this.m_surfaceHolder.addCallback(this);
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.m_surfaceHolder = this.getHolder();
		this.m_surfaceHolder.addCallback(this);
	}



	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "width >> " + width + "|" + "height:" + height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		m_start = true;
		Log.d(TAG, "开始绘制 m_start >> " + m_start);

		m_screenWidth = getWidth();
		m_screenHeight = getHeight();
		Log.d(TAG, "屏幕宽度 >> " + m_screenWidth + "|" + "屏幕高度 >> " + m_screenHeight);
		if (m_screenWidth == 1280 && m_screenHeight == 800) {
			m_inSampleSize = 3;
		} else if (m_screenWidth == 1794 && m_screenHeight == 1080) {
			m_inSampleSize = 6;
		} 

		// 初始化图片
		initBitMap();

		// // 开始游戏进程
		// m_gameThread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		// // 开始发牌
		// handCards();
		// }
		// });
		// m_gameThread.start();

		update();
		// 开始绘图进程
		drawThread = new Thread(this);
		drawThread.start();


	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// m_start = false;
		Log.d(TAG, "结束绘制 m_start >> " + m_start);
	}

	/**
	 * 初始化图片
	 */
	public void initBitMap() {
		initCardBitmap("x_");
		// initCardBitmap("d_");
		//
		// initCardBitmap("x_");
		// initCardBitmap("d_");
		//
		// initCardBitmap("x_");
		// initCardBitmap("d_");
		//
		// initCardBitmap("x_");
		// initCardBitmap("d_");

		m_cardWidth = m_card[0].width;
		m_cardHeight = m_card[0].height;
		Log.d(TAG, "卡牌宽度 >> " + m_cardWidth + "|" + "高度 >> " + m_cardHeight);

		m_cellWidth = m_cardHeight + CELL_CARD_PADDING;
		m_cellHeight = m_cardWidth + CELL_CARD_PADDING;
		Log.d(TAG, "每个格子的宽度 >> " + m_cellWidth + "|" + "高度 >> " + m_cellHeight);

		// 背景
		m_bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);

		m_paint = new Paint();
		m_count = 0;
	}

	/**
	 * 初始化卡牌图片
	 * 
	 * @param pre
	 */
	private void initCardBitmap(String pre) {
		for (int j = 1; j <= 10; j++) {
			if (m_count < 80) {
				// 根据名字找出ID
				String name = pre + j;
				ApplicationInfo appInfo = getContext().getApplicationInfo();
				int id = getResources().getIdentifier(name, "drawable", appInfo.packageName);

				// 图片宽高压缩
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = m_inSampleSize;// 图片宽高都为原来的二分之一，即图片为原来的四分之一
				m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), id, options);

				m_card[m_count] = new Card(m_cardBitmap[m_count].getWidth(), m_cardBitmap[m_count].getHeight(),
						m_cardBitmap[m_count]);
				// 设置Card的名字
				m_card[m_count].setName(name);
				// 设置Card的值
				m_card[m_count].value = j;
				m_count++;
			}
		}
	}

	/**
	 * 发牌
	 */
	private void handCards(Map<Integer, String> datas) {
		for (int m = 0; m < 3; m++) {
			playerList[m] = new Vector<Card>();
		}

		Set<Integer> keys = datas.keySet();
		if (null != keys && !keys.isEmpty()) {
			for (Integer i : keys) {
				// 庄家
				if (i >= 0 && i < 10) {
					m_middleCards.add(m_card[i]);

					m_arr_midCards[i][0] = m_card[i];
				}
				else if(i >= 10 && i < 20){
					m_middleCards.add(m_card[i]);
					m_arr_midCards[i-10][1] = m_card[i];
				}
				// 左边玩家
				else if (i >=20 && i < 30) {
					m_leftCards.add(m_card[i]);
					m_arr_leftCards[i-20][0] = m_card[i];
				}
				else if (i >= 30 && i < 40) {
					m_leftCards.add(m_card[i]);
					m_arr_leftCards[i-30][1] = m_card[i];
				}
				// 右边玩家
				else if (i >= 40 && i < 50) {
					m_rightCards.add(m_card[i]);
					m_arr_rightCards[i-40][0] = m_card[i];
				}
				else if (i >= 50 && i < 60) {
					m_rightCards.add(m_card[i]);
					m_arr_rightCards[i-50][1] = m_card[i];
				}
				// 底牌
				else if (i >= 60 && i < 70) {
					m_diCards.add(m_card[i]);
					m_arr_diCards[i-60][0] = m_card[i];
				}
				else if (i >= 70 && i < 80) {
					m_diCards.add(m_card[i]);
					m_arr_diCards[i-70][1] = m_card[i];
				}
			}
			update();
		}
	}

	/**
	 * 更新画布内容
	 */
	private void update() {
		m_repaint = true;
	}

	/**
	 * 绘图线程
	 */
	@Override
	public void run() {
		while (m_start) {
			if (m_repaint) {
				onDraw();
				m_repaint = false;
				sleep(30);
			}
		}
	}

	/**
	 * 画图
	 */
	private void onDraw() {
		synchronized (m_surfaceHolder) {
			try {
				// 加锁
				m_canvas = m_surfaceHolder.lockCanvas();
				// 画背景
				drawBackground();

				// 画背景方格
				drawBackgroundRect();

				if (m_cn == 2) {
					Log.d(TAG, "遍历...");
					show();
					sleep(30);
				}

				// 画牌
				drawPlayer();
			} catch (Exception e) {
				Log.e(TAG, "Exception >> " + e.getMessage());
			} finally {
				// 解锁
				if (m_surfaceHolder != null && m_canvas != null)
					m_surfaceHolder.unlockCanvasAndPost(m_canvas);
			}
		}
	}

	public void show() {
		// 图片宽高压缩
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = m_inSampleSize;// 图片宽高都为原来的二分之一，即图片为原来的四分之一
		Log.d(TAG, "m_voiceText >> " + m_voiceText);
		if (!TextUtils.isEmpty(m_voiceText)) {
			// 只有两个字时
			if (!m_voiceText.contains(",")) {
				if (m_voiceText.equals("d_1"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_1, options);
				else if (m_voiceText.equals("x_1"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_1, options);
				else if (m_voiceText.equals("d_2"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_2, options);
				else if (m_voiceText.equals("x_2"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_2, options);
				else if (m_voiceText.equals("d_3"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_3, options);
				else if (m_voiceText.equals("x_3"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_3, options);
				else if (m_voiceText.equals("d_4"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_4, options);
				else if (m_voiceText.equals("x_4"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_4, options);
				else if (m_voiceText.equals("d_5"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_5, options);
				else if (m_voiceText.equals("x_5"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_5, options);
				else if (m_voiceText.equals("d_6"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_6, options);
				else if (m_voiceText.equals("x_6"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_6, options);
				else if (m_voiceText.equals("d_7"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_7, options);
				else if (m_voiceText.equals("x_7"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_7, options);
				else if (m_voiceText.equals("d_8"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_8, options);
				else if (m_voiceText.equals("x_8"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_8, options);
				else if (m_voiceText.equals("d_9"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_9, options);
				else if (m_voiceText.equals("x_9"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_9, options);
				else if (m_voiceText.equals("d_10"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_10, options);
				else if (m_voiceText.equals("x_10"))
					m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_10, options);

				m_card[m_count] = new Card(m_cardBitmap[m_count].getWidth(), m_cardBitmap[m_count].getHeight(),
						m_cardBitmap[m_count]);
				// 设置Card的名字
				m_card[m_count].setName(m_voiceText);
				// 设置Card的值
				m_card[m_count].value = m_count + 1;

				Map<Integer, String> m_datas = new HashMap<Integer, String>();
				m_datas.put(m_count, m_voiceText);
				handCards(m_datas);
				m_count++;
			}
			// 当有多个字时
			else {
				String[] voices = m_voiceText.split(",");
				if (null != voices) {
					for (int i = 0; i < voices.length; i++) {
						String voice = voices[i];
						if (voice.equals("d_1"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_1,
									options);
						else if (voice.equals("x_1"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_1,
									options);
						else if (voice.equals("d_2"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_2,
									options);
						else if (voice.equals("x_2"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_2,
									options);
						else if (voice.equals("d_3"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_3,
									options);
						else if (voice.equals("x_3"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_3,
									options);
						else if (voice.equals("d_4"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_4,
									options);
						else if (voice.equals("x_4"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_4,
									options);
						else if (voice.equals("d_5"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_5,
									options);
						else if (voice.equals("x_5"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_5,
									options);
						else if (voice.equals("d_6"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_6,
									options);
						else if (voice.equals("x_6"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_6,
									options);
						else if (voice.equals("d_7"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_7,
									options);
						else if (voice.equals("x_7"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_7,
									options);
						else if (voice.equals("d_8"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_8,
									options);
						else if (voice.equals("x_8"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_8,
									options);
						else if (voice.equals("d_9"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_9,
									options);
						else if (voice.equals("x_9"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_9,
									options);
						else if (voice.equals("d_10"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.d_10,
									options);
						else if (voice.equals("x_10"))
							m_cardBitmap[m_count] = BitmapFactory.decodeResource(getResources(), R.drawable.x_10,
									options);

						m_card[m_count] = new Card(m_cardBitmap[m_count].getWidth(), m_cardBitmap[m_count].getHeight(),
								m_cardBitmap[m_count]);
						// 设置Card的名字
						m_card[m_count].setName(voice);
						// 设置Card的值
						m_card[m_count].value = m_count + 1;

						Map<Integer, String> m_datas = new HashMap<Integer, String>();
						m_datas.put(m_count, voice);
						handCards(m_datas);
						m_count++;
					}

					Log.d(TAG, "m_count >> " + m_count);
				}
			}

			m_cn = 1;
		}
	}

	/**
	 * 画背景
	 */
	private void drawBackground() {
		Rect src = new Rect(0, 0, m_bgBitmap.getWidth() * 3 / 4, 2 * m_bgBitmap.getHeight() / 3);
		Rect dst = new Rect(0, 0, m_screenWidth, m_screenHeight);
		m_canvas.drawBitmap(m_bgBitmap, src, dst, null);
	}

	/**
	 * 画背景格
	 */
	private void drawBackgroundRect() {
		drawBackgroundRectLeft();
		drawBackgroundRectCenter();
		drawBackgroundRectRight();
		drawBackgroundRectTop();
	}

	/**
	 * 画左边
	 */
	private void drawBackgroundRectLeft() {
		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.RED);// 画红色矩形
		Rect dst = new Rect(PADDING_LEFT, PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0),
				PADDING_LEFT + m_cellWidth * CELL_NUM_RED, PADDING_TOP + m_screenHeight / 2
						+ (int) (m_cellHeight * CELL_NUM / 2.0));
		m_canvas.drawRect(dst, m_paint);

		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.GREEN);// 画绿色矩形
		dst = new Rect(PADDING_LEFT + m_cellWidth * CELL_NUM_RED, PADDING_TOP + m_screenHeight / 2
				- (int) (m_cellHeight * CELL_NUM / 2.0), PADDING_LEFT + m_cellWidth * CELL_NUM_RED + m_cellWidth
				* CELL_NUM_GREEN, PADDING_TOP + m_screenHeight / 2 + (int) (m_cellHeight * CELL_NUM / 2.0));
		m_canvas.drawRect(dst, m_paint);

		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.BLUE);// 画蓝色矩形
		dst = new Rect(PADDING_LEFT + m_cellWidth * CELL_NUM_RED + m_cellWidth * CELL_NUM_GREEN, PADDING_TOP
				+ m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0), PADDING_LEFT + m_cellWidth * CELL_NUM_RED
				+ m_cellWidth * CELL_NUM_GREEN + m_cellWidth * CELL_NUM_BLUE, PADDING_TOP + m_screenHeight / 2
				+ (int) (m_cellHeight * CELL_NUM / 2.0));
		m_canvas.drawRect(dst, m_paint);

		// 画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		m_paint.setColor(Color.BLACK);

		// 竖线
		for (int i = 0; i < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE; i++) {
			m_canvas.drawLine(PADDING_LEFT + i * m_cellWidth, PADDING_TOP + m_screenHeight / 2
					- (int) (m_cellHeight * CELL_NUM / 2.0), PADDING_LEFT + i * m_cellWidth, PADDING_TOP
					+ m_screenHeight / 2 + (int) (m_cellHeight * CELL_NUM / 2.0), m_paint);
		}

		// 横线
		for (int i = 0; i < CELL_NUM; i++) {
			m_canvas.drawLine(PADDING_LEFT, PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0)
					+ i * m_cellHeight, PADDING_LEFT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellWidth,
					PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + i * m_cellHeight,
					m_paint);
		}
	}

	/**
	 * 画中间
	 */
	private void drawBackgroundRectCenter() {
		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.RED);// 画红色矩形
		Rect dst = new Rect(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight
				- (PADDING_BOTTOM + (CELL_NUM_RED) * m_cellHeight), m_screenWidth / 2
				+ (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight - PADDING_BOTTOM);
		m_canvas.drawRect(dst, m_paint);

		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.GREEN);// 画绿色矩形
		dst = new Rect(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight
				- (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN) * m_cellHeight), m_screenWidth / 2
				+ (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight - (PADDING_BOTTOM + m_cellHeight * CELL_NUM_RED));
		m_canvas.drawRect(dst, m_paint);

		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.BLUE);// 画蓝色矩形
		dst = new Rect(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight
				- (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellHeight), m_screenWidth / 2
				+ (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight
				- (PADDING_BOTTOM + m_cellHeight * (CELL_NUM_RED + CELL_NUM_GREEN)));
		m_canvas.drawRect(dst, m_paint);

		// 画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		m_paint.setColor(Color.BLACK);

		// 竖线
		for (int i = 0; i < CELL_NUM; i++) {
			m_canvas.drawLine(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0) + i * m_cellWidth,
					m_screenHeight - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellHeight),
					m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0) + i * m_cellWidth, m_screenHeight
							- PADDING_BOTTOM, m_paint);
		}

		// 横线
		for (int i = 0; i < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE; i++) {
			m_canvas.drawLine(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight
					- (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellHeight) + i
					* m_cellHeight, m_screenWidth / 2 + (int) (m_cellWidth * CELL_NUM / 2.0), m_screenHeight
					- (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellHeight) + i
					* m_cellHeight, m_paint);
		}
	}

	/**
	 * 画右边
	 */
	private void drawBackgroundRectRight() {
		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.RED);// 画红色矩形
		Rect dst = new Rect(m_screenWidth - (PADDING_RIGHT + CELL_NUM_RED * m_cellWidth), PADDING_TOP + m_screenHeight
				/ 2 - (int) (m_cellHeight * CELL_NUM / 2.0), m_screenWidth - PADDING_RIGHT, PADDING_TOP
				+ m_screenHeight / 2 + (int) (m_cellHeight * CELL_NUM / 2.0));
		m_canvas.drawRect(dst, m_paint);

		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.GREEN);// 画绿色矩形
		dst = new Rect(m_screenWidth - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN) * m_cellWidth), PADDING_TOP
				+ m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0), m_screenWidth
				- (PADDING_RIGHT + CELL_NUM_RED * m_cellWidth), PADDING_TOP + m_screenHeight / 2
				+ (int) (m_cellHeight * CELL_NUM / 2.0));
		m_canvas.drawRect(dst, m_paint);

		// 画蓝色矩形
		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.BLUE);
		dst = new Rect(m_screenWidth - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellWidth),
				PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0), m_screenWidth
						- (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN) * m_cellWidth), PADDING_TOP + m_screenHeight
						/ 2 + (int) (m_cellHeight * CELL_NUM / 2.0));
		m_canvas.drawRect(dst, m_paint);

		// 画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		m_paint.setColor(Color.BLACK);

		// 竖线
		for (int i = 0; i < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE; i++) {
			m_canvas.drawLine(
					m_screenWidth - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellWidth) + i
							* m_cellWidth, PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0),
					m_screenWidth - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellWidth) + i
							* m_cellWidth, PADDING_TOP + m_screenHeight / 2 + (int) (m_cellHeight * CELL_NUM / 2.0),
					m_paint);
		}

		// 横线
		for (int i = 0; i < CELL_NUM; i++) {
			m_canvas.drawLine(m_screenWidth
					- (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * m_cellWidth), PADDING_TOP
					+ m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + i * m_cellHeight, m_screenWidth
					- PADDING_RIGHT, PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + i
					* m_cellHeight, m_paint);
		}
	}

	/**
	 * 画顶部
	 */
	private void drawBackgroundRectTop() {
		m_paint.setStyle(Style.FILL);// 实心矩形框
		m_paint.setColor(Color.YELLOW);// 画红色矩形
		Rect dst = new Rect(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0), PADDING_TOP, m_screenWidth / 2
				+ (int) (m_cellWidth * CELL_NUM / 2.0), PADDING_TOP + m_cellHeight * 2);
		m_canvas.drawRect(dst, m_paint);

		// 画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		m_paint.setColor(Color.BLACK);

		// 竖线
		for (int i = 0; i < CELL_NUM; i++) {
			m_canvas.drawLine(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0) + i * m_cellWidth, PADDING_TOP,
					m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0) + i * m_cellWidth, PADDING_TOP
							+ m_cellHeight * CELL_NUM_YELLOW, m_paint);
		}

		// 横线
		for (int i = 0; i < CELL_NUM_YELLOW; i++) {
			m_canvas.drawLine(m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0), PADDING_TOP + m_cellHeight * i,
					m_screenWidth / 2 + (int) (m_cellWidth * CELL_NUM / 2.0), PADDING_TOP + m_cellHeight * i, m_paint);
		}
	}

	/**
	 * 画牌
	 */
	private void drawPlayer() {
		drawLeftPlayer();
		drawCenterPlayer();
		drawRightPlayer();
		drawTopCard();
	}

	/**
	 * 画左边的牌
	 */
	private void drawLeftPlayer() {
//		for (int i = 0, len = m_leftCards.size(); i < len; i++) {
//			int x = 0;
//			int y = 0;
//			if (i < CELL_NUM) {
//				x = PADDING_LEFT + CELL_CARD_PADDING / 2;
//				y = PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + CELL_CARD_PADDING / 2
//						+ i * (m_cardHeight + CELL_CARD_PADDING);
//			} else if (i < CELL_NUM * 2) {
//				x = PADDING_LEFT + CELL_CARD_PADDING * 3 / 2 + m_cardWidth;
//				y = PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + CELL_CARD_PADDING / 2
//						+ (i - CELL_NUM) * (m_cardHeight + CELL_CARD_PADDING);
//			}
//
//			m_leftCards.get(i).setLocation(x, y);
//			drawCard(m_leftCards.get(i));
//		}


		for(int i = 0 ; i < CELL_NUM ; i++){
			for( int j = 0 ; j < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE ; j++){

				if(m_arr_leftCards[i][j] == null){
					continue;
				}
				int x = 0;
				int y = 0;

				x = PADDING_LEFT + CELL_CARD_PADDING / 2 + (m_cardWidth + CELL_CARD_PADDING) * j;
				y = PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + CELL_CARD_PADDING / 2
						+ i * (m_cardHeight + CELL_CARD_PADDING);


				m_arr_leftCards[i][j].setLocation(x, y);
				drawCard(m_arr_leftCards[i][j]);

			}


		}
	}

	/**
	 * 画中间的牌
	 */
	private void drawCenterPlayer() {
//		for (int i = 0, len = m_middleCards.size(); i < len; i++) {
//			int x = 0;
//			int y = 0;
//			if (i < CELL_NUM) {
//				x = m_screenWidth / 2 - (5 - i) * (m_cardWidth + CELL_CARD_PADDING) * 1 + CELL_CARD_PADDING / 2;
//				y = m_screenHeight - (PADDING_BOTTOM + m_cellHeight);
//			} else if (i < CELL_NUM * 2) {
//				x = m_screenWidth / 2 - ((5 - (i - CELL_NUM)) * (m_cardWidth + CELL_CARD_PADDING)) * 1
//						+ CELL_CARD_PADDING / 2;
//				y = m_screenHeight - (PADDING_BOTTOM + 2 * m_cardHeight + CELL_CARD_PADDING * 3 / 2);
//			} else if (i < 22) {
//				x = m_screenWidth / 2 - ((5 - (i - CELL_NUM * 2)) * (m_cardWidth + CELL_CARD_PADDING)) * 1
//						+ CELL_CARD_PADDING / 2;
//				y = m_screenHeight - (PADDING_BOTTOM + 3 * m_cardHeight + CELL_CARD_PADDING * 4 / 2);
//			}
//
//			m_middleCards.get(i).setLocation(x, y);
//			drawCard(m_middleCards.get(i));
//		}

		for(int i = 0 ; i < CELL_NUM ; i++) {
			for (int j = 0; j < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE; j++) {

				if(m_arr_midCards[i][j] == null){
					continue;
				}
				int x = 0;
				int y = 0;

				x = m_screenWidth / 2 - (5 - i) * (m_cardWidth + CELL_CARD_PADDING) * 1 + CELL_CARD_PADDING / 2;
				y = m_screenHeight - (PADDING_BOTTOM - CELL_CARD_PADDING/2  + (m_cellHeight ) * (j+1) );


				m_arr_midCards[i][j].setLocation(x, y);
				drawCard(m_arr_midCards[i][j]);
			}
		}
	}

	/**
	 * 画右边的牌
	 */
	private void drawRightPlayer() {
//		for (int i = 0, len = m_rightCards.size(); i < len; i++) {
//			int x = 0;
//			int y = 0;
//			if (i < CELL_NUM) {
//				x = m_screenWidth - (m_cardWidth + CELL_CARD_PADDING / 2 + PADDING_RIGHT);
//				y = PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + CELL_CARD_PADDING / 2
//						+ i * (m_cardHeight + CELL_CARD_PADDING);
//			} else if (i < CELL_NUM * 2) {
//				x = m_screenWidth - (m_cardWidth + CELL_CARD_PADDING * 3 / 2 + m_cardWidth + PADDING_RIGHT);
//				y = PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + CELL_CARD_PADDING / 2
//						+ (i - CELL_NUM) * (m_cardHeight + CELL_CARD_PADDING);
//			}
//
//			m_rightCards.get(i).setLocation(x, y);
//			drawCard(m_rightCards.get(i));
//		}


		for(int i = 0 ; i < CELL_NUM ; i++) {
			for (int j = 0; j < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE; j++) {

				if(m_arr_rightCards[i][j] == null){
					continue;
				}
				int x = 0;
				int y = 0;

				x = m_screenWidth - (m_cardWidth + CELL_CARD_PADDING / 2 + PADDING_RIGHT +(m_cardWidth + CELL_CARD_PADDING) * j);
				y = PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0) + CELL_CARD_PADDING / 2
						+ i * (m_cardHeight + CELL_CARD_PADDING);


				m_arr_rightCards[i][j].setLocation(x, y);
				drawCard(m_arr_rightCards[i][j]);
			}
		}
	}

	/**
	 * 画底牌
	 */
	private void drawTopCard() {
//		for (int i = 0, len = m_diCards.size(); i < len; i++) {
//			int x;
//			int y;
//			if (i < 10) {
//				x = m_screenWidth / 2 - (5 - i) * (m_cardWidth + CELL_CARD_PADDING) * 1 + CELL_CARD_PADDING / 2;
//				y = PADDING_TOP + CELL_CARD_PADDING / 2;
//				m_diCards.get(i).setLocation(x, y);
//				drawCard(m_diCards.get(i));
//			} else if (i < 20) {
//				x = m_screenWidth / 2 - ((5 - (i - 10)) * (m_cardWidth + CELL_CARD_PADDING)) * 1 + CELL_CARD_PADDING
//						/ 2;
//				y = PADDING_TOP + m_cardHeight + CELL_CARD_PADDING + CELL_CARD_PADDING / 2;
//				m_diCards.get(i).setLocation(x, y);
//				drawCard(m_diCards.get(i));
//			}
//		}


		for(int i = 0 ; i < CELL_NUM ; i++) {
			for (int j = 0; j < CELL_NUM_YELLOW; j++) {

				if(m_arr_diCards[i][j] == null){
					continue;
				}
				int x = 0;
				int y = 0;

				x = m_screenWidth / 2 - (5 - i) * (m_cardWidth + CELL_CARD_PADDING) * 1 + CELL_CARD_PADDING / 2;
				y = PADDING_TOP + CELL_CARD_PADDING / 2 + (m_cardHeight + CELL_CARD_PADDING) * j;


				m_arr_diCards[i][j].setLocation(x, y);
				drawCard(m_arr_diCards[i][j]);

			}
		}
	}

	/**
	 * 画牌
	 * 
	 * @param card
	 */
	private void drawCard(Card card) {
		m_canvas.drawBitmap(card.bitmap, card.getSRC(), card.getDST(), null);
	}

	/**
	 * 休眠
	 * 
	 * @param i
	 */
	private void sleep(long i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			Log.e(TAG, "InterruptedException >> " + e.getMessage());
		}
	}

	public Rect getLeftRect(){
		return new Rect(
				PADDING_LEFT,
				PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0),
				PADDING_LEFT + m_cellWidth * ( CELL_NUM_RED + CELL_NUM_GREEN +CELL_NUM_BLUE ),
				PADDING_TOP + m_screenHeight / 2 + (int) (m_cellHeight * CELL_NUM / 2.0));
	}

	public Point getLeftPoint(){
		return new Point(
				PADDING_LEFT,
				PADDING_TOP + m_screenHeight / 2 - (int) (m_cellHeight * CELL_NUM / 2.0));
	}

	public Rect getRightRect(){
		return new Rect(
				m_screenWidth - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN +CELL_NUM_BLUE ) * m_cellWidth),
				PADDING_TOP + m_screenHeight/ 2 - (int) (m_cellHeight * CELL_NUM / 2.0),
				m_screenWidth - PADDING_RIGHT,
				PADDING_TOP+ m_screenHeight / 2 + (int) (m_cellHeight * CELL_NUM / 2.0));
	}


	public Point getRightPoint(){
		return new Point(
				m_screenWidth - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN +CELL_NUM_BLUE ) * m_cellWidth),
				PADDING_TOP + m_screenHeight/ 2 - (int) (m_cellHeight * CELL_NUM / 2.0));
	}

	public Rect getCenterRect(){
		return  new Rect(
				m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0),
				m_screenHeight - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN +CELL_NUM_BLUE ) * m_cellHeight),
				m_screenWidth / 2 + (int) (m_cellWidth * CELL_NUM / 2.0),
				m_screenHeight - PADDING_BOTTOM);
	}


	public Point getCenterPoint(){
		return new Point(
				m_screenWidth / 2 - (int) (m_cellWidth * CELL_NUM / 2.0),
				m_screenHeight - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN +CELL_NUM_BLUE ) * m_cellHeight));
	}





	@Override
	public boolean onTouchEvent(MotionEvent event){
     /*
      * 重载获得x,y把星星或者小图片会知道指定的位置
      * bmp-》创建canvas
      * 屏幕上应该只有一个星星
      */

		EventAction eventAction = new EventAction(this,event);
		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				if(m_old_card_xy == null){
					//第一次点击
					int[] tmp= eventAction.getCard();
					if(tmp[0] != 0){
						//判断点击的位置 是否有牌
						Card[][] ordCard = null;
						if(tmp[0] == 1){
							ordCard = m_arr_leftCards;
						}else if(tmp[0] == 2){
							ordCard = m_arr_rightCards;
						}else {
							ordCard = m_arr_midCards;
						}
						//判断点击的位置 是否有牌
						if(ordCard[tmp[1]][tmp[2]] != null) {
							m_old_card_xy = tmp;
						}
					}
				}else {
					//第二次点击
					int[] newCard= eventAction.getCard();
					if(newCard[0] != 0){
						//交换
						swapCard(m_old_card_xy,newCard);

						m_old_card_xy = null;
						update();

					}else {
						//没有点击方格里
						m_old_card_xy = null;
					}
				}

				break;
		}


		return true;

	}

	private void swapCard(int[] oldArr,int[] newArr){

		Card[][] ordCard = null;
		Card[][] newCard = null;

		if(oldArr[0] == 1){
			ordCard = m_arr_leftCards;
		}else if(oldArr[0] == 2){
			ordCard = m_arr_rightCards;
		}else {
			ordCard = m_arr_midCards;
		}

		if(newArr[0] == 1){
			newCard = m_arr_leftCards;
		}else if(newArr[0] == 2){
			newCard = m_arr_rightCards;
		}else {
			newCard = m_arr_midCards;
		}

		Card tmp = ordCard[oldArr[1]][oldArr[2]];
		ordCard[oldArr[1]][oldArr[2]] = newCard[newArr[1]][newArr[2]];
		newCard[newArr[1]][newArr[2]] = tmp;
	}

}