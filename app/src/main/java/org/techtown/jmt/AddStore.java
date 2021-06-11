package org.techtown.jmt;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

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
import com.kakao.sdk.user.UserApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddStore extends Fragment {
    private static final String TAG = "TAG";
    private final int GALLERY_CODE = 10;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private Context mContext;
    private ArrayAdapter arrayAdapter;

    private EditText store_name_edit;
    private Spinner category_spinner;
    private ImageView food_image;
    private EditText menu_edit;
    private EditText comment_edit;

    private Button register_btn;

    private String commentDocName;
    private String storeDocName;

    private boolean flag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_store, container, false);

        db = FirebaseFirestore.getInstance();

        // 스피너(카테고리) 구현
        category_spinner = (Spinner)v.findViewById(R.id.category);
        arrayAdapter = ArrayAdapter.createFromResource(mContext, R.array.categories, R.layout.support_simple_spinner_dropdown_item);
        category_spinner.setAdapter(arrayAdapter);

        // 뷰 설정
        store_name_edit = (EditText)v.findViewById(R.id.edit_store_name);
        food_image = (ImageView)v.findViewById(R.id.food_image); // 어떻게 해야?
        menu_edit = (EditText)v.findViewById(R.id.menu);
        comment_edit = (EditText)v.findViewById(R.id.comment);

        register_btn = (Button)v.findViewById(R.id.button);
        register_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;

                // 입력 값 가져오기
                String store_name = store_name_edit.getText().toString();
                String category_selected = category_spinner.getSelectedItem().toString();
                String menu_name = menu_edit.getText().toString();
                String comment_content = comment_edit.getText().toString();

                // 사용자 확인 및 데이터 전송
                Map<String, Object> commentData = new HashMap<>();
                Map<String, Object> storeData = new HashMap<>();
                UserApiClient.getInstance().me((user, error) -> {
                    if (error != null) {
                        Log.e(TAG, "사용자 정보 요청 실패", error);
                    } else if (user != null) {
                        commentDocName = setDocID("comment", String.valueOf(user.getId()));
                        storeDocName = setDocID("store", String.valueOf(user.getId()));

                        // store 데이터 전송
                        storeData.put("category", category_selected);
                        storeData.put("comment", commentDocName);
                        storeData.put("location", "전라북도 전주시 덕진구 덕진동1가 664-6번지 KR 1층 110호"); // 주소 추가
                        storeData.put("name", store_name);
                        storeData.put("menu", menu_name);

                        CollectionReference storeColRef = db.collection("store");

                        Task<QuerySnapshot> temp;
                        temp = storeColRef.whereEqualTo("name", store_name).get(); // 이름 같은 가게 존재 여부 확인 후 docName 설정, 나중에 기준 더 추가할 것
                        temp.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (temp.getResult().isEmpty()){
                                    storeColRef.document(storeDocName)
                                            .set(storeData);
                                    storeColRef.document(storeDocName)
                                            .update("comment", FieldValue.arrayUnion("/comment/" + commentDocName),
                                                    "lover", FieldValue.increment(1));

                                    commentData.put("user", String.valueOf(user.getId()));
                                    commentData.put("content", comment_content);
                                    commentData.put("store", storeDocName);

                                    // add comment document
                                    db.collection("comment").document(commentDocName)
                                            .set(commentData);

                                    // user field update
                                    db.collection("user").document(String.valueOf(user.getId()))
                                            .update("store", FieldValue.arrayUnion("/store/" + storeDocName),
                                                    "storeNum", FieldValue.increment(1));
                                } else {    // 이미 등록된 식당 -> comment, lover 필드 update
                                    for(QueryDocumentSnapshot document : temp.getResult()) {
                                        storeDocName = document.getId();
                                        storeColRef.document(storeDocName)
                                                .update("comment", FieldValue.arrayUnion("/comment/" + commentDocName),
                                                        "lover", FieldValue.increment(1));
                                        commentData.put("user", String.valueOf(user.getId()));
                                        commentData.put("content", comment_content);
                                        commentData.put("store", storeDocName);

                                        // add comment document
                                        db.collection("comment").document(commentDocName)
                                                .set(commentData);

                                        // user field update
                                        db.collection("user").document(String.valueOf(user.getId()))
                                                .update("store", FieldValue.arrayUnion("/store/" + storeDocName),
                                                        "storeNum", FieldValue.increment(1));
                                    }
                                }
                            }
                        });
                    }
                    return null;
                });

            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public String setDocID(String docType, String userID) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date time = new Date();
        return docType + "_" + userID + "_" + dateformat.format(time);
    }
}