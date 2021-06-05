package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.sdk.user.UserApiClient;

import java.util.ArrayList;

public class StoreList extends Fragment {
    private static final String TAG = "TAG";
    private Context mComtext;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.store_name_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        StoreAdapter adapter = new StoreAdapter(mComtext);

        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        // 이 부분은 추후 데이터 베이스 연동으로 수정해야 함
//        adapter.addItem(new StoreInfo("히유히유"));
//        adapter.addItem(new StoreInfo("덕천식당"));
//        adapter.addItem(new StoreInfo("청춘 튀겨"));
//        adapter.addItem(new StoreInfo("히유히유"));
//        adapter.addItem(new StoreInfo("덕천식당"));
//        adapter.addItem(new StoreInfo("청춘 튀겨"));
//        adapter.addItem(new StoreInfo("히유히유"));
//        adapter.addItem(new StoreInfo("덕천식당"));
//        adapter.addItem(new StoreInfo("청춘 튀겨"));

        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return v;
    }

    // 프래그먼트는 context를 바로 가져올 수 없음. getActivity 또는 getContext는 종종 Null을 가져오므로 안전한 코드 다음과 같이 작성
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mComtext = context;
    }
}