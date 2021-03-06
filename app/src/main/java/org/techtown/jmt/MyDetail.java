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

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;

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
    private int position;
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
        myId = preferences.getString("myId", "noID");

        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                position = result.getInt("position");
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
                                        Log.d(TAG, "????????? ?????? : " + userDoc.get("store"));
                                        ArrayList<DocumentReference> storeArr = (ArrayList) userDoc.get("store");
                                        DocumentReference storeDR = storeArr.get(position);
                                        storeDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot storeDoc = task.getResult();
                                                    if (storeDoc.exists()) {
                                                        store_name.setText(storeDoc.getString("name"));
                                                        store_address.setText(storeDoc.getString("location"));
                                                        category.setText(storeDoc.getString("category"));
                                                        ArrayList<DocumentReference> commentArr = (ArrayList) storeDoc.get("comment");
                                                        for (DocumentReference commentDR : commentArr) {
                                                            commentDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot commentDoc = task.getResult();
                                                                        if (commentDoc.exists()) {
                                                                            if (commentDoc.getString("user").equals(myId)) {
                                                                                menu_edit.setText(commentDoc.getString("menu"));
                                                                                comment_edit.setText(commentDoc.getString("content"));
                                                                                if (commentDoc.getString("photo") != null) {
                                                                                    storageReference.child(commentDoc.getString("photo")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                        @Override
                                                                                        public void onSuccess(Uri uri) {
                                                                                            Glide.with(getContext())
                                                                                                    .load(uri)
                                                                                                    .into(food_image);
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
                                        Log.d(TAG, "????????? ?????? : " + userDoc.get("store"));
                                        ArrayList<DocumentReference> storeArr = (ArrayList) userDoc.get("store");
                                        DocumentReference storeDR = storeArr.get(position);
                                        storeDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot storeDoc = task.getResult();
                                                    if (storeDoc.exists()) {
                                                        ArrayList<DocumentReference> commentArr = (ArrayList) storeDoc.get("comment");
                                                        for (DocumentReference commentDR : commentArr) {
                                                            commentDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot commentDoc = task.getResult();
                                                                        if (commentDoc.exists()) {
                                                                            Log.d(TAG, "?????? ?????? : " + commentDoc.getData());
                                                                            if (commentDoc.getString("user").equals(myId)) {
                                                                                db.collection("comment").document(commentDoc.getId())
                                                                                        .update("content", comment_edit.getText().toString(),
                                                                                                "menu", menu_edit.getText().toString());
                                                                                if (file != null) {   // ?????? ?????? ????????? ????????? ????????? ?????????.
                                                                                    StorageReference riversRef = storageRef.child(storeDoc.getId() + "/" + myId + ".png");
                                                                                    Log.d(TAG, "?????? : " + riversRef.getPath());
                                                                                    db.collection("comment").document(commentDoc.getId())
                                                                                            .update("photo", riversRef.getPath());
                                                                                    riversRef.putFile(file);
                                                                                }
                                                                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                                                fragmentManager.beginTransaction().remove(MyDetail.this).commit();
                                                                                fragmentManager.popBackStack();
                                                                                getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).addToBackStack(null).commit();
                                                                                Toast.makeText(mContext, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
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
                        });
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                                                Log.d(TAG, "????????? ?????? : " + userDoc.get("store"));
                                                ArrayList<DocumentReference> storeArr = (ArrayList) userDoc.get("store");
                                                DocumentReference storeDR = storeArr.get(position);
                                                storeDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot storeDoc = task.getResult();
                                                            if (storeDoc.exists()) {
                                                                ArrayList<DocumentReference> commentArr = (ArrayList) storeDoc.get("comment");
                                                                for (DocumentReference commentDR : commentArr) {
                                                                    commentDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot commentDoc = task.getResult();
                                                                                if (commentDoc.exists()) {
                                                                                    Log.d(TAG, "?????? ?????? : " + commentDoc.getData());
                                                                                    if (String.valueOf(commentDoc.get("user")).equals(myId)) {
                                                                                        // user ?????? - store???????????? ref??????, storeNum ????????????
                                                                                        db.collection("user")
                                                                                                .document(String.valueOf(myId))
                                                                                                .update("store", FieldValue.arrayRemove(storeDR),
                                                                                                        "storeNum", FieldValue.increment(-1));
                                                                                        // store ?????? - ?????? ????????? ????????? ????????? ??? ????????? ?????? ??????,
                                                                                        // ??? ??? ???????????? comment???????????? ref ??????, lover ????????????
                                                                                        if ((long) storeDoc.get("lover") == 1) {
                                                                                            storeDR.collection("menu").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                    for (DocumentSnapshot document : task.getResult()) {
                                                                                                        document.getReference().delete();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                            storeDR.delete();
                                                                                        } else if ((long) storeDoc.get("lover") > 1) {
                                                                                            storeDR.collection("menu").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                    for (DocumentSnapshot document : task.getResult()) {
                                                                                                        if (document.get("menu_name").equals(commentDoc.get("menu"))) {
                                                                                                            if ((long) document.get("lover") == 1) {
                                                                                                                document.getReference().delete();
                                                                                                            } else if ((long) document.get("lover") > 1) {
                                                                                                                document.getReference().update("lover", FieldValue.increment(-1));
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                            storeDR.update("comment", FieldValue.arrayRemove(commentDR),
                                                                                                    "lover", FieldValue.increment(-1));
                                                                                        }
                                                                                        // ????????? ?????? storage?????? ??????
                                                                                        if (commentDoc.get("photo") != null) {
                                                                                            StorageReference photoRef = storageReference.child(String.valueOf(commentDoc.get("photo")));
                                                                                            photoRef.delete();
                                                                                        }
                                                                                        // comment ??????????????? ?????? ??????
                                                                                        commentDR.delete();  // comment ??????????????? ?????? ??????

                                                                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                                                        fragmentManager.beginTransaction().remove(MyDetail.this).commit();
                                                                                        fragmentManager.popBackStack();
                                                                                        Toast.makeText(mContext, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
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
                                });
                    }
                });
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
                Log.d(TAG, " ???????????? : " + data.getData());
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

}