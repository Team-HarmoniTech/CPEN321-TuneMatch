package com.cpen321.tunematch;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class ListAdapter<T> extends BaseAdapter {
    protected final ReduxStore model = ReduxStore.getInstance();
    protected final Context context;
    protected final Activity parentView;
    protected final WebSocketService webSocketService;
    protected List<T> itemList;

    // ChatGPT Usage: Partial
    public ListAdapter(Context context, Activity parentView, List<T> itemList, WebSocketService webSocketService) {
        this.context = context;
        this.parentView = parentView;
        this.itemList = itemList;
        this.webSocketService = webSocketService;
    }

    // ChatGPT Usage: Yes
    @Override
    public int getCount() {
        return itemList.size();
    }

    // ChatGPT Usage: Yes
    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    // ChatGPT Usage: Yes
    @Override
    public long getItemId(int position) {
        return position;
    }

    // ChatGPT Usage: No
    @Override
    public abstract View getView(int i, View view, ViewGroup viewGroup);

    // ChatGPT Usage: No
    public void updateData(List<T> data) {
        this.itemList = data;
        notifyDataSetChanged();
    }
}