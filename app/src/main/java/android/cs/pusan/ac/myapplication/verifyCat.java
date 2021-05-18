package android.cs.pusan.ac.myapplication;

import android.os.Bundle;
import android.widget.ImageView;

public class verifyCat extends Add_Information {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_cat);

        ImageView img = (ImageView)findViewById(R.id.verify);
        img.setImageBitmap(mImg);

    }
}
