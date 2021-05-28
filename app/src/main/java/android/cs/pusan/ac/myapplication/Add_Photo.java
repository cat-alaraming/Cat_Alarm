package android.cs.pusan.ac.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class Add_Photo extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private permissionSupport permission;
    ArrayList<Uri> mArrayUri;
    ArrayList<Boolean> mArrayIsOpenCV;
    long num = 0;
    CascadeClassifier faceDetector;
    boolean check_camera = false;
    Long mLastClickTime = 0L;

    LinearLayout imageSpace;
    TextView tv_result;
    int mArrayUriSize = 0;
    String catName;

    public static Map<String, String> namesAndTypes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        mArrayUri = new ArrayList<>();
        namesAndTypes = MainActivity.namesAndTypes;
        Intent intent = getIntent();
        int classNum = intent.getIntExtra("class", 0);
        catName = intent.getStringExtra("catName");
        if (classNum == 1) {
            check_camera = intent.getBooleanExtra("check_camera", false);
            mArrayUri.addAll(Add_Information.mArrayUri);
        } else {
            mArrayUri.addAll(editInfo.sendMArrayUri);
        }

        Log.d("NOWHERE", "Add_Photo");
        mArrayUriSize = mArrayUri.size();
        mArrayIsOpenCV = new ArrayList<>();
        for (int i = 0; i < mArrayUri.size(); i++) {
            mArrayIsOpenCV.add(false);
        }

        mDatabase = FirebaseFirestore.getInstance();
        imageSpace = findViewById(R.id.images);
        tv_result = findViewById(R.id.tv_result);


        createImageView();

        setResult();


        OpenCVLoader.initDebug();

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback);
        } else {
            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button btn_opencv = findViewById(R.id.btn_opencv);
        btn_opencv.setOnClickListener(v -> {

            Log.d("ASDFASDF", String.valueOf(mLastClickTime));
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                Log.d("ASDFASDF", "isDoing");
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            tv_result.setText("running....");

            new Thread(() -> runOnUiThread(() -> {
                for (int i = 0; i < mArrayUri.size(); i++) {
                    Log.d("ASDFASDF", String.valueOf(i));
                    Uri imageuri = mArrayUri.get(i);
                    if (imageuri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageuri);
                            mArrayIsOpenCV.set(i, imageprocess(bitmap, i));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setResult();
            })).start();

        });

        Button btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> onBackPressed());
        Button btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(v -> {
            for (Boolean b : mArrayIsOpenCV) {
                if (!b) {
                    Toast.makeText(getApplicationContext(), "고양이가 인식되지 않은 사진이 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            uploadFile(catName);
            onBackPressed();
        });

    }


    public void createImageView() {
        Log.d("createImageView", "Add_Photo");
        for (int i = 0; i < mArrayUri.size(); i++) {
            Log.d("createImageView", "Add_Photo");
            ImageView imageView = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(convertDPtoPX(300), convertDPtoPX(300));
            imageView.setLayoutParams(param);
            imageView.setTag("iv" + i);
            imageView.setOnLongClickListener(v -> {
                int index = Integer.parseInt(imageView.getTag().toString().replaceAll("[^0-9]", ""));
                mArrayUri.set(index, null);
                mArrayIsOpenCV.set(index, true);
                imageView.setVisibility(View.GONE);
                setResult();
                mArrayUriSize--;
                if (mArrayUriSize == 0) {
                    onBackPressed();
                }
                return true;
            });
            Glide.with(getApplicationContext()).load(mArrayUri.get(i)).transform(new CenterCrop()).into(imageView);
            imageSpace.addView(imageView);
        }
    }

    public int convertDPtoPX(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public void setResult() {
        String results = "";
        for (int i = 0; i < mArrayUri.size(); i++) {
            if (mArrayUri.get(i) != null) {
                if (mArrayIsOpenCV.get(i)) {
                    results += " O";
                } else {
                    results += " X";
                }
            }
        }
        tv_result.setText(results);
    }


    public boolean imageprocess(Bitmap catBitmap, int tagNum) throws IOException {

        boolean ret;
        Bitmap albumImg;

        Matrix rotateMatrix = new Matrix();
        //찍힌 사진이 정방향이 아니여서 90도로 회전시킴 //회전을 안시키니까 고양이 인식이 안됨
        if (check_camera) {
            rotateMatrix.postRotate(90);
        } else {
            rotateMatrix.postRotate(0);
        }
        albumImg = Bitmap.createBitmap(catBitmap, 0, 0,
                catBitmap.getWidth(), catBitmap.getHeight(), rotateMatrix, false);


        //기존 이미지에 고양이가 확인되면 color위에 사각형을 그림
        Mat color = new Mat();
        Utils.bitmapToMat(albumImg, color);

        //기존 이미지를 흑백으로 바꾸어서 catfacedetect가 좀더 수월하게 함
        Mat gray = new Mat();
        Utils.bitmapToMat(albumImg, gray);
        Imgproc.cvtColor(gray, gray, Imgproc.COLOR_RGBA2GRAY);

        //고양이 detect with 흑백이미지
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(gray, faceDetections);

        if (faceDetections.empty()) {
            ret = false;
        } else {
            ret = true;
            //고양이 얼굴에 사각형 생성 with color 이미지
            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(color, new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0),
                        20);
            }

            //imageView에 고양이 인식한 사진 올리기
            Utils.matToBitmap(color, albumImg);
            ImageView iv = imageSpace.findViewWithTag("iv" + tagNum);
            iv.setImageBitmap(albumImg);
        }

        return ret;
    }

    private BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalcatface_extended);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    File cascFile = new File(cascadeDir, "haarcascade_frontalcatface_extended.xml");

                    FileOutputStream fos = new FileOutputStream(cascFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    is.close();
                    fos.close();

                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());
                    if (faceDetector.empty()) {
                        faceDetector = null;
                    } else {
                        cascadeDir.delete();
                    }
                }
                break;

                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    //upload the file
    private void uploadFile(String catName) {

        if( check_camera ){
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionCheck();
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Log.d("LOCATIONTEST", "위도: " + String.valueOf(latitude) + ", 경도: " + String.valueOf(longitude));


                                Map<String, Object> data = new HashMap<>();
                                data.put("name", catName);
                                data.put("type", namesAndTypes.get(catName));
                                data.put("latitude", latitude);
                                data.put("longitude", longitude);
                                Date currentTime = Calendar.getInstance().getTime();
                                String yyyyMM = new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(currentTime);
                                String dd = new SimpleDateFormat("dd", Locale.getDefault()).format(currentTime);
                                String detectedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime);
                                data.put("detectedTime", detectedTime);

                                Map<String, Object> newDoc = new HashMap<>();
                                newDoc.put("date", yyyyMM);
                                mDatabase.document("catSmallMarkers/" + yyyyMM)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if( task.isSuccessful() ){
                                                Map<String, Object> getDB = task.getResult().getData();
                                                if( getDB == null ){
                                                    Log.d("DB Error", "Error get DB no data", task.getException());
                                                    mDatabase.document("catSmallMarkers/" + yyyyMM)
                                                            .set(newDoc)
                                                            .addOnSuccessListener(documentReference -> Log.d("ADD","new Doc"))
                                                            .addOnFailureListener(e -> Log.d("ADD","Error adding: ",e));
                                                }
                                            }
                                            else{
                                                Log.d("SHOW", "Error show DB", task.getException());
                                            }
                                        });
                                mDatabase.collection("catSmallMarkers/" + yyyyMM + "/" + dd)
                                        .add(data)
                                        .addOnSuccessListener(documentReference -> Log.d("ADD","Document added ID: "+yyyyMM))
                                        .addOnFailureListener(e -> Log.d("ADD","Error adding: ",e));
                            }
                        }
                    });
        }

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
                                if( filePath == null ) continue;
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
                            onBackPressed();

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

    private void permissionCheck(){
        if( Build.VERSION.SDK_INT >= 23 ){
            permission = new permissionSupport(this, this);
            if( !permission.checkPermission() ){
                permission.requestPermission();
            }
        }
    }
}
