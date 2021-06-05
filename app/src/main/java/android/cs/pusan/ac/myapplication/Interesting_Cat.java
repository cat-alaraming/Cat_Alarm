package android.cs.pusan.ac.myapplication;

import android.net.Uri;
import android.os.Bundle;
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


    ImageView cat_imgView;
    String catName;
    long catNum;
    static Uri uri_ = null;

    private ListView listview ;
    private ListViewAdapter adapter;

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
    public void get_recent_imgUri(){
        // 가장 최근 구독고양이 이미지 파일 가지고 오기
        String filename = catNum + ".jpg";

//        uri_ = Uri.parse("https://firebasestorage.googleapis.com/v0/b/db-7a416.appspot.com/o/blackcat%2F6.jpg?alt=media&token=9870cf64-db50-4b47-a04b-70dac1f98df0");

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://db-7a416.appspot.com/");
        storageRef = storage.getReference();
        storageRef.child(catName + "/"+ filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //이미지 로드 성공시
//                Glide.with(getApplicationContext())
//                        .load(uri)
//                        .into(cat_imgView);
                uri_ = uri;
                adapter.addItem( catName, uri_ , "업데이트 시간");
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
                                get_recent_imgUri();

//                                adapter.addItem( catName, uri_ , "업데이트 시간");
                                Log.d("favor_all", catName + " => " + uri_ );
                            }
//                            adapter.notifyDataSetChanged();
//
                        } else {
                            Log.d("favor_all", "Error getting documents: ", task.getException());
                        }
                    }
                });
        // [END get_all_document]
    }


//    /*
//   DB에서 정보 들고 와서 즐겨찾기 탭에서 구독 고양이 보여주기
//   showCatInfo.java에서 showInfoFromDB 함수 수정
//    */
//    public void favorites_show(String uid){
//        tv_catName = findViewById(R.id.tv_catName); //고양이 이름 보여줄 textView
//        String docPath = "favorites/" + uid;    //DB에 접근
//
//        mDatabase.document(docPath)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if( task.isSuccessful() ){
//                        Map<String, Object> getDB = task.getResult().getData();
//                        if( getDB == null ){
//                            Log.d("favorites_show Error", "Error get DB no data", task.getException());
//                            return;
//                        }
//                        Object ob;
//                        if( (ob = getDB.get("catName")) != null ){  //DB에서 catName 필드 가져오기
//                            catName = ob.toString();
//                            //pick_cat에 있는 텍스트뷰에 이름을 보여주기
//                            tv_catName.setText(catName);
//                        }
//                        Log.d("favorites_show DB", catName+" " );
//
//                        if( (ob = getDB.get("catNum")) != null ){  //DB에서 catNum 필드 가져오기
//                            catNum = (Long)ob;
//                        }
//                        Log.d("favorites_show DB", "가장 최근 고양이 사진 no."+ catNum );
//
////                        if( catNum > 0 ){
////                            noInfo.setVisibility(View.INVISIBLE);
////                            mRecyclerView.setVisibility(View.VISIBLE);
////                        }
//
//                        show_recent_img();
//                    }
//                    else{
//                        Log.d("SHOW", "Error show DB", task.getException());
//                    }
//                });
//
//
//    } // End favorites_show();


}
