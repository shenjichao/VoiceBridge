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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/*
 * QQ:361106306
 * by:小柒
 * 转载此程序须保留版权,未经作者允许不能用作商业用途!
 * */
public class CardView extends SurfaceView implements SurfaceHolder.Callback,
		Runnable {

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
	//按钮
	String buttonText[]=new String[2];
	//提示
	String message[]=new String[3];
	boolean hideButton=true;
	// List
	List<Card> playerList[]=new Vector[3];

	List<LinkedHashMap<Integer,Card>> playerListMap = new Vector<LinkedHashMap<Integer,Card>>();


	//地主牌
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

		//背景
		bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		cardBgBitmap= BitmapFactory.decodeResource(getResources(), R.drawable.cardbg1);


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
			options.inSampleSize = 3;//图片宽高都为原来的二分之一，即图片为原来的四分之一
			cardBitmap[count] = BitmapFactory.decodeResource(getResources(),id,options);

			card[count] = new Card(cardBitmap[count].getWidth(),cardBitmap[count].getHeight(), cardBitmap[count]);
			//设置Card的名字
			card[count].setName(name);
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
	// 玩家牌
	public void drawPlayer(int player){
		if(playerList[player]!=null&&playerList[player].size()>0)
		{
			for(Card card:playerList[player])
				drawCard(card);
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
			if(i>61)//底牌
			{
				//放置底牌
//				card[i].setLocation(screen_width/2-(3*i-155)*cardWidth/2,0);
				dizhuList.add(card[i]);
				card[i].setLocation(screen_width/2-(10-dizhuList.size())*cardWidth*1 + 1,cardHeight);

				update();
				continue;
			}
			switch ((t++)%3) {
				case 0:
					//左边玩家
					card[i].setLocation(cardWidth/2,i/3*cardHeight);
					playerList[0].add(card[i]);
					break;
				case 1:
					//我
					card[i].setLocation(screen_width/2-(10-i/3)*cardWidth*1 + 1,screen_height-cardHeight);
					card[i].rear=false;//翻开
					playerList[1].add(card[i]);
					break;
				case 2:
					//右边玩家
					card[i].setLocation(screen_width-3*cardWidth/2,i/3*cardHeight);
					playerList[2].add(card[i]);
					break;
			}
			update();
			Sleep(100);
		}

		//打开按钮
		hideButton=false;
		update();
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
				// 画牌
				for(int i=0;i<3;i++)
					drawPlayer(i);
				// 地主牌
				for(int i=0,len=dizhuList.size();i<len;i++)
					drawCard(dizhuList.get(i));
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
