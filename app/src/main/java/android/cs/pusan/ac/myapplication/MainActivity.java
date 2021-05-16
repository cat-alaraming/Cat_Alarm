package android.cs.pusan.ac.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Button LoginBtn;
    private int clickedcnt = 0;
    private String clickedname = "?";
    public static ArrayList<String> catNames;

    private FirebaseFirestore mDatabase;
    private permissionSupport permission;

    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        mDatabase = FirebaseFirestore.getInstance();
        catNames = new ArrayList<>();
        LoginBtn = findViewById(R.id.login_button2);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setItemIconTintList(null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if( mapFragment != null ){
            mapFragment.getMapAsync(this);
        }

        LoginBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,LoginActivity.class )));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(""); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.view_menu_icon); //뒤로가기 버튼 이미지 지정
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        NavigationView navigationView = findViewById(R.id.navi_View);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.account){
                    Toast.makeText(getApplicationContext(), title + ": 계정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.setting){
                    Toast.makeText(getApplicationContext(), title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.logout){
                    Toast.makeText(getApplicationContext(), title + ": 로그아웃 시도중", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });



        Button btn_addSmall = findViewById(R.id.addSmallMarker);
        btn_addSmall.setOnClickListener(v -> {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "test");
            data.put("type", "white");
            int pm = 1; int pm2 = 1;
            if( Math.random() < 0.5 ) pm = -1;
            if( Math.random() < 0.5 ) pm2 = -1;
            data.put("latitude", 35.233 + pm * Math.random()*0.005);
            data.put("longitude", 129.08 + pm2 * Math.random()*0.005);
            Date currentTime = Calendar.getInstance().getTime();
            String detectedTime = new SimpleDateFormat("yyyy:MM:dd:HH:mm", Locale.getDefault()).format(currentTime);
            data.put("detectedTime", detectedTime);
            mDatabase.collection("catSmallMarkers")
                    .add(data)
                    .addOnSuccessListener(documentReference -> Log.d("ADD","Document added ID: "+documentReference.getId()))
                    .addOnFailureListener(e -> Log.d("ADD","Error adding: ",e));

        });
    }

    @Override
    public void onStop(){
        super.onStop();
        FcmPush.instance.sendMessage("XFzoaEbWRib2KsjtTNOrSJjM0ph2","hi","bye");
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
                    Intent album = new Intent(getApplicationContext(), showAlbum.class);
                    startActivity(album);
                    break;
                case R.id.d:
                    Intent interesting = new Intent(getApplicationContext(), Interesting_Cat.class);
                    startActivity(interesting);
            }
            return true;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d("Marker", "on map ready");
        mMap = googleMap;
        LatLng PNU = new LatLng(35.233903, 129.079871);

        setMarkersFromDB();
//        setSmallMarkersFromDB();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PNU, 15.5f));
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionCheck();
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
        Log.d("Marker", "set marker");
        mDatabase.collection("catMarkers")
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Log.d("Marker", "Successful");
                        String catName = "?"; String type = "?";
                        double latitude = 0.0; double longitude = 0.0;
                        for(QueryDocumentSnapshot document : task.getResult()){
                            Map<String, Object> getDB = document.getData();
                            Object ob;
                            if( (ob = getDB.get("name")) != null ){
                                catName = ob.toString();
                                catNames.add(catName);
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
                            Log.d("Marker Info", catName + " " + type);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(latitude, longitude))
                                    .title(catName)
                                    .snippet("반가워요")
                                    .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier(type,"drawable",getPackageName())));
                            mMap.addMarker(markerOptions);
                        }
                    }
                    else{
                        Log.d("Marker", "Error show DB", task.getException());
                    }
                });
    } // End setMarkersFromDB();


//    /*
//    DB에서 정보 들고 와서 작은 마커 보여주기
//     */
//    public void setSmallMarkersFromDB(){
//        Log.d("SMarker", "set marker");
//        mDatabase.collection("catSmallMarkers")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if( task.isSuccessful() ){
//                        Log.d("SMarker", "Successful");
//                        String catName = "?"; String type = "?"; String detectedTime = "?";
//                        double latitude = 0.0; double longitude = 0.0;
//                        for(QueryDocumentSnapshot document : task.getResult()){
//                            Map<String, Object> getDB = document.getData();
//                            Object ob;
//                            if( (ob = getDB.get("detectedTime")) != null ){
//                                detectedTime = ob.toString();
//                                String dt[] = detectedTime.split(":");
//                                Date currentTime = Calendar.getInstance().getTime();
//                                String cur = new SimpleDateFormat("yyyy:MM:dd:HH:mm", Locale.getDefault()).format(currentTime);
//                                String ct[] = cur.split(":");
//                                if( !dt[0].equals(ct[0]) || !dt[1].equals(ct[1]) || !dt[2].equals(ct[2]) ){
//                                    // 연도, 월, 일이 다른 경우 마커 표시 X
//                                    return;
//                                }
//                                if( Integer.parseInt(ct[3]) - Integer.parseInt(dt[3]) > 2 ) {
//                                    // 시간이 3시간 이상 차이날 경우 마커 표시 X
//                                    return;
//                                }
//                            }
//                            if( (ob = getDB.get("name")) != null ){
//                                catName = ob.toString();
//                                catNames.add(catName);
//                            }
//                            if( (ob = getDB.get("type")) != null ){
//                                type = ob.toString();
//                            }
//                            if( (ob = getDB.get("latitude")) != null ){
//                                latitude = Double.parseDouble(ob.toString());
//                            }
//                            if( (ob = getDB.get("longitude")) != null ){
//                                longitude = Double.parseDouble(ob.toString());
//                            }
//                            Log.d("Marker Info", catName + " " + type);
//                            MarkerOptions markerOptions = new MarkerOptions();
//                            markerOptions.position(new LatLng(latitude, longitude))
//                                    .title(catName)
//                                    .snippet(detectedTime)
//                                    .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier("s"+type,"drawable",getPackageName())));
//                            mMap.addMarker(markerOptions);
//                        }
//                    }
//                    else{
//                        Log.d("Marker", "Error show DB", task.getException());
//                    }
//                });
//    } // End setMarkersFromDB();



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