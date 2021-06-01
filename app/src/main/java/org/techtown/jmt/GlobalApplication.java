package org.techtown.jmt;

import android.app.Activity;
import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    private static volatile GlobalApplication instance = null;
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;

        KakaoSdk.init(this,getResources().getString(R.string.kakao_key));
    }
}
