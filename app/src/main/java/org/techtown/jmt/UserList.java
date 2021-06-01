package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UserList extends Fragment {    // 어댑터가 storeAdapter랑 유사함. 중복 제거할 방법 찾기!
    private Context mComtext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.user_name_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        UserAdapter adapter = new UserAdapter();

        // 이 부분은 추후 데이터 베이스 연동으로 수정해야 함
        adapter.addItem(new UserInfo("soy-A"));
        adapter.addItem(new UserInfo("백구"));
        adapter.addItem(new UserInfo("랄랄"));
        adapter.addItem(new UserInfo("skql1502"));
        adapter.addItem(new UserInfo("musiclove"));
        adapter.addItem(new UserInfo("zl존고양이"));
        adapter.addItem(new UserInfo("안드s2"));
        adapter.addItem(new UserInfo("소연"));
        adapter.addItem(new UserInfo("예은"));

        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return v;
    }
}