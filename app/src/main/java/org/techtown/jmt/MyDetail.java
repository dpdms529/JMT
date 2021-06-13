package org.techtown.jmt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.UserApiClient;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyDetail extends Fragment {
    private static final String TAG = "TAG";
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private Context mContext;
    private ArrayAdapter arrayAdapter;

    private TextView store_name;
    private TextView store_address;
    private Spinner category_spinner;
    private ImageView food_image;
    private EditText menu_edit;
    private EditText comment_edit;

    private Button modify_btn;

    private String commentDocName;
    private String storeDocName;
    private Uri file;
    private int index;

    private String storeName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_detail, container, false);

        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                storeName = result.getString("store_name");
            }
        });
        store_name = v.findViewById(R.id.store_name);
        store_address = v.findViewById(R.id.store_address);
        category_spinner = v.findViewById(R.id.category);
        food_image = v.findViewById(R.id.food_image);
        menu_edit = v.findViewById(R.id.menu);
        comment_edit = v.findViewById(R.id.comment);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        UserApiClient.getInstance().me((user, error) -> {
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error);
            } else if (user != null) {
                db.collection("user")
                        .document(String.valueOf(user.getId()))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot userDoc = task.getResult();
                                    if (userDoc.exists()) {
                                        Log.d(TAG, "사용자 정보 : " + userDoc.get("store"));
                                        ArrayList storeArr = (ArrayList) userDoc.get("store");
                                        for (int i = 0; i < storeArr.size(); i++) {
                                            DocumentReference sdr = (DocumentReference) storeArr.get(i);
                                            sdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()) {
                                                        DocumentSnapshot storeDoc = task.getResult();
                                                        if(storeDoc.exists() && storeDoc.getString("name").equals(storeName)){
                                                            Log.d(TAG, "가게 정보 : " + storeDoc.getData());
                                                            store_name.setText(storeDoc.getString("name"));
                                                            store_address.setText(storeDoc.getString("location"));
                                                            ArrayList commentArr = (ArrayList) storeDoc.get("comment");
                                                            for (int j = 0; j < commentArr.size(); j++) {
                                                                DocumentReference cdr = (DocumentReference) commentArr.get(j);
                                                                cdr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot commentDoc = task.getResult();
                                                                            if (commentDoc.exists()) {
                                                                                Log.d(TAG, "댓글 정보 : " + commentDoc.getData());
                                                                                if ((Long) commentDoc.get("user") == user.getId()) {
                                                                                    menu_edit.setText(commentDoc.getString("menu"));
                                                                                    comment_edit.setText(commentDoc.getString("content"));
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
                                        }
                                    }
                                }
                            }
                        });
            }
            return null;
        });

        modify_btn = v.findViewById(R.id.button);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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