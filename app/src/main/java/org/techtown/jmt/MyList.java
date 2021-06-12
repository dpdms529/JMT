package org.techtown.jmt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MyList extends Fragment {
    private static final String TAG = "TAG";
    private Context mComtext;
    Fragment frag_add_store;
    ImageButton add_btn;
    ImageButton share_btn;
    RecyclerView recyclerView;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_list, container, false);

        frag_add_store = new AddStore();

        recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        MyAdapter adapter = new MyAdapter(mComtext);

        db = FirebaseFirestore.getInstance();
        UserApiClient.getInstance().me((user, error) -> {
                    if (error != null) {
                        Log.e(TAG, "사용자 정보 요청 실패", error);
                    } else if (user != null) {
                        db.collection("user")
                                .document(String.valueOf(user.getId()))
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                              if(task.isSuccessful()){
                                  DocumentSnapshot userDoc = task.getResult();
                                 if(userDoc.exists()){
                                     Log.d(TAG,"사용자 정보 : " + userDoc.get("store"));
                                     ArrayList storeArr = (ArrayList)userDoc.get("store");
                                     for(int i = 0;i<storeArr.size();i++){
                                         DocumentReference sdr = (DocumentReference)storeArr.get(i);
                                         sdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                             @Override
                                             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                 if(task.isSuccessful()){
                                                     DocumentSnapshot storeDoc = task.getResult();
                                                     if(storeDoc.exists()){
                                                         Log.d(TAG,"가게 정보 : " + storeDoc.getData());
                                                         ArrayList commentArr = (ArrayList)storeDoc.get("comment");
                                                         for(int j = 0;j<commentArr.size();j++){
                                                             DocumentReference cdr = (DocumentReference)commentArr.get(j);
                                                             cdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                     if(task.isSuccessful()){
                                                                         DocumentSnapshot commentDoc = task.getResult();
                                                                         if(commentDoc.exists()){
                                                                             Log.d(TAG,"댓글 정보 : " + commentDoc.getData());
                                                                             if((Long)commentDoc.get("user") == user.getId()){
                                                                                 adapter.addItem(new PersonalComment(storeDoc.getString("name"),commentDoc.getString("content")));
                                                                                 adapter.notifyDataSetChanged();
                                                                             }
                                                                         }
                                                                     }
                                                                 }
                                                             });
                                                         }
                                                     }
                                                 }
                                             }
                                         });
                                     }
                                 }
                              }
                          }
                      });
            }
            return null;
        });
        recyclerView.setAdapter(adapter);

        add_btn = v.findViewById(R.id.add_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_add_store).commit();
            }
        });

        share_btn = v.findViewById(R.id.share_btn);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "list for share should be here";

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing = Intent.createChooser(Sharing_intent, "공유하기");
                startActivity(Sharing);
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mComtext = context;
    }
}

