package org.techtown.jmt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FavoriteList extends Fragment {
    private Context mComtext;
    ImageButton share_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        FavoriteAdapter adapter = new FavoriteAdapter();

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