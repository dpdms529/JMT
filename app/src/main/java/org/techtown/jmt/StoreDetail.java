package org.techtown.jmt;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.kakao.sdk.user.UserApiClient;

import java.util.ArrayList;

public class StoreDetail extends Fragment {

    private static final String TAG = "TAG";
    private FirebaseFirestore db;

    private Context mContext;
    private TextView store_name;
    private TextView store_address;
    RecyclerView recyclerView;
    CommentAdapter adapter;

    ChipGroup chipGroup;

    private String storeName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store_detail, container, false);

        store_name = v.findViewById(R.id.store_name);
        store_address = v.findViewById(R.id.store_address);
        recyclerView = v.findViewById(R.id.comments_recyclerview);
        chipGroup = v.findViewById(R.id.chip_group);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        db = FirebaseFirestore.getInstance();

        adapter = new CommentAdapter(mContext);

        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                storeName = result.getString("store_name");

                store_name.setText(storeName);

                db.collection("store")
                        .whereEqualTo("name", storeName)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot document : task.getResult()){
                                        Log.d(TAG,"가게 정보" + document.getData());
                                        ArrayList<String> menu = new ArrayList<String>();
                                        document.getReference().collection("menu")
                                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    for(QueryDocumentSnapshot menuDoc : task.getResult()){
                                                        menu.add(String.valueOf(menuDoc.get("menu_name")));
                                                    }
                                                    setMenuChips(menu);
                                                }
                                            }
                                        });
                                        store_address.setText(String.valueOf(document.get("location")));
                                        ArrayList commentArr = (ArrayList) document.get("comment");
                                        for (int i = 0; i < commentArr.size(); i++) {
                                            DocumentReference cdr = (DocumentReference) commentArr.get(i);
                                            cdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()) {
                                                        DocumentSnapshot commentDoc = task.getResult();
                                                        if(commentDoc.exists()){
                                                            db.collection("user").document(String.valueOf(commentDoc.get("user")))
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if(task.isSuccessful()) {
                                                                                DocumentSnapshot userDoc = task.getResult();
                                                                                if(userDoc.exists()){
                                                                                    adapter.addItem(new StoreComment(userDoc.getString("name"), commentDoc.getString("content"),commentDoc.getString("photo")));
                                                                                    adapter.notifyDataSetChanged();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        });

                recyclerView.setAdapter(adapter);
            }
        });

        return v;
    }

    public void setMenuChips(ArrayList<String> menuArr) {
        for (String menu : menuArr) {
            Chip mChip = (Chip) this.getLayoutInflater().inflate(R.layout.chip, null, false);
            mChip.setText(menu);
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()
            );
            mChip.setPadding(paddingDp, 0, paddingDp, 0);
            chipGroup.addView(mChip);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}