package com.funny.translation.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.funny.translation.translate.CoreTranslationTask;

import java.util.ArrayList;

public class ResultItemDecoration extends RecyclerView.ItemDecoration
{
	/*
	 参考
	 https://blog.csdn.net/zxt0601/article/details/52355199
	 */
	private ArrayList<CoreTranslationTask> mTasks;
	private Paint mPaint;
	private Rect mBounds;//用于存放测量文字Rect
	
	private int mContentDivision;
	private int mContentLeftMargin;

	private Context context;
	public ResultItemDecoration(Context ctx,ArrayList<CoreTranslationTask> tasks){
		this.context=ctx;
		this.mTasks = tasks;
		mPaint = new Paint();
		mBounds = new Rect();
		mContentDivision=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
		mContentLeftMargin=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
	}

	public void setTasks(ArrayList<CoreTranslationTask> tasks){
		this.mTasks=tasks;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		super.getItemOffsets(outRect, view, parent, state);
		outRect.set(mContentLeftMargin, mContentDivision, mContentLeftMargin, 0);
	}
}

