package org.techtown.jmt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;

public class Logout extends Fragment {
    private static final String TAG = "TAG";
    TextView logout;
    TextView withdraw;
    View v;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_logout, container, false);

        logout = v.findViewById(R.id.logout);
        withdraw = v.findViewById(R.id.withdraw);

        db = FirebaseFirestore.getInstance();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
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
                                        Intent intent = new Intent(getContext(),Login.class);
                                        startActivity(intent);
                                    }
                                    return null;
                                });
                                Toast.makeText(getContext(),"로그아웃 완료",Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(),"로그아웃 취소",Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });

        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
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
                                        Intent intent = new Intent(getContext(),Login.class);
                                        startActivity(intent);

                                    }
                                    return null;
                                });
                                Toast.makeText(getContext(),"탈퇴 완료",Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(),"탈퇴 취소",Toast.LENGTH_SHORT).show();
                    }
                }).show();

            }
        });

        return v;
    }
}