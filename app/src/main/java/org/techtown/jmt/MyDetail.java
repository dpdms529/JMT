package org.techtown.jmt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.fragment.app.FragmentResultListener;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

    private TextView store_name;
    private TextView store_address;
    private TextView category;
    private ImageView food_image;
    private EditText menu_edit;
    private EditText comment_edit;

    private Button modify_btn;
    private Button delete_btn;

    private Uri file;

    private String storeName;
    private String myId;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_detail, container, false);
        store_name = v.findViewById(R.id.store_name);
        store_address = v.findViewById(R.id.store_address);
        category = v.findViewById(R.id.category);
        food_image = v.findViewById(R.id.food_image);
        menu_edit = v.findViewById(R.id.menu);
        comment_edit = v.findViewById(R.id.comment);
        modify_btn = v.findViewById(R.id.button);
        delete_btn = v.findViewById(R.id.button_delete);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance("gs://android-jmt.appspot.com");
        StorageReference storageReference = storage.getReference();

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        myId = preferences.getString("myId","noID");

        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                storeName = result.getString("store_name");
                Log.d(TAG, "storeName is " + storeName);
                db.collection("user")
                        .document(myId)
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
                                                        Log.d(TAG, "StoreName is " + storeName);
                                                        if(storeDoc.exists() && storeDoc.getString("name").equals(storeName)){
                                                            Log.d(TAG, "가게 정보 : " + storeDoc.getData());
                                                            store_name.setText(storeDoc.getString("name"));
                                                            store_address.setText(storeDoc.getString("location"));
                                                            category.setText(storeDoc.getString("category"));
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
                                                                                if (String.valueOf(commentDoc.get("user")).equals(myId)) {
                                                                                    menu_edit.setText(commentDoc.getString("menu"));
                                                                                    comment_edit.setText(commentDoc.getString("content"));
                                                                                    if(commentDoc.getString("photo")!=null){
                                                                                        storageReference.child(commentDoc.getString("photo")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                            @Override
                                                                                            public void onSuccess(Uri uri) {
                                                                                                Glide.with(getContext())
                                                                                                        .load(uri)
                                                                                                        .into(food_image);
                                                                                            }
                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(getContext(),"실패",Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });

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
                                        }
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

        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag_my_list = new MyList();
                StorageReference storageRef = storage.getReference();

                db.collection("user")
                        .document(myId)
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
                                                        Log.d(TAG, "StoreName is " + storeName);
                                                        if(storeDoc.exists() && storeDoc.getString("name").equals(storeName)){
                                                            Log.d(TAG, "가게 정보 : " + storeDoc.getData());
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
                                                                                if (commentDoc.get("user").equals(myId)) {
                                                                                    db.collection("comment").document(commentDoc.getId())
                                                                                            .update("content", comment_edit.getText().toString(),
                                                                                                    "menu", menu_edit.getText().toString());
                                                                                    if(file != null){   // 추가 또는 수정한 사진이 있다면 넣는다.
                                                                                        StorageReference riversRef = storageRef.child(storeDoc.getId() + "/" + myId + ".png");
                                                                                        Log.d(TAG,"사진 : " + riversRef.getPath());
                                                                                        db.collection("comment").document(commentDoc.getId())
                                                                                                .update("photo", riversRef.getPath());

                                                                                        UploadTask uploadTask = riversRef.putFile(file);
                                                                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(mContext,"사진이 정상적으로 업로드 되지 않음", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                                Toast.makeText(mContext,"사진이 정상적으로 업로드 됨",Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
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
                                        }
                                    }
                                }
                            }
                        });
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(MyDetail.this).commit();
                fragmentManager.popBackStack();
                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).addToBackStack(null).commit();
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("정말로 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection("user")
                                .document(String.valueOf(myId))
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
                                                                Log.d(TAG, "StoreName is " + storeName);
                                                                if(storeDoc.exists() && storeDoc.getString("name").equals(storeName)) {
                                                                    Log.d(TAG, "가게 정보 : " + storeDoc.getData());
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
                                                                                        if (String.valueOf(commentDoc.get("user")).equals(myId)) {
                                                                                            // user 문서 - store필드에서 ref제거, storeNum 업데이트
                                                                                            db.collection("user")
                                                                                                    .document(String.valueOf(myId))
                                                                                                    .update("store", FieldValue.arrayRemove(sdr),
                                                                                                            "storeNum", FieldValue.increment(-1));
                                                                                            // store 문서 - 해당 가게를 추가한 사람이 한 명이면 문서 삭제,
                                                                                            // 한 명 이상이면 comment필드에서 ref 제거, lover 업데이트
                                                                                            if((long) storeDoc.get("lover") == 1) {
                                                                                                sdr.collection("menu").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                        for(DocumentSnapshot document : task.getResult()){
                                                                                                            document.getReference().delete();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                                sdr.delete();
                                                                                            } else if((long) storeDoc.get("lover") > 1) {
                                                                                                sdr.collection("menu").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                        for(DocumentSnapshot document : task.getResult()){
                                                                                                            if(document.get("menu_name").equals(commentDoc.get("menu"))) {
                                                                                                                if((long) document.get("lover") == 1){
                                                                                                                    document.getReference().delete();
                                                                                                                } else if((long) document.get("lover") > 1) {
                                                                                                                    document.getReference().update("lover", FieldValue.increment(-1));
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                                sdr.update("comment", FieldValue.arrayRemove(cdr),
                                                                                                        "lover", FieldValue.increment(-1));
                                                                                            }
                                                                                            // 연결된 사진 storage에서 삭제
                                                                                            if(commentDoc.get("photo") != null){
                                                                                                StorageReference photoRef = storageReference.child(String.valueOf(commentDoc.get("photo")));
                                                                                                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        Toast.makeText(mContext,"사진이 정상적으로 삭제됨", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Toast.makeText(mContext,"사진이 정상적으로 삭제되지 않음", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                            // comment 콜렉션에서 문서 제거
                                                                                            cdr.delete();  // comment 콜렉션에서 문서 제거

                                                                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                                                            fragmentManager.beginTransaction().remove(MyDetail.this).commit();
                                                                                            fragmentManager.popBackStack();
                                                                                            Toast.makeText(mContext,"정상적으로 삭제되었습니다.",Toast.LENGTH_SHORT).show();
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
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return v;
    }

    private void loadAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityResult.launch(intent);
    }

    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                Log.d(TAG," 데이터는 : " + data.getData());
                file = data.getData();
                try{
                    InputStream in = getActivity().getContentResolver().openInputStream(file);
                    Bitmap img = BitmapFactory.decodeStream(in);
                    food_image.setImageBitmap(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    });

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

}