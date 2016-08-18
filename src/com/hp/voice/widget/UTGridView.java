package com.hp.voice.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 
 * @author shenjichao@untech.com.cn
 *
 */
public class UTGridView extends GridView {
	public UTGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UTGridView(Context context) {
		super(context);
	}

	public UTGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
