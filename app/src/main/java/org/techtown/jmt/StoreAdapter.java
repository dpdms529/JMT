package org.techtown.jmt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    ArrayList<StoreInfo> items = new ArrayList<StoreInfo>();
    private Context context;

    public StoreAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.my_item, viewGroup, false);
        return new ViewHolder(itemView);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textView;
        public ViewHolder(View itemView) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.name);
        }
        public void setItem(StoreInfo item) {
            title_textView.setText(item.getStoreName());
        }
    }
}
