package org.techtown.jmt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    Fragment frag_add_store;
    Fragment frag_my_detail;
    Fragment frag_my_list;
    RecyclerView recyclerView;
    MyAdapter adapter;
    FirebaseFirestore db;
    String myId;
    String userName;
    private SharedPreferences preferences;
    String mjlist;
    Map<Integer, PersonalComment> adapterData;
    TextView toolbar_text;
    FloatingActionButton add_btn;
    ImageView share_btn;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_list, container, false);

        toolbar_text = getActivity().findViewById(R.id.toolbar_text);
        toolbar_text.setText("?????? ?????? ?????????");

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        frag_add_store = new AddStore();
        frag_my_detail = new MyDetail();
        frag_my_list = new MyList();

        recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(mContext);
        adapterData = new HashMap<>();

        db = FirebaseFirestore.getInstance();
        myId = preferences.getString("myId", "noId");
        Log.d(TAG, "myId is " + myId);
        db.collection("user")
                .document(myId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();
                            if (userDoc.exists()) {
                                userName = String.valueOf(userDoc.get("name"));
                                mjlist = "<" + userName + "?????? ?????? ?????????>";
                                Log.d(TAG, "????????? ?????? : " + userDoc.get("store"));
                                if (userDoc.get("store") != null) {
                                    ArrayList<DocumentReference> storeArr = (ArrayList) userDoc.get("store");
                                    int i = 0;
                                    for (DocumentReference sdr : storeArr) {
                                        int finalI = i;
                                        sdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot storeDoc = task.getResult();
                                                    if (storeDoc.exists()) {
                                                        Log.d(TAG, "?????? ?????? : " + storeDoc.getData());
                                                        ArrayList<DocumentReference> commentArr = (ArrayList) storeDoc.get("comment");
                                                        for (DocumentReference cdr : commentArr) {
                                                            cdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot commentDoc = task.getResult();
                                                                        if (commentDoc.exists()) {
                                                                            Log.d(TAG, "?????? ?????? : " + commentDoc.getData());
                                                                            if (commentDoc.getString("user").equals(myId)) {
                                                                                Log.d(TAG, storeDoc.getString("name") + commentDoc.getString("photo"));
                                                                                adapterData.put(finalI, new PersonalComment(storeDoc.getString("name"), commentDoc.getString("content"), commentDoc.getString("photo"), storeDoc.getString("location")));
                                                                                if (adapterData.size() == storeArr.size()) {
                                                                                    Log.d(TAG, "data size is " + adapterData.size());
                                                                                    for (int i = 0; i < adapterData.size(); i++) {
                                                                                        adapter.addItem(adapterData.get(i));
                                                                                        adapter.notifyDataSetChanged();
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        i++;
                                    }

                                }
                            }
                        }
                    }
                });

        recyclerView.setAdapter(adapter);

        db.collection("user").document(myId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            ArrayList<DocumentReference> storeArr = (ArrayList<DocumentReference>) document.get("store");
                            for (DocumentReference storeDoc : storeArr) {
                                storeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            mjlist = mjlist + "\n- " + String.valueOf(document.get("name"));
                                            mjlist = mjlist + "\n  (" + String.valueOf(document.get("location")) + ")";
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

        adapter.setOnItemClickListener(new OnMyItemClickListener() {
            @Override
            public void onItemClick(MyAdapter.ViewHolder holder, View view, int position) {
                PersonalComment item = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                Log.d(TAG, "send store_name is " + bundle.getString("store_name"));
                getParentFragmentManager().setFragmentResult("requestKey", bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_detail).addToBackStack(null).commit();
            }
        });

        add_btn = v.findViewById(R.id.add_floating_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_add_store).addToBackStack(null).commit();
            }
        });

        share_btn = getActivity().findViewById(R.id.share_tool_btn);
        share_btn.setVisibility(View.VISIBLE);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, mjlist);

                Intent Sharing = Intent.createChooser(Sharing_intent, "????????????");
                startActivity(Sharing);
            }
        });

        mSwipeRefreshLayout = v.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_layout, frag_my_list).commit();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        super.onPause();
        share_btn.setVisibility(View.GONE);
    }
}

