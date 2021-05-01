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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Calendar;
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
        Log.d("Marker", "on map ready");
        mMap = googleMap;
        LatLng PNU = new LatLng(35.233903, 129.079871);

        setMarkersFromDB();

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