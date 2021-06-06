package android.cs.pusan.ac.myapplication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class showCatInfo extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private StorageReference storageRef;
    String uid;
    private CustomImageAdapter mCustomImageAdapter;
    private StaggeredGridLayoutManager manager;
    private Button subscribeButton;
    private Button unsubscribeButton;

    TextView textViewName;
    TextView textViewFeatures;
    Button btn_goMain;
    Button btn_edit;
    RecyclerView mRecyclerView;
    View noInfo;
    View layout1;
    View layout2;
    LinearLayout LL_comments;
    View btns;
    PhotoView photoView;
    Button btn_left;
    Button btn_right;
    ImageButton btn_download;

    ArrayList<Uri> mArrayUri;
    long num;
    String names;
    String features;
    String catName;
    int nowPos;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);

        Log.d("CatInfo", "get intent");
        Intent intent = getIntent();
        catName = intent.getStringExtra("catName");
        builder = new AlertDialog.Builder(this);

        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mDatabase = FirebaseFirestore.getInstance();

        num = 0;
        nowPos = 0;
        setLayout1(catName);
        setLayout2();

//        subscribeButton = (Button)findViewById(R.id.subscribeButton);
//        unsubscribeButton = (Button)findViewById(R.id.unsubscribeButton);

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://db-7a416.appspot.com/");
        storageRef = storage.getReference();

        showInfoFromDB(catName);

    } // End onCreate();

    public void setLayout1(String catName){
        mRecyclerView = findViewById(R.id.recyclerView);
        noInfo = findViewById(R.id.noInfo);
        textViewName = findViewById(R.id.show_name);
        textViewFeatures = findViewById(R.id.show_features);
        btn_goMain = findViewById(R.id.btn_goMain);
        btn_edit = findViewById(R.id.btn_edit);
        photoView = findViewById(R.id.photoView);
        layout1 = findViewById(R.id.layout1);
        LL_comments = findViewById(R.id.LL_comments);

        textViewName.setText(catName);
        textViewName.setMovementMethod(new ScrollingMovementMethod());
        textViewFeatures.setMovementMethod(new ScrollingMovementMethod());
        noInfo.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        mArrayUri = new ArrayList<>();
        manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mCustomImageAdapter = new CustomImageAdapter(1, R.layout.row, getApplicationContext(), mArrayUri);
        mRecyclerView.setAdapter(mCustomImageAdapter);

        btn_goMain.setOnClickListener(v -> onBackPressed());
        btn_edit.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), editInfo.class);
            intent1.putExtra("names", names);
            intent1.putExtra("features", features);
            startActivity(intent1);
        });

        mCustomImageAdapter.setOnItemClickListener((view, position) -> {
            Log.d("CLICKED", "clicked");
            nowPos = position;
            Glide.with(getApplicationContext()).load(mArrayUri.get(nowPos)).diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
            layout1.setVisibility(View.INVISIBLE);
            layout2.setVisibility(View.VISIBLE);
            setBtnVisibility();
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int[] lastItems = new int[2];
                int totalItemCount = manager.getItemCount();
                manager.findLastCompletelyVisibleItemPositions(lastItems);
                int lastVisible = Math.max(lastItems[0], lastItems[1]);
                if( lastVisible >= totalItemCount - 1 ){
                    Log.d("Recycler", "lastVisibled " + lastVisible);
                    manager.invalidateSpanAssignments();
                }
            }
        });


        String docPath = "catInfo/" + catName + "/comments";
        mDatabase.collection(docPath).orderBy("when", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Log.d("SetComments", "Successful");
                        String who = "?"; String what = "?"; String when = "?"; String commentUID = "?";
                        for(QueryDocumentSnapshot document : task.getResult()){
                            String docID = document.getId();
                            Log.d("SetComments", docID);
                            Map<String, Object> getDB = document.getData();
                            Object ob;
                            if( (ob = getDB.get("who")) != null ){
                                who = ob.toString();
                            }
                            if( (ob = getDB.get("what")) != null ){
                                what = ob.toString();
                            }
                            if( (ob = getDB.get("when")) != null ){
                                when = ob.toString();
                            }
                            if( (ob = getDB.get("uid")) != null ){
                                commentUID = ob.toString();
                            }
                            if( commentUID.equals(uid) ){
                                createComment(who, what, when, 1, docID);
                            }
                            else{
                                createComment(who, what, when, 0, "");
                            }
                        }
                        createEditView(LL_comments);
                    }
                    else{
                        Log.d("Marker", "Error show DB", task.getException());
                    }
                });

    }

    public void createComment(String who, String what, String when, int isMyComment, String docID){
        LinearLayout commentBox = new LinearLayout(this);
        commentBox.setOrientation(LinearLayout.VERTICAL);

        commentBox.setTag(docID);
        if( isMyComment == 1 ){
            commentBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    Log.d("ClickedMyComment", docID);

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("ClickedMyComment", "확인");
                            mDatabase.collection("catInfo/" + catName + "/comments").document(docID)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("ClickedMyComment", "DocumentSnapshot successfully deleted!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("ClickedMyComment", "Error deleting document", e);
                                        }
                                    });
                            commentBox.setVisibility(View.GONE);
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("ClickedMyComment", "취소");
                        }
                    });
                    //builder.setIcon(R.drawable.ic_launcher);

                    builder.setTitle("댓글을 지우시겠습니까?");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            });
        }

        commentBox.addView(createTextView(who, 1, isMyComment));
        commentBox.addView(createTextView(what, 2, isMyComment));
        commentBox.addView(createTextView(when, 3, isMyComment));
        LL_comments.addView(commentBox);
    }

    public int convertDPtoPX(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public TextView createTextView(String value, int num, int isMyComment){
        TextView textView = new TextView(getApplicationContext());
        textView.setText(value);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if( num == 1 ){
            if( isMyComment == 1 ){
                textView.setTextColor(Color.parseColor("#FF9800"));
            }
            else{
                textView.setTextColor(Color.parseColor("#000000"));
            }
            textView.setTextSize(13);
            param.topMargin = convertDPtoPX(5);
        }
        else if( num == 2 ){
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setTextSize(15);
            param.leftMargin = convertDPtoPX(2);
        }
        else{
            textView.setTextColor(Color.parseColor("#9F9F9F"));
            textView.setTextSize(10);
            param.bottomMargin = convertDPtoPX(5);
        }
        textView.setLayoutParams(param);
        textView.setOnClickListener(v -> {
            ;
        });
        return textView;
    }

    public void createEditView(LinearLayout linearLayout){
        EditText editText = new EditText(getApplicationContext());
        editText.setHint("댓글을 입력하세요");
        editText.setEms(20);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(param);

        editText.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_ENTER ){
                String who = "익명";
                String what = editText.getText().toString();
                if( what.equals("") ) return true;
                Date currentTime = Calendar.getInstance().getTime();
                String when = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(currentTime);
                editText.setVisibility(View.GONE);

                Map<String, Object> data = new HashMap<>();
                data.put("who", who);
                data.put("what", what);
                data.put("when", when);
                data.put("uid", uid);

                mDatabase.collection("catInfo/" + catName + "/comments")
                        .add(data)
                        .addOnSuccessListener(documentReference -> {
                            createComment(who, what, when, 1, documentReference.getId());
                            createEditView(linearLayout);
                        })
                        .addOnFailureListener(e -> Log.d("ADD","Error adding: ",e));
                return true;
            }
            return false;
        });

        linearLayout.addView(editText);
    }

    public void setLayout2(){
        layout2 = findViewById(R.id.layout2);
        btns = findViewById(R.id.btns);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_download = findViewById(R.id.btn_download);

        btn_left.setOnClickListener(v -> {
            if( layout2.getVisibility() == View.VISIBLE && nowPos > 0 ){
                nowPos--;
                Glide.with(getApplicationContext()).load(mArrayUri.get(nowPos)).diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
                setBtnVisibility();
            }
        });
        btn_right.setOnClickListener(v -> {
            if( layout2.getVisibility() == View.VISIBLE && nowPos < mArrayUri.size() - 1 ){
                nowPos++;
                Glide.with(getApplicationContext()).load(mArrayUri.get(nowPos)).diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
                setBtnVisibility();
            }
        });
        btn_download.setOnClickListener(v -> {
            Glide.with(getApplicationContext()).asBitmap().load(mArrayUri.get(nowPos))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            try {
                                Date currentTime = Calendar.getInstance().getTime();
                                String date_text = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(currentTime);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    saveBitmap(getApplicationContext(), resource, Bitmap.CompressFormat.JPEG, "image/jpeg", "Cat-"+date_text+".jpg");
                                }
                                else{
                                    saveBitmapToJpeg(resource, date_text);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        });
        photoView.setOnClickListener(v -> {
            if( btns.getVisibility() == View.VISIBLE ){
                btns.setVisibility(View.INVISIBLE);
            }
            else{
                btns.setVisibility(View.VISIBLE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveBitmap(@NonNull final Context context, @NonNull final Bitmap bitmap,
                           @NonNull final Bitmap.CompressFormat format,
                           @NonNull final String mimeType,
                           @NonNull final String displayName) throws IOException {
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        final ContentResolver resolver = context.getContentResolver();
        Uri uri = null;
        try{
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);
            if( uri == null )
                throw new IOException("Failed to create new MediaStore record.");
            try( final OutputStream stream = resolver.openOutputStream(uri) ){
                if (stream == null)
                    throw new IOException("Failed to open output stream.");
                if (!bitmap.compress(format, 100, stream))
                    throw new IOException("Failed to save bitmap.");
            }
            Toast.makeText(getApplicationContext(), "파일을 저장했습니다", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            if( uri != null ){
                resolver.delete(uri, null, null);
            }
            throw e;
        }
    }

    public void saveBitmapToJpeg(Bitmap bitmap, String fileName) {
        File tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            tempFile.createNewFile();
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            Toast.makeText(getApplicationContext(), "파일을 저장했습니다", Toast.LENGTH_SHORT).show();
        } catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
    }

    public void setBtnVisibility(){
        if( nowPos == mArrayUri.size() - 1 ){
            btn_right.setVisibility(View.INVISIBLE);
        }
        else{
            btn_right.setVisibility(View.VISIBLE);
        }
        if( nowPos == 0 ){
            btn_left.setVisibility(View.INVISIBLE);
        }
        else{
            btn_left.setVisibility(View.VISIBLE);
        }
    }

    /*
    DB에서 정보 들고 와서 인포 보여주기
     */
    public void showInfoFromDB(String catName){
        String docPath = "catInfo/" + catName;
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
                        if( (ob = getDB.get("names")) != null ){
                            names = ob.toString();
                            textViewName.setText(names.replace("(endline)", ", "));
                        }
                        if( (ob = getDB.get("features")) != null ){
                            features = ob.toString();
                            textViewFeatures.setText(features.replace("(endline)", "\n"));
                        }
                        if( (ob = getDB.get("num")) != null ){
                            num = (Long)ob;
                        }
                        Log.d("SHOW", catName + " => " + features + " " + num);

                        if( num > 0 ){
                            noInfo.setVisibility(View.INVISIBLE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                        for(int i = 1; i < num + 1; i++){
                            String filename = i + ".jpg";
                            storageRef.child(catName + "/" + filename).getDownloadUrl().addOnCompleteListener(task1 -> {
                                if( task1.isSuccessful() ){
                                    Log.d("GETURI", catName + "/" + filename + " Success ");
                                    mArrayUri.add(task1.getResult());
                                    manager.invalidateSpanAssignments();
                                }
                                else{
                                    Log.d("GETURI", catName + "/" + filename + " Fail");
                                }
                            });
                        } // End for
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });


    } // End showInfoFromDB();

    @Override
    public void onBackPressed(){
        if( layout2.getVisibility() == View.VISIBLE ){
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.INVISIBLE);
        }
        else{
            super.onBackPressed();
        }
    }


}
