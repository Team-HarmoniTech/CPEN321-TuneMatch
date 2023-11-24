package com.cpen321.tunematch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class ListAdapter<T> extends BaseAdapter {
    protected final ReduxStore model = ReduxStore.getInstance();
    protected final Context context;
    protected final Activity parentView;
    protected List<T> itemList;
    protected final WebSocketService webSocketService;

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