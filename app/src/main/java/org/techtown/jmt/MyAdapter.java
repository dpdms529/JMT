package org.techtown.jmt;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements OnMyItemClickListener {
    ArrayList<PersonalComment> items = new ArrayList<PersonalComment>();
    ArrayList<String> paths = new ArrayList<String>();
    OnMyItemClickListener listener;
    private Context context;

    public MyAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        // if item에 image 있으면, comment_item 사용, image 없으면 comment_nopic_item 사용(gone 사용해야. invisible은 공간 차지함)
        View itemView = inflater.inflate(R.layout.comment_item, viewGroup, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        PersonalComment item = items.get(position);
        viewHolder.setItem(context,item,paths.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(PersonalComment item, String path) {
        items.add(item);
        paths.add(path);
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

    public void setOnItemClickListener(OnMyItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textView;
        TextView comment_textView;
        ImageView picture_imageView;
        FirebaseStorage storage;
        StorageReference storageReference;

        public ViewHolder(View itemView, final OnMyItemClickListener listener) {
            super(itemView);
            title_textView = itemView.findViewById(R.id.comment_title);
            comment_textView = itemView.findViewById(R.id.comment_text);
            picture_imageView = itemView.findViewById(R.id.comment_img);
            storage = FirebaseStorage.getInstance("gs://android-jmt.appspot.com");
            storageReference = storage.getReference();
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
        public void setItem(Context context, PersonalComment item, String path) {
            title_textView.setText(item.getStoreName());
            comment_textView.setText(item.getComment());
            if(path != null){
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
            }

        }

    }
}
