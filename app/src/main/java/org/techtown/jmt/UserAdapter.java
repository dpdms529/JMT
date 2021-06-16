package org.techtown.jmt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
        return new ViewHolder(context,itemView,this);
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
        private static final String TAG = "TAG";
        TextView title_textView;
        TextView num_of_comment;
        ImageButton star;
        FirebaseFirestore db;
        String myId;
        private SharedPreferences preferences;
        UserInfo item;

        public ViewHolder(Context context, View itemView, final OnUserItemClickListener listener) {
            super(itemView);

            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            myId = preferences.getString("myId","noId");

            db = FirebaseFirestore.getInstance();
            final DocumentReference[] ref = new DocumentReference[1];

            title_textView = itemView.findViewById(R.id.name);
            num_of_comment = itemView.findViewById(R.id.num_of_comment);
            star = itemView.findViewById(R.id.star);
            star.setVisibility(View.VISIBLE);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.isSelected()){
                        view.setSelected(false);
                        db.collection("user")
                                .document(item.getUserID())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot userDoc = task.getResult();
                                            if(userDoc.exists()){
                                                ref[0] = userDoc.getReference();
                                                db.collection("user")
                                                        .document(myId)
                                                        .update("favorite", FieldValue.arrayRemove(ref[0]));

                                            }
                                        }
                                    }
                                });
                    }else{
                        view.setSelected(true);
                        db.collection("user")
                                .document(item.getUserID())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot userDoc = task.getResult();
                                            if(userDoc.exists()){
                                                ref[0] = userDoc.getReference();
                                                db.collection("user")
                                                        .document(myId)
                                                        .update("favorite", FieldValue.arrayUnion(ref[0]));

                                            }
                                        }
                                    }
                                });

                    }
                }
            });
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
            starState(item);
            this.item = item;
        }

        public void starState(UserInfo item){
            Log.d(TAG,"myId is "+myId);
            db.collection("user")
                    .document(myId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot userDoc = task.getResult();
                                if(userDoc.exists()){
                                    if(userDoc.get("favorite") != null){
                                        ArrayList<DocumentReference> favoriteArr = (ArrayList)userDoc.get("favorite");
                                        for(DocumentReference fdr : favoriteArr){
                                            fdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        DocumentSnapshot favoriteDoc = task.getResult();
                                                        if(favoriteDoc.exists()){
                                                            if(favoriteDoc.getString("id").equals(item.getUserID())){
                                                                star.setSelected(true);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }

                                    }

                                }
                            }
                        }
                    });
        }
    }
}
