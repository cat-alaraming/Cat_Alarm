package android.cs.pusan.ac.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
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
    private int clickedcnt = 0;
    private String clickedname = "?";
    public static ArrayList<String> catNames;
    public static Map<String, String> namesAndTypes;

    private FirebaseFirestore mDatabase;
    private permissionSupport permission;

    private DrawerLayout mDrawerLayout;
    private TextView tvEmailId;
    private Boolean checking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        mDatabase = FirebaseFirestore.getInstance();
        catNames = new ArrayList<>();
        namesAndTypes = new HashMap<>();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setItemIconTintList(null);

        //네비드로어 헤더 현재 계정 보여주기
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if( mapFragment != null ){
            mapFragment.getMapAsync(this);
        }

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
        Menu menu = navigationView.getMenu();
        MenuItem logoutItem = menu.findItem(R.id.logout);
        if (firebaseUser != null){
            logoutItem.setVisible(true);
        } else{
            logoutItem.setVisible(false);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                FirebaseUser user = firebaseAuth.getCurrentUser();
                Menu menu = navigationView.getMenu();
                MenuItem logoutItem = menu.findItem(R.id.logout);

                if(id == R.id.account){
                    if (user != null){
                        Toast.makeText(getApplicationContext(), "이미 로그인이 되어있습니다.", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(getApplicationContext(), title + ": 계정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class ));
                    }
                }
                else if(id == R.id.setting){
                    Intent setting = new Intent(getApplicationContext(), setting.class);
                    startActivity(setting);
                    Toast.makeText(getApplicationContext(), title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.logout){
                    if (user != null){
                        firebaseAuth.signOut();
                        tvEmailId.setText("로그인 해주세요");
                        Toast.makeText(getApplicationContext(), title + ": 로그아웃 완료", Toast.LENGTH_SHORT).show();
                        logoutItem.setVisible(false);
                    } else{
                        Toast.makeText(getApplicationContext(), "로그인 안되어 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
        smallMarkerChecking();

        //네비드로어 헤더 현재 계정 보여주기
        View header = navigationView.getHeaderView(0);
        tvEmailId = (TextView)header.findViewById(R.id.tv_email_id);
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null){
            tvEmailId.setText(firebaseUser.getEmail());
        } else{
            tvEmailId.setText("로그인 해주세요");
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.navi_menu2, menu);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        smallMarkerChecking();
    }

    public void smallMarkerChecking(){
        SharedPreferences pref = getSharedPreferences("Setting",0);
        checking = pref.getBoolean("checking",true);
        if(checking == true){
            Button btn_addSmall = findViewById(R.id.addSmallMarker);
            btn_addSmall.setVisibility(View.VISIBLE);
            btn_addSmall.setOnClickListener(v -> {
                Intent intent1 = new Intent(getApplicationContext(), addSmallMarkers.class);
                startActivity(intent1);
            });
            if(mMap != null){
                mMap.clear();
                setMarkersFromDB();
                setSmallMarkersFromDB();
            }
        } else{
            Button btn_addSmall = findViewById(R.id.addSmallMarker);
            btn_addSmall.setVisibility(View.GONE);
            if(mMap != null){
                mMap.clear();
                setMarkersFromDB();
            }

        }
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.a: //Item의 Id값에 해당하는 것을 누를 시
                    Intent information = new Intent(getApplicationContext(), Add_Information.class);
                    startActivity(information);
                    break;
                case R.id.b: //Item의 Id값에 해당하는 것을 누를 시
                    Intent album = new Intent(getApplicationContext(), showAlbum.class);
                    startActivity(album);
                    break;
                case R.id.c:
                    Intent interesting = new Intent(getApplicationContext(), Interesting_Cat.class);
                    startActivity(interesting);
                    break;
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
            case R.id.renew:{
                smallMarkerChecking();
                Toast.makeText(getApplicationContext(),  " 지도 새로고침", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        Log.d("Marker", "on map ready");
        mMap = googleMap;
        LatLng PNU = new LatLng(35.233903, 129.079871);

        smallMarkerChecking();

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
        if( marker.getTitle().equals("??") ){
            clickedcnt = 0;
            return false;
        }
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
                                if( !(catNames.contains(catName)) ) {
                                    catNames.add(catName);
                                }
                            }
                            if( (ob = getDB.get("type")) != null ){
                                type = ob.toString();
                                namesAndTypes.put(catName, type);
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


    /*
    DB에서 정보 들고 와서 작은 마커 보여주기
     */
    public void setSmallMarkersFromDB(){
        Log.d("SMarker", "set marker");

        Date currentTime = Calendar.getInstance().getTime();
        String yyyyMM = new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(currentTime);
        String dd = new SimpleDateFormat("dd", Locale.getDefault()).format(currentTime);
        mDatabase.collection("catSmallMarkers/" + yyyyMM + "/" + dd)
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Log.d("SMarker", "Successful");
                        String catName = "?"; String type = "?"; String detectedTime = "?";
                        double latitude = 0.0; double longitude = 0.0;
                        for(QueryDocumentSnapshot document : task.getResult()){
                            Log.d("MarkerInfo", document.getId());
                            Map<String, Object> getDB = document.getData();
                            Object ob;
                            int time = 0;
                            if( (ob = getDB.get("detectedTime")) != null ){
                                detectedTime = ob.toString();
                                String dt[] = detectedTime.split(":");
                                String cur = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime);
                                String ct[] = cur.split(":");
                                time = (Integer.parseInt(ct[0]) - Integer.parseInt(dt[0])) * 60 + Integer.parseInt(ct[1]) - Integer.parseInt(dt[1]);
                                Log.d("MarkerInfo", String.valueOf(time));
                                if( time >= 180 ) {
                                    // 시간이 3시간 이상 차이날 경우 마커 표시 X
                                    continue;
                                }
                            }
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
                            Log.d("MarkerInfo", catName + " " + type);
                            MarkerOptions markerOptions = new MarkerOptions();
                            Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), getApplicationContext().getResources().getIdentifier("s"+type,"drawable",getPackageName()));
                            bm = Bitmap.createScaledBitmap(bm, convertDPtoPX((int)(10*(180-time)/180.0+30)), convertDPtoPX((int)(10*(180-time)/180.0+30)), false);
                            markerOptions.position(new LatLng(latitude, longitude))
                                    .title(catName)
                                    .snippet(detectedTime)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bm));
                            mMap.addMarker(markerOptions);
                        }
                    }
                    else{
                        Log.d("Marker", "Error show DB", task.getException());
                    }
                });
    } // End setMarkersFromDB();

    public int convertDPtoPX(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }


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