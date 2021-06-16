package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    FirebaseFirestore db;
    Fragment frag_other_list;
    RecyclerView recyclerView;
    UserAdapter adapter;
    TextView toolbar_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);

        toolbar_text = getActivity().findViewById(R.id.toolbar_text);
        toolbar_text.setText("맛집 킬러");

        frag_other_list = new OtherList();

        recyclerView = v.findViewById(R.id.user_name_recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new UserAdapter(mContext);

        db = FirebaseFirestore.getInstance();
        db.collection("user")
                .orderBy("storeNum", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "회원 정보" + document.getData());
                                adapter.addItem(new UserInfo(document.getString("name"), document.getString("id"), document.getLong("storeNum")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnUserItemClickListener() {
            @Override
            public void onItemClick(UserAdapter.ViewHolder holder, View view, int position) {
                UserInfo item = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("user_id", item.getUserID());
                Log.d(TAG, bundle.getString("user_id"));
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