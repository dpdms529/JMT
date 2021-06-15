package org.techtown.jmt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    Fragment frag_add_store;
    Fragment frag_my_detail;
    ImageButton add_btn;
    ImageButton share_btn;
    RecyclerView recyclerView;
    MyAdapter adapter;
    FirebaseFirestore db;
    String myId;
    String userName;
    private SharedPreferences preferences;
    String mjlist;
    Map<Integer,PersonalComment> adapterData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_list, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        frag_add_store = new AddStore();
        frag_my_detail = new MyDetail();

        recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(mContext);
        adapterData = new HashMap<>();

        db = FirebaseFirestore.getInstance();
        myId = preferences.getString("myId","noId");
        Log.d(TAG,"myId is " + myId);
        db.collection("user")
                .document(myId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();
                            if (userDoc.exists()) {
                                userName = String.valueOf(userDoc.get("name"));
                                mjlist = "<" + userName + "님의 맛집 리스트>";
                                Log.d(TAG, "사용자 정보 : " + userDoc.get("store"));
                                if (userDoc.get("store") != null) {
                                    ArrayList<DocumentReference> storeArr = (ArrayList) userDoc.get("store");
                                    int i = 0;
                                    for (DocumentReference sdr : storeArr) {
                                        int finalI = i;
                                        sdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot storeDoc = task.getResult();
                                                    if (storeDoc.exists()) {
                                                        Log.d(TAG, "가게 정보 : " + storeDoc.getData());
                                                        ArrayList<DocumentReference> commentArr = (ArrayList) storeDoc.get("comment");
                                                        for (DocumentReference cdr : commentArr) {
                                                            cdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot commentDoc = task.getResult();
                                                                        if (commentDoc.exists()) {
                                                                            Log.d(TAG, "댓글 정보 : " + commentDoc.getData());
                                                                            if (commentDoc.getString("user").equals(myId)) {
                                                                                Log.d(TAG, storeDoc.getString("name") + commentDoc.getString("photo"));
                                                                                adapterData.put(finalI,new PersonalComment(storeDoc.getString("name"), commentDoc.getString("content"), commentDoc.getString("photo")));
                                                                                if(adapterData.size() == storeArr.size()){
                                                                                    Log.d(TAG,"data size is " + adapterData.size());
                                                                                    for(int i = 0;i<adapterData.size();i++){
                                                                                        adapter.addItem(adapterData.get(i));
                                                                                        adapter.notifyDataSetChanged();
                                                                                    }
                                                                                }
//                                                                                adapter.addItem(new PersonalComment(storeDoc.getString("name"), commentDoc.getString("content"), commentDoc.getString("photo")));
//                                                                                adapter.notifyDataSetChanged();
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
                                        i++;
                                    }
                                    
                                }
                            }
                        }
                    }
                });


        recyclerView.setAdapter(adapter);

        db.collection("user").document(myId).get()
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

        adapter.setOnItemClickListener(new OnMyItemClickListener() {
            @Override
            public void onItemClick(MyAdapter.ViewHolder holder, View view, int position) {
                PersonalComment item = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putInt("position",position);
                Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                getParentFragmentManager().setFragmentResult("requestKey",bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_detail).addToBackStack(null).commit();
            }
        });

        add_btn = v.findViewById(R.id.add_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_add_store).addToBackStack(null).commit();
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
        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

}

