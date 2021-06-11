package org.techtown.jmt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.kakao.sdk.user.UserApiClient;

public class Login extends AppCompatActivity {
    private static final String TAG = "user";
    ImageButton login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }
}