package org.techtown.jmt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class OtherList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    RecyclerView recyclerView;
    FirebaseFirestore db;

    private String userId;

    ImageButton share_btn;
    MyAdapter adapter;
    Fragment frag_store_detail;
    String mjlist;
    String userName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_other_list, container, false);

        frag_store_detail = new StoreDetail();

        recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(mContext);
        db = FirebaseFirestore.getInstance();

        getParentFragmentManager().setFragmentResultListener("otherList", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                userId = bundle.getString("user_id");
                Log.d(TAG, "userId is " + userId);
                db.collection("user")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot userDoc = task.getResult();
                                if(userDoc.exists()){
                                    userName = String.valueOf(userDoc.get("name"));
                                    mjlist = "<" + userName + "님의 맛집 리스트>";
                                    ArrayList storeArr = (ArrayList)userDoc.get("store");
                                    for(int i = 0;i<storeArr.size();i++){
                                        DocumentReference sdr = (DocumentReference)storeArr.get(i);
                                        sdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot storeDoc = task.getResult();
                                                    if(storeDoc.exists()){
                                                        Log.d(TAG, "store info is " + storeDoc.getData());
                                                        ArrayList commentArr = (ArrayList)storeDoc.get("comment");
                                                        for(int j = 0;j<commentArr.size();j++){
                                                            DocumentReference cdr = (DocumentReference)commentArr.get(j);
                                                            cdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if(task.isSuccessful()){
                                                                        DocumentSnapshot commentDoc = task.getResult();
                                                                        if(commentDoc.exists()){
                                                                            if(String.valueOf(commentDoc.get("user")).equals(userId)){
                                                                                Log.d(TAG, "comment info is " + commentDoc.getData());
                                                                                adapter.addItem(new PersonalComment(storeDoc.getString("name"),commentDoc.getString("content"),commentDoc.getString("photo")) );
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
                recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(new OnMyItemClickListener() {
                    @Override
                    public void onItemClick(MyAdapter.ViewHolder holder, View view, int position) {
                        PersonalComment item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name",item.getStoreName());
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout,frag_store_detail).addToBackStack(null).commit();
                    }
                });

                db.collection("user").document(userId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    ArrayList<DocumentReference> storeArr = (ArrayList<DocumentReference>) document.get("store");
                                    for(DocumentReference storeDoc : storeArr){
                                        storeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot document = task.getResult();
                                                    mjlist = mjlist + "\n- " + String.valueOf(document.get("name"));
                                                    mjlist = mjlist + "\n  (" + String.valueOf(document.get("location")) + ")";
                                                    Log.d(TAG,"mjList: " + mjlist);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
            }
        });

        share_btn = v.findViewById(R.id.share_btn);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, mjlist);

                Intent Sharing = Intent.createChooser(Sharing_intent, "공유하기");
                startActivity(Sharing);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}


