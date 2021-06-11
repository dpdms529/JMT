package org.techtown.jmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    ArrayList<PersonalComment> items = new ArrayList<PersonalComment>();
    private Context context;

    public MyAdapter(ArrayList<PersonalComment> items, Context context){
        this.items = items;
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
        PersonalComment item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(PersonalComment item) {
        items.add(item);
    }

    public void setItems(ArrayList<PersonalComment> items){
        this.items = items;
    }

    public PersonalComment getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, PersonalComment item) {
        items.set(position, item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textView;
        TextView comment_textView;
//        ImageView picture_imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.comment_title);
            comment_textView = itemView.findViewById(R.id.comment_text);
//            picture_imageView = itemView.findViewById(R.id.comment_img);
        }
        public void setItem(PersonalComment item) {
            title_textView.setText(item.getStoreName());
            comment_textView.setText(item.getComment());
//            picture_imageView.setImageBitmap(); // https://art-coding3.tistory.com/38 참고
        }
    }
}
