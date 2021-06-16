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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StoreList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    private FirebaseFirestore db;
    private Spinner category_spinner;
    private Spinner do_spinner;
    private Spinner si_spinner;
    private ArrayAdapter categoryAdapter;
    private ArrayAdapter locationAdapter;
    private RecyclerView recyclerView;
    private Fragment frag_store_detail;
    private TextView toolbar_text;
    private StoreAdapter adapter;
    private ArrayList<Integer> si_array;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store_list, container, false);

        toolbar_text = getActivity().findViewById(R.id.toolbar_text);
        toolbar_text.setText("모두의 맛집");

        frag_store_detail = new StoreDetail();
        adapter = new StoreAdapter(mContext);

        recyclerView = v.findViewById(R.id.store_name_recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 지역 배열 저장
        si_array = new ArrayList<Integer>();
        si_array.add(R.array.nothing);
        si_array.add(R.array.location_si_seoul);
        si_array.add(R.array.location_si_busan);
        si_array.add(R.array.location_si_daegu);
        si_array.add(R.array.location_si_incheon);
        si_array.add(R.array.location_si_gwangju);
        si_array.add(R.array.location_si_daejeon);
        si_array.add(R.array.location_si_ulsan);
        si_array.add(R.array.location_si_sejong);
        si_array.add(R.array.location_si_gyunggi);
        si_array.add(R.array.location_si_gangwon);
        si_array.add(R.array.location_si_chungbuk);
        si_array.add(R.array.location_si_chungnam);
        si_array.add(R.array.location_si_jeonbuk);
        si_array.add(R.array.location_si_jeonnam);
        si_array.add(R.array.location_si_gyungbuk);
        si_array.add(R.array.location_si_gyungnam);
        si_array.add(R.array.location_si_jeju);

        // 스피너(카테고리) 구현
        category_spinner = (Spinner) v.findViewById(R.id.category);
        categoryAdapter = ArrayAdapter.createFromResource(mContext, R.array.categories_all, R.layout.support_simple_spinner_dropdown_item);
        category_spinner.setAdapter(categoryAdapter);

        // 스피너(도, 시) 구현
        do_spinner = (Spinner) v.findViewById(R.id.do_spinner);
        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_do, R.layout.support_simple_spinner_dropdown_item);
        do_spinner.setAdapter(locationAdapter);

        si_spinner = (Spinner) v.findViewById(R.id.si_spinner);
        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.nothing, R.layout.support_simple_spinner_dropdown_item);
        si_spinner.setAdapter(locationAdapter);

        // 카테고리별 분류에 알맞은 데이터 표시
        category_spinner.setOnItemSelectedListener(spinnerSelected);

        // 도 선택시 각각에 알맞은 시 스피너 노출, 데이터 표시
        do_spinner.setOnItemSelectedListener(doSpinnerSelected);

        // 시 선택시 각각에 알맞은 데이터 표시
        si_spinner.setOnItemSelectedListener(spinnerSelected);

        return v;
    }

    public void DBAdapter_category_do_si(StoreAdapter adapter, String category, String do_name, String si_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .whereEqualTo("do", do_name)
                .whereEqualTo("si", si_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(DBComplete);
    }

    public void DBAdapter_category_do(StoreAdapter adapter, String category, String do_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .whereEqualTo("do", do_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(DBComplete);
    }

    public void DBAdapter_do_si(StoreAdapter adapter, String do_name, String si_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("do", do_name)
                .whereEqualTo("si", si_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(DBComplete);
    }

    public void DBAdapter_do(StoreAdapter adapter, String do_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("do", do_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(DBComplete);
    }

    public void DBAdapter_category(StoreAdapter adapter, String category) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(DBComplete);
    }

    public void DBAdapter_all(StoreAdapter adapter) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(DBComplete);
    }

    private void setRecyclerView() {
        //private void setRecyclerView(StoreAdapter adapter){
        String selectedCategory = category_spinner.getSelectedItem().toString();
        String selectedDo = do_spinner.getSelectedItem().toString();
        String selectedSi = si_spinner.getSelectedItem().toString();

        if (selectedCategory.equals("모두") && selectedDo.equals("전체")) {
            // 모든 카테고리, 전체 지역
            DBAdapter_all(adapter);
        } else if (selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
            // 모든 카테고리, 세부 도, 전체 시
            DBAdapter_do(adapter, selectedDo);
        } else if (selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
            // 모든 카테고리, 세부 도, 세부 시
            DBAdapter_do_si(adapter, selectedDo, selectedSi);
        } else if (!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
            // 세부 카테고리, 전체 지역
            DBAdapter_category(adapter, selectedCategory);
        } else if (!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
            // 세부 카테고리, 세부 도, 전체 시
            DBAdapter_category_do(adapter, selectedCategory, selectedDo);
        } else if (!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
            // 세부 카테고리, 세부 도, 세부 시
            DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
        }
        recyclerView.setAdapter(adapter);
    }

    OnStoreItemClickListener onClick = new OnStoreItemClickListener() {
        @Override
        public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
            StoreInfo item = adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("store_name", item.getStoreName());
            bundle.putString("location", item.getLocation());
            Log.d(TAG, "send store_name is " + bundle.getString("store_name"));
            getParentFragmentManager().setFragmentResult("requestKey", bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
        }
    };

    AdapterView.OnItemSelectedListener spinnerSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setRecyclerView();
            adapter.setOnItemClickListener(onClick);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            setRecyclerView();
            adapter.setOnItemClickListener(onClick);
        }
    };

    private void setLocationAdapter(ArrayList<Integer> si_array, int position) {
        locationAdapter = ArrayAdapter.createFromResource(mContext, si_array.get(position), R.layout.support_simple_spinner_dropdown_item);
    }

    AdapterView.OnItemSelectedListener doSpinnerSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setRecyclerView();
            adapter.setOnItemClickListener(onClick);

            setLocationAdapter(si_array, position);
            si_spinner.setAdapter(locationAdapter);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            setRecyclerView();
            adapter.setOnItemClickListener(onClick);
        }
    };

    OnCompleteListener<QuerySnapshot> DBComplete = new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                adapter.clear();
                if (task.getResult().isEmpty()) {
                    adapter.addItem(new StoreInfo("등록된 데이터가 없습니다.", 0, ""));
                    adapter.notifyDataSetChanged();
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "가게 정보" + document.getData());
                        adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    // 프래그먼트는 context를 바로 가져올 수 없음. getActivity 또는 getContext는 종종 Null을 가져오므로 안전한 코드 다음과 같이 작성
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

}