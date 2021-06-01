package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StoreList extends Fragment {
    private Context mComtext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.store_name_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        StoreAdapter adapter = new StoreAdapter();

        // 이 부분은 추후 데이터 베이스 연동으로 수정해야 함
        adapter.addItem(new StoreInfo("히유히유"));
        adapter.addItem(new StoreInfo("덕천식당"));
        adapter.addItem(new StoreInfo("청춘 튀겨"));
        adapter.addItem(new StoreInfo("히유히유"));
        adapter.addItem(new StoreInfo("덕천식당"));
        adapter.addItem(new StoreInfo("청춘 튀겨"));
        adapter.addItem(new StoreInfo("히유히유"));
        adapter.addItem(new StoreInfo("덕천식당"));
        adapter.addItem(new StoreInfo("청춘 튀겨"));

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