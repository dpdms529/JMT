package org.techtown.jmt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRF = storage.getReference();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        myId = preferences.getString("myId","noId");

        // 프래그먼트 생성
        frag_my_list = new MyList();
        frag_store_list = new StoreList();
        frag_user_list = new UserList();
        frag_favorite_list = new FavoriteList();

        // 커스텀 액션바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar_text = findViewById(R.id.toolbar_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // 첫 화면 지정
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).commit();

        // 하단바 생성 및 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);  // 하단바 타이틀 표시
        // 하단바에서 메뉴 선택했을 때, 알맞은 프래그먼트 띄우는 리스너
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

    private void setFragment(Fragment fragment, String title){
        for(int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++){  // 메인 프래그먼트 진입시 백스택의 프래그먼트를 지워줌
            getSupportFragmentManager().popBackStack();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        currentTitle = title;  // 백업을 위한 flag
    }


    // 단말 방향 전환 처리
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
            case "나의 맛집":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).commit();
                break;
            case "모두의 맛집":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_list).commit();
                break;
            case "맛집 킬러":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_user_list).commit();
                break;
            case "즐겨찾는 리스트":
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
        switch (item.getItemId()){
            case R.id.logout:
                // 로그아웃
                new AlertDialog.Builder(this)
                        .setTitle("로그아웃")
                        .setMessage("로그아웃하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                UserApiClient.getInstance().logout(error->{
                                    if(error != null){
                                        Log.e(TAG,"로그아웃 실패", error);
                                    }else{
                                        Log.i(TAG,"로그아웃 성공");
                                        Intent intent = new Intent(getApplicationContext(),Login.class);
                                        startActivity(intent);
                                    }
                                    return null;
                                });
                                Toast.makeText(getApplicationContext(),"로그아웃 완료",Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(),"로그아웃 취소",Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                break;
            case R.id.leave:
                // 탈퇴
                new AlertDialog.Builder(this)
                        .setTitle("탈퇴")
                        .setMessage("탈퇴하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                UserApiClient.getInstance().unlink(error->{
                                    if(error != null){
                                        Log.e(TAG,"연결 끊기 실패", error);
                                    }else{
                                        Log.i(TAG,"연결 끊기 성공");
                                        db.collection("comment")
                                                .whereEqualTo("user",myId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            for(QueryDocumentSnapshot commentDoc : task.getResult()){
                                                                DocumentReference commentDR = commentDoc.getReference();
                                                                String store = commentDoc.getString("store");
                                                                Log.d(TAG,"store id  : " + store);
                                                                db.collection("store")
                                                                        .document(store)
                                                                        .get()
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if(task.isSuccessful()){
                                                                                    DocumentSnapshot storeDoc = task.getResult();
                                                                                    if(storeDoc.exists()){
                                                                                        Log.d(TAG,"storeDoc  : " + storeDoc.getData());
                                                                                        DocumentReference storeDR = storeDoc.getReference();
                                                                                        if(storeDoc.getLong("lover")==1){
                                                                                            storeDR.collection("menu")
                                                                                                    .get()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                for(QueryDocumentSnapshot menuDoc : task.getResult()){
                                                                                                                    Log.d(TAG,"menuDoc lover1 : " + menuDoc.getData());
                                                                                                                    DocumentReference menuDR = menuDoc.getReference();
                                                                                                                    menuDR.delete();
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                            storeDR.delete();
                                                                                        }else{
                                                                                            storeDR.update("comment", FieldValue.arrayRemove(commentDR)
                                                                                            ,"lover", FieldValue.increment(-1));
                                                                                            storeDR.collection("menu")
                                                                                                    .whereEqualTo("menu_name",commentDoc.get("menu"))
                                                                                                    .get()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                for(QueryDocumentSnapshot menuDoc : task.getResult()){
                                                                                                                    DocumentReference menuDR = menuDoc.getReference();
                                                                                                                    if(menuDoc.getLong("lover") ==  1){
                                                                                                                        Log.d(TAG,"menuDoc menuLover1 : " + menuDoc.getData());
                                                                                                                        menuDR.delete();
                                                                                                                    }else{
                                                                                                                        Log.d(TAG,"menuDoc menuLoverMore : " + menuDoc.getData());
                                                                                                                        menuDR.update("lover",FieldValue.increment(-1));
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
                                                                if(commentDoc.get("photo") != null){
                                                                    StorageReference desertRF = storageRF.child(commentDoc.getString("photo"));
                                                                    Log.d(TAG,"storage : " + desertRF.getName());
                                                                    desertRF.delete();

                                                                }
                                                                commentDR.delete();
                                                                Log.d(TAG,"comment deleted ");
                                                            }
                                                        }
                                                    }
                                                });
                                        DocumentReference userDR = db.collection("user").document(myId);
                                        db.collection("user")
                                                .whereNotEqualTo("id",myId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            for(QueryDocumentSnapshot userDoc : task.getResult()){
                                                                if(userDoc.exists()){
                                                                    ArrayList<DocumentReference> favoriteArr = (ArrayList<DocumentReference>)userDoc.get("favorite");
                                                                    if(favoriteArr.size()!=0){
                                                                        for(DocumentReference favoriteDR : favoriteArr){
                                                                            if(favoriteDR.equals(userDR)){
                                                                                userDoc.getReference().update("favorite",FieldValue.arrayRemove(favoriteDR));
                                                                            }
                                                                        }

                                                                    }

                                                                }
                                                            }
                                                            userDR.delete();
                                                            Log.d(TAG,"user deleted ");
                                                        }
                                                    }
                                                });
                                        Intent intent = new Intent(getApplicationContext(),Login.class);
                                        startActivity(intent);

                                    }
                                    return null;
                                });
                                Toast.makeText(getApplicationContext(),"탈퇴 완료",Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(),"탈퇴 취소",Toast.LENGTH_SHORT).show();
                    }
                }).show();
                break;
            case android.R.id.home:
                // 액션바 뒤로가기
                if(fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.beginTransaction().remove(fragmentManager.getFragments().get(0)).commit();
                    fragmentManager.popBackStack();
                } else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}