package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserList extends Fragment {    // 어댑터가 storeAdapter랑 유사함. 중복 제거할 방법 찾기!
    private static final String TAG = "TAG";
    private Context mContext;
    FirebaseFirestore db;
    private Spinner category_spinner;
    private ArrayAdapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.user_name_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 스피너(카테고리) 구현
        category_spinner = (Spinner)v.findViewById(R.id.category);
        arrayAdapter = ArrayAdapter.createFromResource(mContext, R.array.categories_all, R.layout.support_simple_spinner_dropdown_item);
        category_spinner.setAdapter(arrayAdapter);

        UserAdapter adapter = new UserAdapter(mContext);

        db = FirebaseFirestore.getInstance();
        db.collection("user")
                .orderBy("storeNum", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"회원 정보" + document.getData());
                                adapter.addItem(new UserInfo(document.getString("name"), (Long) document.get("storeNum")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}