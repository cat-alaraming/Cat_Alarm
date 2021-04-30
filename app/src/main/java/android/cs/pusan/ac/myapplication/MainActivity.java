package android.cs.pusan.ac.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Button LoginBtn;
    private int clickedcnt = 0;
    private String clickedname = "?";

//    not my code
//    not my code
    private FirebaseFirestore mDatabase;
    private ArrayList<Uri> mArrayUri;
    private permissionSupport permission;
    long num = 0;
    public static String[] catNames;
    String allNames = "";

    Spinner spinner;
    String selected;
    Spinner spinner2;
    String selected2;
//    not my code
    //    not my code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginBtn = (Button)findViewById(R.id.login_button2);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setItemIconTintList(null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if( mapFragment != null ){
            mapFragment.getMapAsync(this);
        }
        mDatabase = FirebaseFirestore.getInstance();

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class ));
            }
        });

//        not my code
        // not my code

        permissionCheck();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LOGIN", "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    } else {
                        Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                    }
                });

        //verifyEmailLink();
        mDatabase = FirebaseFirestore.getInstance();


        spinner = findViewById(R.id.spinner);


        EditText editText_name = findViewById(R.id.editText_name);
        EditText editText_features = findViewById(R.id.editText_features);
        spinner2 = findViewById(R.id.spinner2);
        String[] types = {"black1", "black2", "cheese", "godeung", "chaos", "samsaek"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected2 = types[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected2 = types[0];
            }
        });


        String getCatName = editText_name.getText().toString();
        String getFeature = editText_features.getText().toString();


        Map<String, Object> data = new HashMap<>();
        data.put("name", getCatName);
        data.put("type", selected2);
        int pm = 1; int pm2 = 1;
        if( Math.random() < 0.5 ) pm = -1;
        if( Math.random() < 0.5 ) pm2 = -1;
        data.put("latitude", 35.233 + pm * Math.random()*0.005);
        data.put("longitude", 129.08 + pm2 * Math.random()*0.005);
        mDatabase.collection("catinfo")
                .add(data)
                .addOnSuccessListener(documentReference -> Log.d("ADD","Document added ID: "+documentReference.getId()))
                .addOnFailureListener(e -> Log.d("ADD","Error adding: ",e));

        data = new HashMap<>();
        data.put("names", getCatName);
        data.put("features", getFeature);
        data.put("num", 0);
        mDatabase.collection("catIMG").document(getCatName)
                .set(data);
        data = new HashMap<>();
        data.put(getCatName, getCatName);
        mDatabase.collection("catImgNum").document("names")
                .set(data, SetOptions.merge());
        data = new HashMap<>();
        data.put(getCatName, 0);
        mDatabase.collection("catImgNum").document("num")
                .set(data, SetOptions.merge());

        mDatabase.document("catImgNum/names")
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Map<String, Object> getDB = task.getResult().getData();
                        if( getDB == null ){
                            Log.d("DB Error", "Error get DB no data", task.getException());
                            return;
                        }
                        Object ob;
                        if( (ob = getDB.get("allNames")) != null ){
                            allNames = ob.toString() + "," + getCatName;
                            Log.d("AllNames", "allnames " + allNames);
                            mDatabase.document("catImgNum/names").update("allNames", allNames);
                            getAllNames();
                        }
                        else{
                            Log.d("AllNames", "Error");
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
        editText_name.setText(null);
        editText_features.setText(null);



    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.a: //Item의 Id값에 해당하는 것을 누를 시
                    Intent camera = new Intent(getApplicationContext(), Take_Photo.class);
                    startActivity(camera);
                    break;
                case R.id.b: //Item의 Id값에 해당하는 것을 누를 시
                    Intent information = new Intent(getApplicationContext(), Add_Information.class);
                    startActivity(information);
                    break;
                case R.id.c:
                    Intent intent = new Intent(getApplicationContext(), showAlbum.class);
                    startActivity(intent);
                    break;
                case R.id.d:
                    Intent interesting = new Intent(getApplicationContext(), Interesting_Cat.class);
                    startActivity(interesting);
            }
            return true;
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        LatLng PNU = new LatLng(35.233903, 129.079871);

        setMarkersFromDB();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PNU, 15.5f));
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(MainActivity.this);
    } // End onMapReady();

    /*
    마커 2번 클릭된 경우 해당 catInfo로 넘어감
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Marker", "marker clicked");

        if( clickedcnt == 0 ){
            clickedname = marker.getTitle();
            clickedcnt++;
        }
        else if( clickedname.equals(marker.getTitle()) ){
            clickedname = "?";
            clickedcnt = 0;
            Intent intent = new Intent(getApplicationContext(), showCatInfo.class);
            intent.putExtra("catName", marker.getTitle());
            Log.d("Marker", "send intent");
            startActivity(intent);
        }
        else{
            clickedname = marker.getTitle();
        }

        return false;
    } // End onMarkerClick();

    /*
    DB에서 정보 들고 와서 마커 보여주기
     */
    public void setMarkersFromDB(){
        mDatabase.collection("catinfo")
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        String catName = "?"; String type = "?";
                        double latitude = 0.0; double longitude = 0.0;
                        for(QueryDocumentSnapshot document : task.getResult()){
                            Map<String, Object> getDB = document.getData();
                            Object ob;
                            if( (ob = getDB.get("name")) != null ){
                                catName = ob.toString();
                            }
                            if( (ob = getDB.get("type")) != null ){
                                type = ob.toString();
                            }
                            if( (ob = getDB.get("latitude")) != null ){
                                latitude = Double.parseDouble(ob.toString());
                            }
                            if( (ob = getDB.get("longitude")) != null ){
                                longitude = Double.parseDouble(ob.toString());
                            }
                            Log.d("GETDB", catName);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(latitude, longitude))
                                    .title(catName)
                                    .snippet("반가워요")
                                    .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier(type,"drawable",getPackageName())));
                            mMap.addMarker(markerOptions);
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End setMarkersFromDB();

//    DBDBDBDBDBDBDBD
//    DBDBDBDBDBd
    public void getImgFromAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mArrayUri = new ArrayList<>();

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            // Get the Image from data
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                int cnt = mClipData.getItemCount();
                for (int i = 0; i < cnt; i++) {
                    Uri imageuri = mClipData.getItemAt(i).getUri();
                    mArrayUri.add(imageuri);
                }
            }
            else {
                Uri imageuri = data.getData();
                mArrayUri.add(imageuri);
            }
            uploadFile(selected);
        }
        else{
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }

    }

    //upload the file
    private void uploadFile(String catName) {
        if (mArrayUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();

            String docPath = "catIMG/" + catName;
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
                            if( (ob = getDB.get("num")) != null ){
                                num = (Long)ob;
                            }
                            for(int i = 0; i < mArrayUri.size(); i++){
                                Uri filePath = mArrayUri.get(i);
                                String filename = (++num) + ".jpg";
                                StorageReference storageRef = storage.getReferenceFromUrl("gs://catproj.appspot.com/").child( catName + "/" + filename);
                                storageRef.putFile(filePath)
                                        .addOnSuccessListener(taskSnapshot -> Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show())
                                        .addOnProgressListener(taskSnapshot -> {
                                            @SuppressWarnings("VisibleForTests")
                                            double progress = (100f * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        });
                            }
                            mDatabase.document(docPath).update("num", num);
                            mDatabase.document("catImgNum/num").update(catName, num);
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    });
            num = 0;
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    } // End uploadFile()


    public void getAllNames(){
        mDatabase.document("catImgNum/names")
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Map<String, Object> getDB = task.getResult().getData();
                        if( getDB == null ){
                            Log.d("DB Error", "Error get DB no data", task.getException());
                            return;
                        }
                        Object ob;
                        if( (ob = getDB.get("allNames")) != null ){
                            catNames = ob.toString().split(",");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, catNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selected = catNames[position];
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selected = catNames[0];
                            }
                        });
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End getAllNames()


    private void permissionCheck(){
        if( Build.VERSION.SDK_INT >= 23 ){
            permission = new permissionSupport(this, this);
            if( !permission.checkPermission() ){
                permission.requestPermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if( permission.permissionResult(requestCode, permissions, grantResults) ){
            permission.requestPermission();
        }
    }
}