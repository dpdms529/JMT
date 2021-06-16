package org.techtown.jmt;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        viewHolder.setItem(context,item);
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
        ImageView picture_imageView;
        GradientDrawable rounding;
        LinearLayout layout;
        FirebaseStorage storage;
        StorageReference storageReference;
        String path;

        public ViewHolder(View itemView) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.comment_title);
            comment_textView = itemView.findViewById(R.id.comment_text);
            picture_imageView = itemView.findViewById(R.id.comment_img);
            layout = itemView.findViewById(R.id.background_layout);
            layout.setBackgroundResource(R.drawable.comment_border);
            storage = FirebaseStorage.getInstance("gs://android-jmt.appspot.com");
            storageReference = storage.getReference();
        }
        public void setItem(Context context, StoreComment item) {
            rounding = (GradientDrawable) context.getDrawable(R.drawable.backgroud_rounding);
            picture_imageView.setBackground(rounding);
            picture_imageView.setClipToOutline(true);
            title_textView.setText(item.getUserName());
            comment_textView.setText(item.getComment());
            path = item.getImageUrl();
            if(path != null){
                picture_imageView.setVisibility(View.VISIBLE);
                storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context)
                                .load(uri)
                                .into(picture_imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"실패",Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                picture_imageView.setVisibility(View.GONE);
            }
        }
    }
}
