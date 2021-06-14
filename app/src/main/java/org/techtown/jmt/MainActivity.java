package org.techtown.jmt;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

public class MainActivity extends AppCompatActivity {
    Fragment frag_my_list;
    Fragment frag_store_list;
    Fragment frag_user_list;
    Fragment frag_favorite_list;
    String currentTitle = "";

    TextView toolbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 프래그먼트 생성
        frag_my_list = new MyList();
        frag_store_list = new StoreList();
        frag_user_list = new UserList();
        frag_favorite_list = new FavoriteList();

        // 커스텀 액션바 설정
//        getSupportActionBar().setDisplayShowTitleEnabled(false); // 기본 타이틀 사용 안함
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); // 커스텀 사용
//        getSupportActionBar().setCustomView(R.layout.actionbar_custom); // 커스텀 사용할 파일 위치
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar_text = findViewById(R.id.toolbar_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 첫 화면 지정
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_my_list).commit();
        toolbar_text.setText("나의 맛집");
        //setActionbarTitle("나의 맛집"); // 첫 화면에 맞는 타이틀 지정

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
        toolbar_text.setText(title);
        //setActionbarTitle(title);
        currentTitle = title;  // 백업을 위한 flag
    }

    // 액션바 타이틀 변경 메소드
    private void setActionbarTitle(String title)
    {
        View v = getSupportActionBar().getCustomView();
        TextView titleView = (TextView) v.findViewById(R.id.actionbar_custom);
        titleView.setText(title);
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
                toolbar_text.setText("나의 맛집");
                //setActionbarTitle("나의 맛집");
                break;
            case "모두의 맛집":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_list).commit();
                toolbar_text.setText("모두의 맛집");
                //setActionbarTitle("모두의 맛집");
                break;
            case "맛집 킬러":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_user_list).commit();
                toolbar_text.setText("맛집 킬러");
                //setActionbarTitle("맛집 킬러");
                break;
            case "즐겨찾는 리스트":
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_favorite_list).commit();
                toolbar_text.setText("즐겨찾는 리스트");
                //setActionbarTitle("즐겨찾는 리스트");
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

                break;
            case R.id.leave:
                // 탈퇴
                
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