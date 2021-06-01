package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyList extends Fragment {
    private Context mComtext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        MyAdapter adapter = new MyAdapter();

        // 이 부분은 추후 데이터 베이스 연동으로 수정해야 함
        adapter.addItem(new PersonalComment("히유히유", "연어 덮밥 존맛. 연어가 살아있다!"));
        adapter.addItem(new PersonalComment("덕천식당", "순대 국밥이 참 맛있다~"));
        adapter.addItem(new PersonalComment("청춘 튀겨", "치킨 JMT~~"));
        adapter.addItem(new PersonalComment("히유히유", "연어 덮밥 존맛. 연어가 살아있다!"));
        adapter.addItem(new PersonalComment("덕천식당", "순대 국밥이 참 맛있다~"));
        adapter.addItem(new PersonalComment("청춘 튀겨", "치킨 JMT~~"));
        adapter.addItem(new PersonalComment("히유히유", "연어 덮밥 존맛. 연어가 살아있다!"));
        adapter.addItem(new PersonalComment("덕천식당", "순대 국밥이 참 맛있다~"));
        adapter.addItem(new PersonalComment("청춘 튀겨", "치킨 JMT~~"));

        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mComtext = context;
    }
}