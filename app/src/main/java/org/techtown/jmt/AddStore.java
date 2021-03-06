package org.techtown.jmt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
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

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStore extends Fragment {
    private static final String TAG = "TAG";
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private Context mContext;
    private ArrayAdapter arrayAdapter;

    private Spinner category_spinner;
    private ImageView food_image;
    private EditText menu_edit;
    private EditText comment_edit;

    private Button register_btn;

    private String commentDocName;
    private String storeDocName;
    private Uri file;

    private String myId;
    private SharedPreferences preferences;

    private ArrayList<Document> documentArrayList = new ArrayList<>(); //지역명 검색 결과 리스트
    private EditText mSearchEdit;
    private TextView address_tv;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_store, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        myId = preferences.getString("myId", "noID");

        // 맛집 검색
        mSearchEdit = v.findViewById(R.id.search_editText);
        address_tv = v.findViewById(R.id.store_address);
        recyclerView = v.findViewById(R.id.search_recyclerView);
        SearchAdapter searchAdapter = new SearchAdapter(documentArrayList, getActivity().getApplicationContext(), mSearchEdit, address_tv, recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(searchAdapter);

        // editText 검색 텍스처이벤트
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() >= 1) {
                    documentArrayList.clear();
                    searchAdapter.clear();
                    searchAdapter.notifyDataSetChanged();
                    ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);    // 통신 인터페이스를 객체로 생성
                    Call<SearchResult> call = apiInterface.getSearchLocation(getString(R.string.kakao_REST_API), charSequence.toString(), 15, "FD6");   // 검색 조건 입력

                    // API 서버에 요청
                    call.enqueue(new Callback<SearchResult>() {
                        @Override
                        public void onResponse(@NotNull Call<SearchResult> call, @NotNull Response<SearchResult> response) {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                for (Document document : response.body().getDocuments()) {
                                    searchAdapter.addItem(document);
                                }
                                searchAdapter.notifyDataSetChanged();
                            } else {
                                Log.e(TAG, "failed: " + response.toString());
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<SearchResult> call, @NotNull Throwable t) {
                        }
                    });
                } else {
                    if (charSequence.length() <= 0) {
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // 데이터베이스 업로드
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // 스피너(카테고리) 구현
        category_spinner = (Spinner) v.findViewById(R.id.category);
        arrayAdapter = ArrayAdapter.createFromResource(mContext, R.array.categories, R.layout.support_simple_spinner_dropdown_item);
        category_spinner.setAdapter(arrayAdapter);

        // 뷰 설정
        food_image = (ImageView) v.findViewById(R.id.food_image);
        menu_edit = (EditText) v.findViewById(R.id.menu);
        comment_edit = (EditText) v.findViewById(R.id.comment);

        register_btn = (Button) v.findViewById(R.id.button);
        register_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 필수 입력 확인
                if (mSearchEdit.getText().toString().length() == 0) {
                    Toast.makeText(mContext, "식당 검색 후, 식당을 선택해주세요", Toast.LENGTH_SHORT).show();
                    mSearchEdit.requestFocus();
                    return;
                } else if (menu_edit.getText().toString().length() == 0) {
                    Toast.makeText(mContext, "메뉴를 입력해주세요", Toast.LENGTH_SHORT).show();
                    menu_edit.requestFocus();
                    return;
                } else if (comment_edit.getText().toString().length() == 0) {
                    Toast.makeText(mContext, "한줄평을 입력해주세요", Toast.LENGTH_SHORT).show();
                    comment_edit.requestFocus();
                    return;
                }

                // 입력 값 가져오기
                String store_name = mSearchEdit.getText().toString();
                String category_selected = category_spinner.getSelectedItem().toString();
                String menu_name = menu_edit.getText().toString();
                String comment_content = comment_edit.getText().toString();
                String location = address_tv.getText().toString();
                String[] location_array = location.split(" ");
                String do_location = location_array[0];
                String si_location = location_array[1];

                // 사용자 확인 및 데이터 전송
                Map<String, Object> commentData = new HashMap<>();
                Map<String, Object> storeData = new HashMap<>();
                Map<String, Object> menuData = new HashMap<>();
                final DocumentReference[] ref = new DocumentReference[2];

                commentDocName = setDocID("comment", myId);
                storeDocName = setDocID("store", myId);

                // store 데이터 전송
                storeData.put("category", category_selected);
                storeData.put("comment", commentDocName);
                storeData.put("location", location); // 주소 추가
                storeData.put("do", do_location);
                storeData.put("si", si_location);
                storeData.put("name", store_name);
                menuData.put("menu_name", menu_name);
                menuData.put("lover", 1);

                CollectionReference storeColRef = db.collection("store");   // 이름, 주소 같은 가게 존재 여부 확인 후 docName 설정
                storeColRef.whereEqualTo("name", store_name)
                        .whereEqualTo("location", location).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult().isEmpty()) {    // 등록된 적 없는 식당
                                    if (file != null) {   // 사진 업로드
                                        commentData.put("photo", uploadPicture());
                                    }

                                    storeColRef.document(storeDocName).set(storeData);
                                    storeColRef.document(storeDocName).collection("menu").add(menuData);
                                    storeColRef.document(storeDocName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot doc = task.getResult();
                                                if (doc.exists()) {
                                                    ref[1] = doc.getReference();
                                                    // user field update
                                                    db.collection("user").document(myId)
                                                            .update("store", FieldValue.arrayUnion(ref[1]),
                                                                    "storeNum", FieldValue.increment(1));
                                                }
                                            }
                                        }
                                    });

                                    commentData.put("user", myId);
                                    commentData.put("content", comment_content);
                                    commentData.put("store", storeDocName);
                                    commentData.put("menu", menu_name);

                                    // add comment document
                                    db.collection("comment").document(commentDocName).set(commentData);
                                    db.collection("comment").document(commentDocName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot doc = task.getResult();
                                                if (doc.exists()) {
                                                    ref[0] = doc.getReference();
                                                    storeColRef.document(storeDocName)
                                                            .update("comment", FieldValue.arrayUnion(ref[0]),
                                                                    "lover", FieldValue.increment(1));
                                                }
                                            }
                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            fragmentManager.beginTransaction().remove(AddStore.this).commit();
                                            fragmentManager.popBackStack();
                                            Toast.makeText(mContext, "맛집이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {    // 이미 등록된 식당 -> comment, lover 필드 update
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        storeDocName = document.getId();
                                        db.collection("comment")
                                                .whereEqualTo("user", myId)
                                                .whereEqualTo("store", storeDocName)
                                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {// 본인이 올린 적 있는 곳 - 등록 불가
                                                    QuerySnapshot doc = task.getResult();
                                                    if (!doc.isEmpty()) {
                                                        Toast.makeText(mContext, "이미 리스트에 존재하는 식당입니다.", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    } else {
                                                        if (file != null) {   // 사진 업로드
                                                            commentData.put("photo", uploadPicture());
                                                        }

                                                        commentData.put("user", myId);
                                                        commentData.put("content", comment_content);
                                                        commentData.put("store", storeDocName);
                                                        commentData.put("menu", menu_name);

                                                        // add comment document
                                                        db.collection("comment").document(commentDocName)
                                                                .set(commentData);
                                                        db.collection("comment").document(commentDocName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot doc = task.getResult();
                                                                    if (doc.exists()) {
                                                                        ref[0] = doc.getReference();
                                                                        storeColRef.document(storeDocName)
                                                                                .update("comment", FieldValue.arrayUnion(ref[0]),
                                                                                        "lover", FieldValue.increment(1));
                                                                        Task<QuerySnapshot> temp;
                                                                        temp = storeColRef.document(storeDocName).collection("menu")
                                                                                .whereEqualTo("menu_name", menu_name).get();    // 하위 콜렉션 menu에 같은 메뉴 입력된 적 있는지 확인
                                                                        temp.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                if (temp.getResult().isEmpty()) { // 같은 메뉴 없다면 새로 입력
                                                                                    storeColRef.document(storeDocName).collection("menu").add(menuData);
                                                                                } else { // 이미 입력된 메뉴라면, 카운트 값만 추가
                                                                                    for (QueryDocumentSnapshot document : temp.getResult()) {
                                                                                        storeColRef.document(storeDocName).collection("menu").document(document.getId())
                                                                                                .update("lover", FieldValue.increment(1));
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        // user field update
                                                        db.collection("user").document(myId)
                                                                .update("store", FieldValue.arrayUnion(document.getReference()),
                                                                        "storeNum", FieldValue.increment(1));
                                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                        fragmentManager.beginTransaction().remove(AddStore.this).commit();
                                                        fragmentManager.popBackStack();
                                                        Toast.makeText(mContext, "맛집이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
            }
        });

        food_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAlbum();
            }
        });
        return v;
    }

    public String uploadPicture() {
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child(storeDocName + "/" + myId + ".png");
        Log.d(TAG, "사진 : " + riversRef.getPath());
        riversRef.putFile(file);
        return riversRef.getPath();
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

    private void loadAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityResult.launch(intent);
    }

    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                Log.d(TAG, " 데이터는 : " + data.getData());
                file = data.getData();
                try {
                    InputStream in = getActivity().getContentResolver().openInputStream(file);
                    Bitmap img = BitmapFactory.decodeStream(in);
                    food_image.setImageBitmap(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    });
}