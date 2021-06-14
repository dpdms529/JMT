package org.techtown.jmt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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

import java.util.ArrayList;

public class FavoriteList extends Fragment {
    private static final String TAG = "TAG";
    private Context mComtext;
    FirebaseFirestore db;
    Fragment frag_other_list;
    RecyclerView recyclerView;
    ImageButton share_btn;
    String myId;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        frag_other_list = new OtherList();

        recyclerView = v.findViewById(R.id.comments_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mComtext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        FavoriteAdapter adapter = new FavoriteAdapter(mComtext);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        myId = preferences.getString("myId","noId");

        db = FirebaseFirestore.getInstance();
        db.collection("user")
                .document(myId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot userDoc = task.getResult();
                            if(userDoc.exists()){
                                if(userDoc.get("favorite") != null){
                                    ArrayList<DocumentReference> favoriteArr = (ArrayList)userDoc.get("favorite");
                                    for(DocumentReference fdr : favoriteArr){
                                        fdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot favoriteDoc = task.getResult();
                                                    if(favoriteDoc.exists()){
                                                        adapter.addItem(new UserInfo(favoriteDoc.getString("name"),String.valueOf(favoriteDoc.get("id")),favoriteDoc.getLong("storeNum")));
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

        adapter.setOnItemClickListener(new OnFavoriteItemClickListener() {
            @Override
            public void onItemClcik(FavoriteAdapter.ViewHolder holder, View view, int position) {
                UserInfo item = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("user_id",item.getUserID());
                getParentFragmentManager().setFragmentResult("otherList",bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout,frag_other_list).addToBackStack(null).commit();
            }
        });

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