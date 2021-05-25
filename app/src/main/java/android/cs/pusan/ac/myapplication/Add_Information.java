package android.cs.pusan.ac.myapplication;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Add_Information extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri photoUri;

    Spinner spinner;
    String selected;
    Spinner spinner2;
    String selected2;
    String allNames = "";

    boolean check_camera;

    private FirebaseFirestore mDatabase;
    protected static ArrayList<Uri> mArrayUri;
    ArrayList<String> catNames;



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);

        mDatabase = FirebaseFirestore.getInstance();

        catNames = MainActivity.catNames;

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, catNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = catNames.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected = catNames.get(0);
            }
        });

        EditText editText_name = findViewById(R.id.editText_name);
        EditText editText_features = findViewById(R.id.editText_features);
        spinner2 = findViewById(R.id.spinner2);
        String[] types = {"black1", "black2", "cheese", "godeung", "chaos", "samsaek"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
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
        Button btn_uploadNewCat = findViewById(R.id.btn_uploadNewCat);
        btn_uploadNewCat.setOnClickListener(v -> {
            String getCatName = editText_name.getText().toString();
            String getFeature = editText_features.getText().toString();

            Map<String, Object> data = new HashMap<>();
            data.put("name", getCatName);
            data.put("type", selected2);
            int pm = 1;
            int pm2 = 1;
            if (Math.random() < 0.5) pm = -1;
            if (Math.random() < 0.5) pm2 = -1;
            data.put("latitude", 35.233 + pm * Math.random() * 0.005);
            data.put("longitude", 129.08 + pm2 * Math.random() * 0.005);
            mDatabase.collection("catMarkers").document(getCatName)
                    .set(data)
                    .addOnSuccessListener(documentReference -> Log.d("ADD", "Document added ID: " + getCatName))
                    .addOnFailureListener(e -> Log.d("ADD", "Error adding: ", e));

            data = new HashMap<>();
            data.put("names", getCatName);
            data.put("features", getFeature);
            data.put("num", 0);
            mDatabase.collection("catInfo").document(getCatName)
                    .set(data);
            data = new HashMap<>();
            data.put(getCatName, getCatName);
            mDatabase.collection("catNamesNums").document("names")
                    .set(data, SetOptions.merge());
            data = new HashMap<>();
            data.put(getCatName, 0);
            mDatabase.collection("catNamesNums").document("nums")
                    .set(data, SetOptions.merge());

            mDatabase.document("catNamesNums/names")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> getDB = task.getResult().getData();
                            if (getDB == null) {
                                Log.d("DB Error", "Error get DB no data", task.getException());
                                return;
                            }
                            Object ob;
                            if ((ob = getDB.get("allNames")) != null) {
                                allNames = ob.toString() + "," + getCatName;
                                Log.d("AllNames", "allnames " + allNames);
                                mDatabase.document("catNamesNums/names").update("allNames", allNames);
                            } else {
                                Log.d("AllNames", "Error");
                            }
                        } else {
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    });

            editText_name.setText(null);
            editText_features.setText(null);

        });


        Button btn_uploadCameraImages = findViewById(R.id.btn_uploadCameraImages);
        btn_uploadCameraImages.setOnClickListener((v -> {
            check_camera = true;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    onBackPressed();
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

        }));

        Button btn_uploadImages = findViewById(R.id.btn_uploadImages);
        btn_uploadImages.setOnClickListener(v -> {
            check_camera = false;
            getImgFromAlbum();
        });

    }

    public void getImgFromAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mArrayUri = new ArrayList<>();
        if( check_camera ){
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                mArrayUri.add(photoUri);
                Intent intent1 = new Intent(getApplicationContext(), Add_Photo.class);
                intent1.putExtra("catName", selected);
                intent1.putExtra("check_camera", check_camera);
                startActivity(intent1);
            }
            else{
                Toast.makeText(this, "카메라 취소", Toast.LENGTH_LONG).show();
            }
        }
        else{
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
                Intent intent1 = new Intent(getApplicationContext(), Add_Photo.class);
                intent1.putExtra("catName", selected);
                intent1.putExtra("check_camera", check_camera);
                startActivity(intent1);
            }
            else{
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }


}