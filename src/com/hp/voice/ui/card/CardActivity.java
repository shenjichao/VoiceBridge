package com.hp.voice.ui.card;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class CardActivity extends Activity {
	/*
	 * 转载此程序须保留版权,未经作者允许不能用作商业用途!
	 */
	CardView myView;
	String messString;
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				messString = msg.getData().getString("data");
				showDialog();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		myView = new CardView(this, handler);
		setContentView(myView);
	}

	public void showDialog() {
		new AlertDialog.Builder(this).setMessage(messString)
				.setPositiveButton("重新开始游戏", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						reGame();
					}
				}).setTitle("By:小柒,QQ:361106306").create().show();
	}

	// 重新开始游戏
	public void reGame() {
		myView = new CardView(this, handler);
		setContentView(myView);
	}

}
