package org.techtown.jmt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.sdk.user.UserApiClient;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreList extends Fragment {
    private static final String TAG = "TAG";
    private Context mContext;
    FirebaseFirestore db;
    private Spinner category_spinner;
    private Spinner do_spinner;
    private Spinner si_spinner;
    private ArrayAdapter categoryAdapter;
    private ArrayAdapter locationAdapter;
    RecyclerView recyclerView;
    Fragment frag_store_detail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store_list, container, false);

        frag_store_detail = new StoreDetail();

        recyclerView = v.findViewById(R.id.store_name_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 스피너(카테고리) 구현
        category_spinner = (Spinner)v.findViewById(R.id.category);
        categoryAdapter = ArrayAdapter.createFromResource(mContext, R.array.categories_all, R.layout.support_simple_spinner_dropdown_item);
        category_spinner.setAdapter(categoryAdapter);

        // 스피너(도, 시) 구현
        do_spinner = (Spinner)v.findViewById(R.id.do_spinner);
        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_do, R.layout.support_simple_spinner_dropdown_item);
        do_spinner.setAdapter(locationAdapter);

        si_spinner = (Spinner)v.findViewById(R.id.si_spinner);
        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.nothing, R.layout.support_simple_spinner_dropdown_item);
        si_spinner.setAdapter(locationAdapter);

        // 카테고리별 분류
        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                String selectedCategory = category_spinner.getSelectedItem().toString();
                String selectedDo = do_spinner.getSelectedItem().toString();
                String selectedSi = si_spinner.getSelectedItem().toString();

                if(selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 모든 카테고리, 전체 지역
                    DBAdapter_all(adapter);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 전체 시
                    DBAdapter_do(adapter, selectedDo);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 세부 시
                    DBAdapter_do_si(adapter, selectedDo, selectedSi);
                } else if(!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 세부 카테고리, 전체 지역
                    DBAdapter_category(adapter, selectedCategory);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 전체 시
                    DBAdapter_category_do(adapter, selectedCategory, selectedDo);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 세부 시
                    DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
                }
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnStoreItemClickListener() {
                    @Override
                    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
                        StoreInfo item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name", item.getStoreName());
                        bundle.putString("location", item.getLocation());
                        Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                String selectedCategory = category_spinner.getSelectedItem().toString();
                String selectedDo = do_spinner.getSelectedItem().toString();
                String selectedSi = si_spinner.getSelectedItem().toString();

                if(selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 모든 카테고리, 전체 지역
                    DBAdapter_all(adapter);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 전체 시
                    DBAdapter_do(adapter, selectedDo);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 세부 시
                    DBAdapter_do_si(adapter, selectedDo, selectedSi);
                } else if(!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 세부 카테고리, 전체 지역
                    DBAdapter_category(adapter, selectedCategory);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 전체 시
                    DBAdapter_category_do(adapter, selectedCategory, selectedDo);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 세부 시
                    DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
                }
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnStoreItemClickListener() {
                    @Override
                    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
                        StoreInfo item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name", item.getStoreName());
                        bundle.putString("location", item.getLocation());
                        Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
                    }
                });
            }
        });

        // 도 선택시 각각에 알맞은 시 스피너 노출
        do_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                String selectedCategory = category_spinner.getSelectedItem().toString();
                String selectedDo = do_spinner.getSelectedItem().toString();
                String selectedSi = si_spinner.getSelectedItem().toString();

                if(selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 모든 카테고리, 전체 지역
                    DBAdapter_all(adapter);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 전체 시
                    DBAdapter_do(adapter, selectedDo);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 세부 시
                    DBAdapter_do_si(adapter, selectedDo, selectedSi);
                } else if(!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 세부 카테고리, 전체 지역
                    DBAdapter_category(adapter, selectedCategory);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 전체 시
                    DBAdapter_category_do(adapter, selectedCategory, selectedDo);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 세부 시
                    DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
                }
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnStoreItemClickListener() {
                    @Override
                    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
                        StoreInfo item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name", item.getStoreName());
                        bundle.putString("location", item.getLocation());
                        Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
                    }
                });

                switch(i){
                    case 1:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_seoul, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 2:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_busan, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 3:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_daegu, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 4:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_incheon, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 5:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_gwangju, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 6:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_daejeon, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 7:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_ulsan, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 8:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_sejong, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 9:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_gyunggi, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 10:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_gangwon, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 11:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_chungbuk, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 12:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_chungnam, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 13:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_jeonbuk, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 14:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_jeonnam, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 15:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_gyungbuk, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 16:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_gyungnam, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    case 17:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.location_si_jeju, R.layout.support_simple_spinner_dropdown_item);
                        break;
                    default:
                        locationAdapter = ArrayAdapter.createFromResource(mContext, R.array.nothing, R.layout.support_simple_spinner_dropdown_item);
                        break;
                }
                si_spinner.setAdapter(locationAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                String selectedCategory = category_spinner.getSelectedItem().toString();
                String selectedDo = do_spinner.getSelectedItem().toString();
                String selectedSi = si_spinner.getSelectedItem().toString();

                if(selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 모든 카테고리, 전체 지역
                    DBAdapter_all(adapter);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 전체 시
                    DBAdapter_do(adapter, selectedDo);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 세부 시
                    DBAdapter_do_si(adapter, selectedDo, selectedSi);
                } else if(!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 세부 카테고리, 전체 지역
                    DBAdapter_category(adapter, selectedCategory);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 전체 시
                    DBAdapter_category_do(adapter, selectedCategory, selectedDo);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 세부 시
                    DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
                }
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnStoreItemClickListener() {
                    @Override
                    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
                        StoreInfo item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name", item.getStoreName());
                        bundle.putString("location", item.getLocation());
                        Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
                    }
                });
                // @@@@@@@@@@@@@@@@@@@@@@@ 가능하면 GPS 기능 여기에 넣으면 좋을 듯. 자동 설정 되도록
            }
        });

        // 시 선택시 각각에 알맞은 데이터 표시
        si_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                String selectedCategory = category_spinner.getSelectedItem().toString();
                String selectedDo = do_spinner.getSelectedItem().toString();
                String selectedSi = si_spinner.getSelectedItem().toString();

                if(selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 모든 카테고리, 전체 지역
                    DBAdapter_all(adapter);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 전체 시
                    DBAdapter_do(adapter, selectedDo);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 세부 시
                    DBAdapter_do_si(adapter, selectedDo, selectedSi);
                } else if(!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 세부 카테고리, 전체 지역
                    DBAdapter_category(adapter, selectedCategory);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 전체 시
                    DBAdapter_category_do(adapter, selectedCategory, selectedDo);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 세부 시
                    DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
                }
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnStoreItemClickListener() {
                    @Override
                    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
                        StoreInfo item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name", item.getStoreName());
                        bundle.putString("location", item.getLocation());
                        Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                StoreAdapter adapter = new StoreAdapter(mContext);
                String selectedCategory = category_spinner.getSelectedItem().toString();
                String selectedDo = do_spinner.getSelectedItem().toString();
                String selectedSi = si_spinner.getSelectedItem().toString();

                if(selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 모든 카테고리, 전체 지역
                    DBAdapter_all(adapter);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 전체 시
                    DBAdapter_do(adapter, selectedDo);
                } else if(selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 모든 카테고리, 세부 도, 세부 시
                    DBAdapter_do_si(adapter, selectedDo, selectedSi);
                } else if(!selectedCategory.equals("모두") && selectedDo.equals("전체")) {
                    // 세부 카테고리, 전체 지역
                    DBAdapter_category(adapter, selectedCategory);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 전체 시
                    DBAdapter_category_do(adapter, selectedCategory, selectedDo);
                } else if(!selectedCategory.equals("모두") && !selectedDo.equals("전체") && !selectedSi.equals("전체")) {
                    // 세부 카테고리, 세부 도, 세부 시
                    DBAdapter_category_do_si(adapter, selectedCategory, selectedDo, selectedSi);
                }
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnStoreItemClickListener() {
                    @Override
                    public void onItemClick(StoreAdapter.ViewHolder holder, View view, int position) {
                        StoreInfo item = adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("store_name", item.getStoreName());
                        bundle.putString("location", item.getLocation());
                        Log.d(TAG,"send store_name is " + bundle.getString("store_name"));
                        getParentFragmentManager().setFragmentResult("requestKey",bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.main_layout, frag_store_detail).addToBackStack(null).commit();
                    }
                });
            }
        });

        return v;
    }

    public void DBAdapter_category_do_si(StoreAdapter adapter, String category, String do_name, String si_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .whereEqualTo("do", do_name)
                .whereEqualTo("si", si_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void DBAdapter_category_do(StoreAdapter adapter, String category, String do_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .whereEqualTo("do", do_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void DBAdapter_do_si(StoreAdapter adapter, String do_name, String si_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("do", do_name)
                .whereEqualTo("si", si_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void DBAdapter_do(StoreAdapter adapter, String do_name) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("do", do_name)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void DBAdapter_category(StoreAdapter adapter, String category) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .whereEqualTo("category", category)
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void DBAdapter_all(StoreAdapter adapter) {
        db = FirebaseFirestore.getInstance();
        db.collection("store")
                .orderBy("lover", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG,"가게 정보" + document.getData());
                                adapter.addItem(new StoreInfo(document.getString("name"), (Long) document.get("lover"), document.getString("location")));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    // 프래그먼트는 context를 바로 가져올 수 없음. getActivity 또는 getContext는 종종 Null을 가져오므로 안전한 코드 다음과 같이 작성
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

}