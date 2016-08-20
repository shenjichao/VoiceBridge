package com.hp.voice.ui.card;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.hp.voice.R;
import com.iflytek.thirdparty.P;

import java.util.*;

/*

 * 转载此程序须保留版权,未经作者允许不能用作商业用途!
 * */
public class CardView extends SurfaceView implements SurfaceHolder.Callback,
		Runnable {

	public final static int inSampleSize = 4; //图片缩小倍数

	public final static int PADDING_TOP = 10;
	public final static int PADDING_LEFT = 5;
	public final static int PADDING_RIGHT = 5;
	public final static int PADDING_BOTTOM = 10;

	public final static int CELL_NUM = 10;//横向格子数目   要求双数  单数会有问题
	public final static int CELL_NUM_RED = 4;//红色竖向数
	public final static int CELL_NUM_GREEN = 1;//绿色竖向数
	public final static int CELL_NUM_BLUE = 1;//蓝色竖向数
	public final static int CELL_NUM_YELLOW = 2;//黄色竖向数

	public final static int CELL_CARD_PADDING = 4;//格子和牌的间隔

	//每个 格子 宽高
	int cell_height;
	int cell_width;

	SurfaceHolder surfaceHolder;
	Canvas canvas;
	Boolean repaint=false;
	Boolean start;
	Thread gameThread,drawThread;
	// 判断当前是否要牌
	int []flag=new int[3];
	// 屏幕宽度和高度
	int screen_height;
	int screen_width;
	// 图片资源
	Bitmap cardBitmap[] = new Bitmap[80];
	Bitmap bgBitmap;    //背景
	Bitmap cardBgBitmap;//图片背面

	// 基本参数
	int cardWidth, cardHeight;

	// 牌对象
	Card card[] = new Card[80];

	//画笔
	private Paint paint;
	//按钮
	String buttonText[]=new String[2];
	//提示
	String message[]=new String[3];
	boolean hideButton=true;
	// List
	List<Card> playerList[]=new Vector[3];


	LinkedHashMap<Integer,Vector<Card>> playerCardMap1 = new LinkedHashMap<Integer,Vector<Card>>();
	LinkedHashMap<Integer,Vector<Card>> playerCardMap2 = new LinkedHashMap<Integer,Vector<Card>>();
	LinkedHashMap<Integer,Vector<Card>> playerCardMap3 = new LinkedHashMap<Integer,Vector<Card>>();

	//底牌
	List<Card> dizhuList=new Vector<Card>();
	//谁是地主
	int dizhuFlag=-1;
	//轮流
	int turn=-1;
	//已出牌表
	List<Card> outList[]=new Vector[3];
	Handler handler;
	// 构造函数
	public CardView(Context context, Handler handler) {
		super(context);
		this.handler=handler;
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
	}
	int count=0;

	// 初始化图片,参数
	public void InitBitMap() {
		for(int i=0;i<3;i++)
			flag[i]=0;
		dizhuFlag=-1;
		turn=-1;

		initCardBitmap("x_");
		initCardBitmap("d_");

		initCardBitmap("x_");
		initCardBitmap("d_");

		initCardBitmap("x_");
		initCardBitmap("d_");

		initCardBitmap("x_");
		initCardBitmap("d_");


		cardWidth=card[0].width;
		cardHeight=card[0].height;

		cell_height = cardWidth+CELL_CARD_PADDING;
		cell_width = cardHeight+CELL_CARD_PADDING;


		//背景
		bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		cardBgBitmap= BitmapFactory.decodeResource(getResources(), R.drawable.cardbg1);

		paint = new Paint();

	}

	private void initCardBitmap(String pre) {
		for (int j = 1; j <= 10; j++) {
			//根据名字找出ID
			String name = pre + j;
			ApplicationInfo appInfo = getContext().getApplicationInfo();
			int id = getResources().getIdentifier(name, "drawable",
					appInfo.packageName);

			//图片宽高压缩
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = inSampleSize;//图片宽高都为原来的二分之一，即图片为原来的四分之一
			cardBitmap[count] = BitmapFactory.decodeResource(getResources(),id,options);

			card[count] = new Card(cardBitmap[count].getWidth(),cardBitmap[count].getHeight(), cardBitmap[count]);
			//设置Card的名字
			card[count].setName(name);
			//设置Card的值
			card[count].value = j;
			count++;
		}
	}
	// 画背景
	public void drawBackground() {
		Rect src = new Rect(0, 0, bgBitmap.getWidth()*3 / 4,
				2*bgBitmap.getHeight() / 3);
		Rect dst = new Rect(0, 0, screen_width, screen_height);
		canvas.drawBitmap(bgBitmap, src, dst, null);
	}

	//画背景格
	public void drawBackgroundRect() {
		drawBackgroundRectLeft();

		drawBackgroundRectCenter();
		drawBackgroundRectRight();

		drawBackgroundRectTop();

	}

	public void drawBackgroundRectTop() {
		//画红色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.YELLOW);
		Rect dst = new Rect(
				screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0),
				PADDING_TOP  ,
				screen_width / 2 + (int)(cell_width * CELL_NUM / 2.0),
				PADDING_TOP + cell_height * 2
		);
		canvas.drawRect(dst, paint);



		//画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		paint.setColor(Color.BLACK);

		// 竖线
		for(int i = 0 ; i < CELL_NUM ; i++){
			canvas.drawLine(
					screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0) + i * cell_width,
					PADDING_TOP ,
					screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0) + i * cell_width,
					PADDING_TOP + cell_height * CELL_NUM_YELLOW,
					paint
			);
		}

		//横线
		for(int i = 0 ; i <  CELL_NUM_YELLOW; i++){
			canvas.drawLine(
					screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0),
					PADDING_TOP + cell_height * i,
					screen_width / 2 + (int)(cell_width * CELL_NUM / 2.0),
					PADDING_TOP + cell_height * i,
					paint
			);
		}
	}

	public void drawBackgroundRectLeft() {

		//画红色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.RED);
		Rect dst = new Rect(
				PADDING_LEFT,
				PADDING_TOP,
				PADDING_LEFT + cell_width * CELL_NUM_RED,
				PADDING_TOP + CELL_NUM * cell_height
		);
		canvas.drawRect(dst, paint);

		//画绿色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.GREEN);
		dst = new Rect(
				PADDING_LEFT + cell_width * CELL_NUM_RED,
				PADDING_TOP,
				PADDING_LEFT + cell_width * CELL_NUM_RED + cell_width * CELL_NUM_GREEN,
				PADDING_TOP + CELL_NUM * cell_height
		);
		canvas.drawRect(dst, paint);


		//画蓝色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.BLUE);
		dst = new Rect(
				PADDING_LEFT + cell_width * CELL_NUM_RED + cell_width * CELL_NUM_GREEN,
				PADDING_TOP,
				PADDING_LEFT + cell_width * CELL_NUM_RED + cell_width * CELL_NUM_GREEN + cell_width * CELL_NUM_BLUE,
				PADDING_TOP + CELL_NUM * cell_height
		);
		canvas.drawRect(dst, paint);


		//画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		paint.setColor(Color.BLACK);

		// 竖线
		for(int i = 0 ; i < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE ; i++){
			canvas.drawLine(
					PADDING_LEFT + i * cell_width,
					PADDING_TOP ,
					PADDING_LEFT + i * cell_width,
					PADDING_TOP + CELL_NUM * cell_height,
					paint
			);
		}

		//横线
		for(int i = 0 ; i < CELL_NUM ; i++){
			canvas.drawLine(
					PADDING_LEFT ,
					PADDING_TOP + i * cell_height,
					PADDING_LEFT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * cell_width,
					PADDING_TOP + i * cell_height,
					paint
			);
		}

	}

	public void drawBackgroundRectCenter() {
		//画红色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.RED);
		Rect dst = new Rect(
				screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0),
				screen_height - (PADDING_BOTTOM + (CELL_NUM_RED) * cell_height) ,
				screen_width / 2 + (int)(cell_width * CELL_NUM / 2.0),
				screen_height - PADDING_BOTTOM
		);
		canvas.drawRect(dst, paint);

		//画绿色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.GREEN);
		dst = new Rect(
				screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0),
				screen_height - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN)  * cell_height),
				screen_width / 2 + (int)(cell_width * CELL_NUM / 2.0),
				screen_height - (PADDING_BOTTOM + cell_height * CELL_NUM_RED)
		);
		canvas.drawRect(dst, paint);


		//画蓝色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.BLUE);
		dst = new Rect(
				screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0),
				screen_height - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE)  * cell_height),
				screen_width / 2 + (int)(cell_width * CELL_NUM / 2.0),
				screen_height - (PADDING_BOTTOM + cell_height * (CELL_NUM_RED + CELL_NUM_GREEN))
		);
		canvas.drawRect(dst, paint);


		//画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		paint.setColor(Color.BLACK);

		// 竖线
		for(int i = 0 ; i < CELL_NUM ; i++){
			canvas.drawLine(
					screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0) + i * cell_width,
					screen_height - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE)  * cell_height) ,
					screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0) + i * cell_width,
					screen_height -PADDING_BOTTOM,
					paint
			);
		}

		//横线
		for(int i = 0 ; i <  CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE; i++){
			canvas.drawLine(
					screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0)  ,
					screen_height - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE)  * cell_height) + i * cell_height,
					screen_width / 2 + (int)(cell_width * CELL_NUM / 2.0),
					screen_height - (PADDING_BOTTOM + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE)  * cell_height) + i * cell_height,
					paint
			);
		}
	}

	public void drawBackgroundRectRight() {
		//画红色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.RED);
		Rect dst = new Rect(
				screen_width - (PADDING_RIGHT + CELL_NUM_RED * cell_width) ,
				PADDING_TOP,
				screen_width - PADDING_RIGHT ,
				PADDING_TOP + CELL_NUM * cell_height
		);
		canvas.drawRect(dst, paint);

		//画绿色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.GREEN);
		dst = new Rect(
				screen_width - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN) * cell_width),
				PADDING_TOP,
				screen_width - (PADDING_RIGHT + CELL_NUM_RED * cell_width),
				PADDING_TOP + CELL_NUM * cell_height
		);
		canvas.drawRect(dst, paint);


		//画蓝色矩形
		paint.setStyle(Style.FILL);//实心矩形框
		paint.setColor(Color.BLUE);
		dst = new Rect(
				screen_width - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * cell_width),
				PADDING_TOP,
				screen_width - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN) * cell_width),
				PADDING_TOP + CELL_NUM * cell_height
		);
		canvas.drawRect(dst, paint);


		//画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。
		paint.setColor(Color.BLACK);

		// 竖线
		for(int i = 0 ; i < CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE ; i++){
			canvas.drawLine(
					screen_width - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * cell_width) + i * cell_width ,
					PADDING_TOP ,
					screen_width - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * cell_width) + i * cell_width ,
					PADDING_TOP + CELL_NUM * cell_height,
					paint
			);
		}

		//横线
		for(int i = 0 ; i < CELL_NUM ; i++){
			canvas.drawLine(
					screen_width - (PADDING_RIGHT + (CELL_NUM_RED + CELL_NUM_GREEN + CELL_NUM_BLUE) * cell_width) ,
					PADDING_TOP + i * cell_height,
					screen_width - PADDING_RIGHT,
					PADDING_TOP + i * cell_height,
					paint
			);
		}
	}

	// 玩家牌
	public void drawPlayer(){
		drawLeftPlayer();
		drawRightPlayer();
		drawCenterPlayer();
	}

	private void drawCenterPlayer() {
		//画中间的
		int i = 0  ;
		for(Map.Entry<Integer,Vector<Card>> entry : playerCardMap2.entrySet()){
			int j = 0;
			for(Card card : entry.getValue()){
				card.setLocation(
						screen_width / 2 - (int)(cell_width * CELL_NUM / 2.0) + i*(cardWidth + CELL_CARD_PADDING) + CELL_CARD_PADDING/2,
						screen_height-((cardHeight + CELL_CARD_PADDING )*(j+1) + PADDING_BOTTOM - CELL_CARD_PADDING/2));
				drawCard(card);
				j ++;
			}
			i++;
		}
	}

	private void drawRightPlayer() {
		//画右边的
		int i = 0  ;
		for(Map.Entry<Integer,Vector<Card>> entry : playerCardMap3.entrySet()){
			int j = 0;
			for(Card card : entry.getValue()){
				card.setLocation(
						screen_width-(cardWidth + CELL_CARD_PADDING/2 + j*(cardWidth + CELL_CARD_PADDING) + PADDING_RIGHT) ,
						i*(cardHeight+CELL_CARD_PADDING)+CELL_CARD_PADDING/2+PADDING_TOP)
				;
				drawCard(card);
				j ++;
			}
			i++;
		}
	}

	private void drawLeftPlayer() {
		//画左边的
		int i = 0  ;
		for(Map.Entry<Integer,Vector<Card>> entry : playerCardMap1.entrySet()){
			int j = 0;
			for(Card card : entry.getValue()){
				card.setLocation(
						PADDING_LEFT+CELL_CARD_PADDING/2+ j*(cardWidth+CELL_CARD_PADDING),
						PADDING_TOP +CELL_CARD_PADDING/2 + i*(cardHeight+CELL_CARD_PADDING )
				);
				drawCard(card);
				j ++;
			}
			i++;
		}
	}



	private void drawTopCard() {
		//画头部的
		for(int i=0,len=dizhuList.size();i<len;i++) {
			if(i<10){
				dizhuList.get(i).setLocation(screen_width/2-(5-i)*(cardWidth + CELL_CARD_PADDING) *1 + CELL_CARD_PADDING/2,PADDING_TOP +CELL_CARD_PADDING/2);
				drawCard(dizhuList.get(i));
			}else if(i<20){
				dizhuList.get(i).setLocation(screen_width/2-((5-(i-10))*(cardWidth + CELL_CARD_PADDING))*1 + CELL_CARD_PADDING/2,PADDING_TOP + cardHeight + CELL_CARD_PADDING +CELL_CARD_PADDING/2);
				drawCard(dizhuList.get(i));
			}else {

			}

		}
	}

	//画牌
	public void drawCard(Card card){
		Bitmap tempbitBitmap;

		tempbitBitmap=card.bitmap;

		canvas.drawBitmap(tempbitBitmap, card.getSRC(),
				card.getDST(), null);
	}
	//发牌
	public void handCards(){
		//开始发牌
		int t=0;
		for(int i=0;i<3;i++){
			playerList[i]=new Vector<Card>();
		}
		for(int i=0;i<80;i++)
		{
			if(i>60)//底牌
			{
				//放置底牌
//				card[i].setLocation(screen_width/2-(3*i-155)*cardWidth/2,0);
				dizhuList.add(card[i]);


				update();
				continue;
			}
			switch ((t++)%3) {
				case 0:

					//我

					card[i].rear=false;//翻开
					playerList[1].add(card[i]);

					addToPlayMap2(card[i]);
					break;
				case 1:
					//右边玩家

					playerList[2].add(card[i]);

					addToPlayMap3(card[i]);
					break;
				case 2:
					//左边玩家

					playerList[0].add(card[i]);
					addToPlayMap1(card[i]);
					break;
			}
			update();
			Sleep(100);
		}

		//打开按钮
		hideButton=false;
		update();
	}

	private void addToPlayMap1(Card card) {
		if(playerCardMap1.containsKey(card.value)){
            playerCardMap1.get(card.value).add(card);
        }else {
            Vector<Card> vector = new Vector<>();
            vector.add(card);
            playerCardMap1.put(card.value,vector);
        }
	}

	private void addToPlayMap2(Card card) {
		if(playerCardMap2.containsKey(card.value)){
			playerCardMap2.get(card.value).add(card);
		}else {
			Vector<Card> vector = new Vector<>();
			vector.add(card);
			playerCardMap2.put(card.value,vector);
		}
	}

	private void addToPlayMap3(Card card) {
		if(playerCardMap3.containsKey(card.value)){
			playerCardMap3.get(card.value).add(card);
		}else {
			Vector<Card> vector = new Vector<>();
			vector.add(card);
			playerCardMap3.put(card.value,vector);
		}
	}

	//sleep();
	public void Sleep(long i){
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//下一个玩家
	public void nextTurn(){
		turn=(turn+1)%3;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		start=true;
		screen_height = getHeight();
		screen_width = getWidth();


		// 初始化
		InitBitMap();

		// 开始游戏进程
		gameThread=new Thread(new Runnable() {
			@Override
			public void run() {
				//开始发牌
				handCards();
			}
		});
		gameThread.start();
		// 开始绘图进程
		drawThread=new Thread(this);
		drawThread.start();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		start=false;
	}
	//主要绘图线程
	@Override
	public void run() {
		while (start) {
			if(repaint)
			{
				onDraw();
				repaint=false;
				Sleep(33);
			}
		}
	}
	//画图函数
	public void onDraw(){
		//枷锁
		synchronized (surfaceHolder) {
			try {
				canvas = surfaceHolder.lockCanvas();
				// 画背景
				drawBackground();
				// 画背景方格
				drawBackgroundRect();
				// 画牌
				drawPlayer();
				// 底牌

				drawTopCard();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (canvas != null)
					surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	//更新函数
	public void update(){
		repaint=true;
	}
	//触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//只接受按下事件
		if(event.getAction()!= MotionEvent.ACTION_UP)
			return true;
		//点选牌
		EventAction eventAction=new EventAction(this,event);
		Card card=eventAction.getCard();
		if(card!=null)
		{
			Log.i("mylog", card.name);
			if(card.clicked)
				card.y+=card.height/3;
			else
				card.y-=card.height/3;
			card.clicked=!card.clicked;
			update();//重绘
		}
		//按钮事件
		eventAction.getButton();
		return true;
	}
	//计时器
	public void setTimer(int t,int flag)
	{
		while(t-->0){
			Sleep(1000);
			message[flag]=t+"";
			update();
		}
		message[flag]="";
	}


}