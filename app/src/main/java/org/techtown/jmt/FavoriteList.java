package org.techtown.jmt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

public class FavoriteList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    FirebaseFirestore db;
    Fragment frag_other_list;
    RecyclerView recyclerView;
    String myId;
    SharedPreferences preferences;
    TextView toolbar_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        toolbar_text = getActivity().findViewById(R.id.toolbar_text);
        toolbar_text.setText("즐겨찾는 리스트");

        frag_other_list = new OtherList();

        recyclerView = v.findViewById(R.id.comments_recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        FavoriteAdapter adapter = new FavoriteAdapter(mContext);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        myId = preferences.getString("myId", "noId");

        db = FirebaseFirestore.getInstance();   // 즐겨찾기 등록한 회원 목록 가져오기
        db.collection("user")
                .document(myId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();
                            if (userDoc.exists()) {
                                if (userDoc.get("favorite") != null) {
                                    ArrayList<DocumentReference> favoriteArr = (ArrayList) userDoc.get("favorite");
                                    for (DocumentReference fdr : favoriteArr) {
                                        fdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot favoriteDoc = task.getResult();
                                                    if (favoriteDoc.exists()) {
                                                        adapter.addItem(new UserInfo(favoriteDoc.getString("name"), favoriteDoc.getString("id"), favoriteDoc.getLong("storeNum")));
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnFavoriteItemClickListener() {  // 클릭한 회원의 맛집 리스트 화면으로 이동
            @Override
            public void onItemClick(FavoriteAdapter.ViewHolder holder, View view, int position) {
                UserInfo item = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("user_id", item.getUserID());
                getParentFragmentManager().setFragmentResult("otherList", bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_other_list).addToBackStack(null).commit();
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