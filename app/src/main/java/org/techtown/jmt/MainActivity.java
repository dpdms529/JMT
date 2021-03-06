package org.techtown.jmt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    Fragment frag_my_list;
    Fragment frag_store_list;
    Fragment frag_user_list;
    Fragment frag_favorite_list;
    String currentTitle = "";

    TextView toolbar_text;

    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRF;

    SharedPreferences preferences;
    String myId;

    private long lastTimeBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRF = storage.getReference();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        myId = preferences.getString("myId", "noId");

        // ??????????????? ??????
        frag_my_list = new MyList();
        frag_store_list = new StoreList();
        frag_user_list = new UserList();
        frag_favorite_list = new FavoriteList();

        // ????????? ????????? ??????
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar_text = findViewById(R.id.toolbar_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // ??? ?????? ??????
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).commit();

        // ????????? ?????? ??? ??????
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);  // ????????? ????????? ??????
        // ??????????????? ?????? ???????????? ???, ????????? ??????????????? ????????? ?????????
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tab_my_list:
                                setFragment(frag_my_list, String.valueOf(item.getTitle()));
                                return true;
                            case R.id.tab_store_list:
                                setFragment(frag_store_list, String.valueOf(item.getTitle()));
                                return true;
                            case R.id.tab_user_list:
                                setFragment(frag_user_list, String.valueOf(item.getTitle()));
                                return true;
                            case R.id.tab_favorite_list:
                                setFragment(frag_favorite_list, String.valueOf(item.getTitle()));
                                return true;
                        }
                        return false;
                    }
                }
        );
    }

    private void setFragment(Fragment fragment, String title) {
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {  // ?????? ??????????????? ????????? ???????????? ?????????????????? ?????????
            getSupportFragmentManager().popBackStack();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        currentTitle = title;  // ????????? ?????? flag
    }
    
    // ?????? ?????? ?????? ??????
    @Override
    public void onSaveInstanceState(Bundle backup) {
        super.onSaveInstanceState(backup);
        backup.putString("currentTitle", currentTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle backup) {
        super.onRestoreInstanceState(backup);

        String backup_str = backup.getString("currentTitle");

        switch (backup_str) {
            case "?????? ??????":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).commit();
                break;
            case "????????? ??????":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_list).commit();
                break;
            case "?????? ??????":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_user_list).commit();
                break;
            case "???????????? ?????????":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_favorite_list).commit();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.logout:
                // ????????????
                new AlertDialog.Builder(this)
                        .setTitle("????????????")
                        .setMessage("???????????????????????????????")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                UserApiClient.getInstance().logout(error -> {
                                    if (error != null) {
                                        Log.e(TAG, "???????????? ??????", error);
                                    } else {
                                        Log.i(TAG, "???????????? ??????");
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(intent);
                                    }
                                    return null;
                                });
                                Toast.makeText(getApplicationContext(), "???????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "???????????? ??????", Toast.LENGTH_SHORT).show();
                    }
                }).show();
                break;
            case R.id.leave:
                // ??????
                new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage("?????????????????????????")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                UserApiClient.getInstance().unlink(error -> {
                                    if (error != null) {
                                        Log.e(TAG, "?????? ?????? ??????", error);
                                    } else {
                                        Log.i(TAG, "?????? ?????? ??????");
                                        db.collection("comment")
                                                .whereEqualTo("user", myId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot commentDoc : task.getResult()) {
                                                                DocumentReference commentDR = commentDoc.getReference();
                                                                String store = commentDoc.getString("store");
                                                                Log.d(TAG, "store id  : " + store);
                                                                db.collection("store")
                                                                        .document(store)
                                                                        .get()
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    DocumentSnapshot storeDoc = task.getResult();
                                                                                    if (storeDoc.exists()) {
                                                                                        Log.d(TAG, "storeDoc  : " + storeDoc.getData());
                                                                                        DocumentReference storeDR = storeDoc.getReference();
                                                                                        if (storeDoc.getLong("lover") == 1) {
                                                                                            storeDR.collection("menu")
                                                                                                    .get()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                for (QueryDocumentSnapshot menuDoc : task.getResult()) {
                                                                                                                    Log.d(TAG, "menuDoc lover1 : " + menuDoc.getData());
                                                                                                                    DocumentReference menuDR = menuDoc.getReference();
                                                                                                                    menuDR.delete();
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                            storeDR.delete();
                                                                                        } else {
                                                                                            storeDR.update("comment", FieldValue.arrayRemove(commentDR)
                                                                                                    , "lover", FieldValue.increment(-1));
                                                                                            storeDR.collection("menu")
                                                                                                    .whereEqualTo("menu_name", commentDoc.get("menu"))
                                                                                                    .get()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                for (QueryDocumentSnapshot menuDoc : task.getResult()) {
                                                                                                                    DocumentReference menuDR = menuDoc.getReference();
                                                                                                                    if (menuDoc.getLong("lover") == 1) {
                                                                                                                        Log.d(TAG, "menuDoc menuLover1 : " + menuDoc.getData());
                                                                                                                        menuDR.delete();
                                                                                                                    } else {
                                                                                                                        Log.d(TAG, "menuDoc menuLoverMore : " + menuDoc.getData());
                                                                                                                        menuDR.update("lover", FieldValue.increment(-1));
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
                                                                if (commentDoc.get("photo") != null) {
                                                                    StorageReference desertRF = storageRF.child(commentDoc.getString("photo"));
                                                                    Log.d(TAG, "storage : " + desertRF.getName());
                                                                    desertRF.delete();

                                                                }
                                                                commentDR.delete();
                                                                Log.d(TAG, "comment deleted ");
                                                            }
                                                        }
                                                    }
                                                });
                                        DocumentReference userDR = db.collection("user").document(myId);
                                        db.collection("user")
                                                .whereNotEqualTo("id", myId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot userDoc : task.getResult()) {
                                                                if (userDoc.exists()) {
                                                                    ArrayList<DocumentReference> favoriteArr = (ArrayList<DocumentReference>) userDoc.get("favorite");
                                                                    if (favoriteArr.size() != 0) {
                                                                        for (DocumentReference favoriteDR : favoriteArr) {
                                                                            if (favoriteDR.equals(userDR)) {
                                                                                userDoc.getReference().update("favorite", FieldValue.arrayRemove(favoriteDR));
                                                                            }
                                                                        }

                                                                    }

                                                                }
                                                            }
                                                            userDR.delete();
                                                            Log.d(TAG, "user deleted ");
                                                        }
                                                    }
                                                });
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(intent);

                                    }
                                    return null;
                                });
                                Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                    }
                }).show();
                break;
            case android.R.id.home:
                onBackPressed();    // ????????? ????????????
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //??? ??? ????????? ?????? ??????
            if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
                finish();
                return;
            }
            lastTimeBackPressed = System.currentTimeMillis();
            Toast.makeText(this, "'??????' ????????? ??? ??? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
        } else {
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().getFragments().get(0)).commit();
            getSupportFragmentManager().popBackStack();
        }

    }
}