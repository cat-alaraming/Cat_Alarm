package android.cs.pusan.ac.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class showAlbum extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private StorageReference storageRef;
    private CustomImageAdapter mCustomImageAdapter;
    private StaggeredGridLayoutManager manager;
    private Button subscribeButton;
    private Button unsubscribeButton;
    private Spinner spinnerTopics;
    private String topicSelected;

    EditText editText;
    Button btn_search;
    View noInfo;
    RecyclerView mRecyclerView;
    ArrayList<Uri> mArrayUri;
    ArrayList<String> catNames;
    Object[] IndexArray;
    ArrayList<Uri> searchedUri;
    ArrayList<String> searchedUriName;
    boolean searched = false;
    int cnt = 0;

    //구독하는 uid저장 -> 즐겨찾기 구현
    String[] uids;
    long catNum= 0; //favoritesDB 함수에서 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_album);

        mRecyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        btn_search = findViewById(R.id.btn_search);
        noInfo = findViewById(R.id.noInfo);
        noInfo.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        subscribeButton = (Button)findViewById(R.id.subscribeButton);
        unsubscribeButton = (Button)findViewById(R.id.unsubscribeButton);
        spinnerTopics = (Spinner)findViewById(R.id.spinnerTopics);

        mDatabase = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://db-7a416.appspot.com/");
        storageRef = storage.getReference();

        catNames = MainActivity.catNames;

        Log.d("SHOWALBUM", catNames.get(0) + ' ' + catNames.size());
        mArrayUri = new ArrayList<>();
        IndexArray = new Object[catNames.size()];
        cnt = 0;

        manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mCustomImageAdapter = new CustomImageAdapter(2, R.layout.row2, getApplicationContext(), mArrayUri);
        mCustomImageAdapter.setIndexArray(IndexArray);
        mRecyclerView.setAdapter(mCustomImageAdapter);

        // [START bring_topic_spinner]
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, catNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTopics.setAdapter(adapter);
        spinnerTopics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                topicSelected = catNames.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                topicSelected = catNames.get(0);
            }
        });
        // [END bring_topic_spinner]

        // [START subscribe_topics]
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = spinnerTopics.getSelectedItem().toString();
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                        .addOnCompleteListener(task ->{
                            if(task.isSuccessful()){
                                //firestoreDB favorites에 정보 추가
                                add_favoritesDB(topic);
                                Toast.makeText(showAlbum.this, topic + " 구독 성공", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(showAlbum.this, topic + " 구독 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        // [END subscribe_topics]

        // [START unsubscribe_topics]
        unsubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = spinnerTopics.getSelectedItem().toString();
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                        .addOnCompleteListener(task ->{
                            if(task.isSuccessful()){
                                delete_favoritesDB(topic);
                                Toast.makeText(showAlbum.this, topic + " 구독취소 성공", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(showAlbum.this, topic + " 구독취소 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        // [END unsubscribe_topics]

        showRecyclerView();

        editText.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_ENTER ){
                Log.d("IndexArray", "key clicked");
                searchName();
                return true;
            }
            return false;
        });

        btn_search.setOnClickListener(v -> {
            Log.d("IndexArray", "btn clicked"); searchName();});

        mCustomImageAdapter.setOnItemClickListener((view, position) -> {
            Log.d("CLICKED", "clicked " + IndexArray[position]);
            Intent intent1 = new Intent(getApplicationContext(), showCatInfo.class);
            if( searched ){
                intent1.putExtra("catName", searchedUriName.get(position));
            }
            else{
                intent1.putExtra("catName", IndexArray[position].toString());
            }
            startActivity(intent1);
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int[] lastItems = new int[2];
                int totalItemCount = manager.getItemCount();
                manager.findLastCompletelyVisibleItemPositions(lastItems);
                int lastVisible = Math.max(lastItems[0], lastItems[1]);
                if (lastVisible >= totalItemCount - 1) {
                    Log.d("Recycler", "lastVisibled");
                    manager.invalidateSpanAssignments();
                }
            }
        });
    } // End onCreate();

    // [START delete_favoritesDB]
    private void delete_favoritesDB(String topic) {//topic은 고양이 이름

    }
    // [END delete_favoritesDB]


    // [START add_favoritesDB]
    private void add_favoritesDB(String topic){//topic은 고양이 이름
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(showAlbum.this, "즐겨찾기에 추가 실패!.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // [START get_document]
                        DocumentReference docRef = mDatabase.collection("catNamesNums").document("nums");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Map<String, Object> getDB = task.getResult().getData();
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
//                                        catNum[0] = document.get(catName[0]).toString();
//                                        Log.d("favoritesDB", "Document exists" + document.getData());

                                        //catnumcatNamesNums/nums에서 고양이 수 가져오기_start
                                        Object ob;
                                        if( (ob = getDB.get(topic)) != null ){
                                            catNum = (Long)ob;
                                        }
                                        //catnumcatNamesNums/nums에서 고양이 수 가져오기_end

                                        //DB에 저장
                                        Map<String, Object> favorites_list = new HashMap<>();
                                        favorites_list.put("catNum", catNum);
                                        favorites_list.put("catName", topic);

                                        mDatabase.collection("favorites/"+uid+"/favorites_list").document(topic)
                                                .set(favorites_list)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("favorites_list", "DocumentSnapshot written with ID: " + aVoid);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("favorites_list", "Error adding document", e);
                                                    }
                                                });
                                        // Log and toast
//                                        Toast.makeText(showAlbum.this,  "즐겨찾기에 추가 성공!", Toast.LENGTH_SHORT).show();
//                                        Log.d("favoritesDB", "Document exists " + catNum );

                                    } else {
                                        Log.d("favoritesDB", "Document not exists");
                                    }
                                } else {
                                    Log.d("favoritesDB", "get failed with ", task.getException());
                                }
                            }
                        });
                        // [END get_document]
                    }
                });
    }
    // [END favoritesDB]

    /*
    DB에서 대표 이미지 들고 와서 리사이클러뷰 보여주기
     */
    public void showRecyclerView(){
        String docPath = "catNamesNums/nums";
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Map<String, Object> getDB = task.getResult().getData();
                        if( getDB == null ){
                            Log.d("DB Error", "Error get DB no data", task.getException());
                            return;
                        }
                        Object ob;
                        for(int i = 0; i < catNames.size(); i++){
                            String catName = catNames.get(i);
                            long num = 0;
                            if( (ob = getDB.get(catName)) != null ){
                                num = (Long)ob;
                                Log.d("GETURI", catName + "/" + num);
                            }
                            if( num == 0 ){
                                storageRef.child("0.jpg").getDownloadUrl().addOnCompleteListener(task1 -> {
                                    if( task1.isSuccessful() ){
                                        IndexArray[cnt] = catName;
                                        cnt++;
                                        mArrayUri.add(task1.getResult());
                                        Log.d("GETURI!!", "Success "+cnt);
                                        manager.invalidateSpanAssignments();
                                    }
                                    else{
                                        Log.d("GETURI!!", "Fail");
                                    }
                                });
                            }
                            else{
                                storageRef.child(catName + "/" + num + ".jpg").getDownloadUrl().addOnCompleteListener(task1 -> {
                                    if( task1.isSuccessful() ){
                                        IndexArray[cnt] = catName;
                                        cnt++;
                                        mArrayUri.add(task1.getResult());
                                        Log.d("GETURI!!", "Success "+cnt);
                                        manager.invalidateSpanAssignments();
                                    }
                                    else{
                                        Log.d("GETURI!!", "Fail");
                                    }
                                });
                            }
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End showRecyclerView();

    /*
    DB에 별명이 있는지 검색해서 결과 보여줌
     */
    public void searchName(){
        noInfo.setVisibility(View.INVISIBLE);
        String searchName = editText.getText().toString();
        if( searchName.equals("") ){
            mCustomImageAdapter.setArrayUri(mArrayUri);
            mCustomImageAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            searched = false;
            mCustomImageAdapter.setSearched(searched);
            return;
        }
        String docPath = "catNamesNums/names";
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() && task.getResult().getData() != null ){
                        Map<String, Object> getDB = task.getResult().getData();
                        Object ob;
                        if( (ob = getDB.get(searchName)) != null ){
                            String catName = ob.toString();
                            searchedUri = new ArrayList<>();
                            searchedUriName = new ArrayList<>();
                            for(int i = 0; i < IndexArray.length; i++){
                                if( IndexArray[i].toString().equals(catName) ){
                                    searchedUri.add(mArrayUri.get(i));
                                    searchedUriName.add(catName);
                                    searched = true;
                                }
                            }
                            if( searched ){
                                mCustomImageAdapter.setSearched(searched);
                                mCustomImageAdapter.setSearchedArrayUriName(searchedUriName);
                                mCustomImageAdapter.setArrayUri(searchedUri);
                                mCustomImageAdapter.notifyDataSetChanged();
                                mRecyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                        else{
                            noInfo.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.INVISIBLE);
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End searchName();


}
