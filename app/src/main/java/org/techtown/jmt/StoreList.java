package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    FirebaseFirestore db;
    private Spinner category_spinner;
    private ArrayAdapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.store_name_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 스피너(카테고리) 구현
        category_spinner = (Spinner)v.findViewById(R.id.category);
        arrayAdapter = ArrayAdapter.createFromResource(mContext, R.array.categories_all, R.layout.support_simple_spinner_dropdown_item);
        category_spinner.setAdapter(arrayAdapter);

        // 카테고리별 분류
        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                switch(i){
                    case 1:
                        DBonAdapter(adapter, "한식");
                        break;
                    case 2:
                        DBonAdapter(adapter, "중식");
                        break;
                    case 3:
                        DBonAdapter(adapter, "일식");
                        break;
                    case 4:
                        DBonAdapter(adapter, "아시안, 양식");
                        break;
                    case 5:
                        DBonAdapter(adapter, "분식");
                        break;
                    case 6:
                        DBonAdapter(adapter, "카페, 디저트");
                        break;
                    case 7:
                        DBonAdapter(adapter, "패스트푸드");
                        break;
                    default:
                        AllDBonAdapter(adapter);
                        break;
                }
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                AllDBonAdapter(adapter);
                recyclerView.setAdapter(adapter);
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    public void DBonAdapter(StoreAdapter adapter, String category) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void AllDBonAdapter(StoreAdapter adapter) {
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
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    // 프래그먼트는 context를 바로 가져올 수 없음. getActivity 또는 getContext는 종종 Null을 가져오므로 안전한 코드 다음과 같이 작성
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}