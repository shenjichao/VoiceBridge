package com.hp.voice.ui.card;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * 
 * Card.java
 *
 * @author shenjichao@untech.com.cn
 *
 *         TODO 卡牌实体类
 *
 * @create 2016年8月21日 下午3:35:00
 */
public class Card {
	public int x = 0; // 横坐标
	public int y = 0; // 纵坐标
	public int width; // 宽度
	public int height; // 高度
	public Bitmap bitmap;// 图片
	public String name; // Card的名称
	public int value; // card的数值
	public boolean rear = true;// 是否是背面
	public boolean clicked = false;// 是否被点击

	/**
	 * 构造方法
	 * 
	 * @param width
	 * @param height
	 * @param bitmap
	 */
	public Card(int width, int height, Bitmap bitmap) {
		this.width = width;
		this.height = height;
		this.bitmap = bitmap;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Rect getSRC() {
		return new Rect(0, 0, width, height);
	}

	public Rect getDST() {
		return new Rect(x, y, x + width, y + height);
	}
}
