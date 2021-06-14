package org.techtown.jmt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.StringValue;
import com.kakao.sdk.user.UserApiClient;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String TAG = "user";
    ImageButton login;
    FirebaseFirestore db;
    String myId;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Map<String,Object> userInfo;
    boolean isUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        userInfo = new HashMap<>();

        UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error2) -> {
            if(error2 != null){
                Log.e(TAG,"토큰 정보 보기 실패",error2);
                setContentView(R.layout.activity_login);

                login = findViewById(R.id.login);
                login.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        UserApiClient.getInstance().loginWithKakaoAccount(getApplicationContext(),(oAuthToken, error) -> {
                            if (error != null) {
                                Log.e(TAG, "로그인 실패", error);
                            } else if (oAuthToken != null) {
                                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());

                                UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error2) -> {
                                    if(error2 != null){
                                        Log.e(TAG,"토큰 정보 보기 실패",error2);
                                    }else if(accessTokenInfo != null){
                                        Log.i(TAG,"토큰 정보 보기 성공" + "\n회원 번호 : " + accessTokenInfo.getId());

                                        UserApiClient.getInstance().me((user, error3) -> {
                                            if(error3 != null){
                                                Log.e(TAG,"사용자 정보 요청 실패",error3);
                                            }else if(user != null){
                                                Log.i(TAG,"사용자 정보 요청 성공" + "\n회원번호 : " + user.getId() + "\n닉네임 : " + user.getKakaoAccount().getProfile().getNickname());
                                                myId = String.valueOf(user.getId());
                                                isUser = db.collection("user")
                                                        .whereEqualTo("id",user.getId())
                                                        .get()
                                                        .getResult()
                                                        .isEmpty();
                                                if(!isUser){
                                                    userInfo.put("name",user.getKakaoAccount().getProfile().getNickname());
                                                    userInfo.put("id",user.getId());
                                                    userInfo.put("storeNum",0);
                                                    userInfo.put("favorite",null);
                                                    db.collection("user")
                                                            .document(String.valueOf(user.getId()))
                                                            .set(userInfo);
                                                }
                                                editor.putString("myId",myId);
                                                editor.apply();
                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                startActivity(intent);
                                                finish();
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

            }else if(accessTokenInfo != null) {
                Log.i(TAG, "토큰 정보 보기 성공" + "\n회원 번호 : " + accessTokenInfo.getId());
                myId = String.valueOf(accessTokenInfo.getId());
                editor.putString("myId",myId);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
            return null;
        });
    }
}