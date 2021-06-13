package org.techtown.jmt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    ArrayList<StoreComment> items = new ArrayList<StoreComment>();
    private Context context;

    public CommentAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        // if item에 image 있으면, comment_item 사용, image 없으면 comment_nopic_item 사용(gone 사용해야. invisible은 공간 차지함)
        View itemView = inflater.inflate(R.layout.comment_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        StoreComment item = items.get(position);
        viewHolder.setItem(item);
    }

    public int getItemCount() {
        return items.size();
    }

    public void addItem(StoreComment item) {
        items.add(item);
    }

    public void setItems(ArrayList<StoreComment> items){
        this.items = items;
    }

    public StoreComment getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, StoreComment item) {
        items.set(position, item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textView;
        TextView comment_textView;

        public ViewHolder(View itemView) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.comment_title);
            comment_textView = itemView.findViewById(R.id.comment_text);
        }
        public void setItem(StoreComment item) {
            title_textView.setText(item.getUserName());
            comment_textView.setText(item.getComment());
        }
    }
}
