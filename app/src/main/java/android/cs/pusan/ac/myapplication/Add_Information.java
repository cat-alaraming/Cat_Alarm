package android.cs.pusan.ac.myapplication;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Add_Information extends AppCompatActivity {

    Spinner spinner;
    String selected;
    Spinner spinner2;
    String selected2;

    private FirebaseFirestore mDatabase;
    private ArrayList<Uri> mArrayUri;
    long num = 0;
    ArrayList<String> catNames;
    String allNames = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);

        mDatabase = FirebaseFirestore.getInstance();
        Button btn_map = findViewById(R.id.btn_map);
        btn_map.setOnClickListener(v -> {
            onBackPressed();
        });
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
            int pm = 1; int pm2 = 1;
            if( Math.random() < 0.5 ) pm = -1;
            if( Math.random() < 0.5 ) pm2 = -1;
            data.put("latitude", 35.233 + pm * Math.random()*0.005);
            data.put("longitude", 129.08 + pm2 * Math.random()*0.005);
            mDatabase.collection("catMarkers")
                    .add(data)
                    .addOnSuccessListener(documentReference -> Log.d("ADD","Document added ID: "+documentReference.getId()))
                    .addOnFailureListener(e -> Log.d("ADD","Error adding: ",e));

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
                                mDatabase.document("catNamesNums/names").update("allNames", allNames);
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

        });
        Button btn_goToAlbum = findViewById(R.id.btn_goToAlbum);
        btn_goToAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), showAlbum.class);
            startActivity(intent);
        });

        Button btn_uploadImages = findViewById(R.id.btn_uploadImages);
        btn_uploadImages.setOnClickListener(v -> getImgFromAlbum());

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
            uploadFile(selected2);
        }
        else{
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }

    }

    //upload the file
    private void uploadFile(String catName) {
        if (mArrayUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
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
                            if( (ob = getDB.get(catName)) != null ){
                                num = (Long)ob;
                            }
                            for(int i = 0; i < mArrayUri.size(); i++){
                                Uri filePath = mArrayUri.get(i);
                                String filename = (++num) + ".jpg";
                                StorageReference storageRef = storage.getReferenceFromUrl("gs://db-7a416.appspot.com/").child( catName + "/" + filename);
                                storageRef.putFile(filePath)
                                        .addOnSuccessListener(taskSnapshot -> Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show())
                                        .addOnProgressListener(taskSnapshot -> {
                                            @SuppressWarnings("VisibleForTests")
                                            double progress = (100f * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        });
                            }
                            mDatabase.document("catInfo/"+catName).update("num", num);
                            mDatabase.document("catNamesNums/nums").update(catName, num);
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


}