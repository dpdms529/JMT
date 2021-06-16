package org.techtown.jmt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.sdk.user.UserApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String TAG = "user";
    ImageButton login;
    FirebaseFirestore db;
    String myId;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Map<String, Object> userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        userInfo = new HashMap<>();

        setContentView(R.layout.activity_login);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 1500);

        UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error2) -> {
            if (error2 != null) {
                Log.e(TAG, "토큰 정보 보기 실패", error2);

                login = findViewById(R.id.login);
                login.setVisibility(View.VISIBLE);
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserApiClient.getInstance().loginWithKakaoAccount(getApplicationContext(), (oAuthToken, error) -> {
                            if (error != null) {
                                Log.e(TAG, "로그인 실패", error);
                            } else if (oAuthToken != null) {
                                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());

                                UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error2) -> {
                                    if (error2 != null) {
                                        Log.e(TAG, "토큰 정보 보기 실패", error2);
                                    } else if (accessTokenInfo != null) {
                                        Log.i(TAG, "토큰 정보 보기 성공" + "\n회원 번호 : " + accessTokenInfo.getId());

                                        UserApiClient.getInstance().me((user, error3) -> {
                                            if (error3 != null) {
                                                Log.e(TAG, "사용자 정보 요청 실패", error3);
                                            } else if (user != null) {
                                                Log.i(TAG, "사용자 정보 요청 성공" + "\n회원번호 : " + user.getId() + "\n닉네임 : " + user.getKakaoAccount().getProfile().getNickname());
                                                myId = String.valueOf(user.getId());
                                                db.collection("user")
                                                        .whereEqualTo("id", String.valueOf(user.getId()))
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    if (task.getResult().isEmpty()) {
                                                                        userInfo.put("name", user.getKakaoAccount().getProfile().getNickname());
                                                                        userInfo.put("id", String.valueOf(user.getId()));
                                                                        userInfo.put("storeNum", 0);
                                                                        userInfo.put("store", new ArrayList<DocumentReference>());
                                                                        userInfo.put("favorite", new ArrayList<DocumentReference>());
                                                                        db.collection("user")
                                                                                .document(String.valueOf(user.getId()))
                                                                                .set(userInfo);
                                                                    }
                                                                    editor.putString("myId", myId);
                                                                    editor.apply();
                                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                            }
                                            return null;
                                        });
                                    }
                                    return null;
                                });
                            }
                            return null;
                        });
                    }
                });

            } else if (accessTokenInfo != null) {
                Log.i(TAG, "토큰 정보 보기 성공" + "\n회원 번호 : " + accessTokenInfo.getId());
                myId = String.valueOf(accessTokenInfo.getId());
                editor.putString("myId", myId);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            return null;
        });
    }
}