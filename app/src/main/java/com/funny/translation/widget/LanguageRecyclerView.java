package com.funny.translation.widget;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import com.funny.translation.bean.LanguageBean;
import java.util.ArrayList;
import android.content.res.TypedArray;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funny.translation.R;
public class LanguageRecyclerView extends RecyclerView
{
	Context ctx;
	ArrayList<LanguageBean> list;
	short checkKind;
	
	LanguageAdapter adapter;
	public LanguageRecyclerView(Context ctx){
		this(ctx,null);
	}
	
	public LanguageRecyclerView(Context ctx,AttributeSet attr){
		super(ctx,attr);
		this.ctx=ctx;
		@SuppressLint("CustomViewStyleable") TypedArray arr=ctx.obtainStyledAttributes(attr,R.styleable.LanguageRV);
		checkKind=(short)arr.getInteger(R.styleable.LanguageRV_check_kind,0);
		arr.recycle();
	}
	
	public void initData(ArrayList<LanguageBean> list,int[] mapping){
		this.list=list;
		if(adapter == null){
			adapter=new LanguageAdapter(ctx,list,checkKind,this,mapping);
			LinearLayoutManager linearLM=new LinearLayoutManager(ctx,LinearLayoutManager.VERTICAL,false);
			setLayoutManager(linearLM);
			setAdapter(adapter);

			//以下解决滑动卡顿
			setHasFixedSize(true);
			setNestedScrollingEnabled(false);
		}else{
			adapter.list = list;
			adapter.mapping = mapping;
			adapter.notifyDataSetChanged();
		}

	}

	@Nullable
	@Override
	public LanguageAdapter getAdapter() {
		return adapter;
	}

	public void updateData(){
		adapter.notifyDataSetChanged();
	}
}
