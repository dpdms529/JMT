package org.techtown.jmt;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> implements OnStoreItemClickListener {
    ArrayList<StoreInfo> items = new ArrayList<StoreInfo>();
    OnStoreItemClickListener listener;
    private Context context;

    public StoreAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.my_item, viewGroup, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        StoreInfo item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(StoreInfo item) {
        items.add(item);
    }

    public void setItems(ArrayList<StoreInfo> items){
        this.items = items;
    }

    public StoreInfo getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, StoreInfo item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnStoreItemClickListener listener){
        this.listener = listener;
    }

    public void clear() { items.clear(); }

    @Override
    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textView;
        TextView lover_textView;

        public ViewHolder(View itemView, final OnStoreItemClickListener listener) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.name);
            lover_textView = itemView.findViewById(R.id.num_of_comment);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(StoreAdapter.ViewHolder.this,view,position);
                    }
                }
            });
        }
        public void setItem(StoreInfo item) {
            title_textView.setText(item.getStoreName());
            if(item.getLover() == 0) {
                lover_textView.setText("");
            } else {
                lover_textView.setText(String.valueOf(item.getLover()) + "명의 유저가 좋아합니다");
            }
        }
    }
}
