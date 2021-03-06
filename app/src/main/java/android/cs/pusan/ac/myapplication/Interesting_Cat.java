package android.cs.pusan.ac.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class Interesting_Cat extends AppCompatActivity {

    private StorageReference storageRef;
    private FirebaseFirestore mDatabase;
    private String uid;

    String catName;
    long catNum;
    static Uri uri_ = null;

    private ListView listview ;
    private ListViewAdapter adapter;

    BaseApplication base_Activity = new BaseApplication(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_list);

        mDatabase = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //사용자 개별 uid 받아오기

        // Adapter 생성
        adapter = new ListViewAdapter(getApplicationContext()); // 리스트뷰 참조 및 Adapter 달기
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);
//        listview.setOnItemClickListener(listener);

        showallFavorites();
//        favorites_show(uid);
    }
    /*
    Storage에서 가장 최근 고양이 이미지 들고 와서 즐겨찾기 탭에서 사진 보여주기
     */
    public void get_recent_imgUri(String catName){
        // 가장 최근 구독고양이 이미지 파일 가지고 오기
        String cat_name = catName;
        String filename = catNum + ".jpg";

//        uri_ = Uri.parse("https://firebasestorage.googleapis.com/v0/b/db-7a416.appspot.com/o/blackcat%2F6.jpg?alt=media&token=9870cf64-db50-4b47-a04b-70dac1f98df0");
        startProgress();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://db-7a416.appspot.com/");
        storageRef = storage.getReference();
        storageRef.child(cat_name + "/"+ filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                uri_ = uri;
                adapter.addItem( cat_name, uri_ , "");
                adapter.notifyDataSetChanged();
                Log.d("get_recent_imgUri", "Uri : " + uri_);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지 로드 실패시
                Log.d("get_recent_imgUri", catName + "/" + filename + " Fail");
            }
        });
//        Log.d("before_ReturnUri", "Uri : " + uri_);
    }// End show_recent_img();

    public void showallFavorites(){
        // [START get_all_document]
        mDatabase.collection("favorites/"+uid+"/favorites_list")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> getDB = document.getData();

                                Object ob;
                                if( (ob = getDB.get("catName")) != null ){  //DB에서 catName 필드 가져오기
                                    catName = ob.toString();
                                }
                                if( (ob = getDB.get("catNum")) != null ) {  //DB에서 catNum 필드 가져오기
                                    catNum = (Long) ob;
                                }
                                get_recent_imgUri(catName);
//                                Log.d("favor_all", catName + " => " + uri_ );
                            }
//
                        } else {
                            Log.d("favor_all", "Error getting documents: ", task.getException());
                        }
                    }
                });
        // [END get_all_document]
    }

    private void startProgress() {

        progressON("로딩중...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressOFF();
            }
        }, 1800);

    }

    public void progressON() {
        base_Activity.progressON(null);
    }

    public void progressON(String message) {
        base_Activity.progressON(message);
    }

    public void progressOFF() {
        base_Activity.progressOFF();
    }

}
