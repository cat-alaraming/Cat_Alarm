package android.cs.pusan.ac.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class verifyCatAlbum extends Add_Information{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_cat);

        ImageView img = (ImageView)findViewById(R.id.verify);
        img.setImageBitmap(albumImg);

        Button btn_check = (Button)findViewById(R.id.check);
        btn_check.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        Button btn_cancle = (Button)findViewById(R.id.cancle);
        btn_cancle.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),  "취소하였습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }
}
