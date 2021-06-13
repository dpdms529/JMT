package org.techtown.jmt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements OnUserItemClickListener{
    ArrayList<UserInfo> items = new ArrayList<UserInfo>();
    OnUserItemClickListener listener;
    private Context context;

    public UserAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.my_item, viewGroup, false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        UserInfo item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(UserInfo item) {
        items.add(item);
    }

    public void setItems(ArrayList<UserInfo> items){
        this.items = items;
    }

    public UserInfo getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, UserInfo item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnUserItemClickListener listener){this.listener = listener;}

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textView;
        TextView num_of_comment;
        public ViewHolder(View itemView, final OnUserItemClickListener listener) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.name);
            num_of_comment = itemView.findViewById(R.id.num_of_comment);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(ViewHolder.this,view,position);
                    }
                }
            });
        }
        public void setItem(UserInfo item) {
            title_textView.setText(item.getUserName());
            num_of_comment.setText("총 " + String.valueOf(item.getNumOfComment()) + " 개의 맛집");
        }
    }
}
