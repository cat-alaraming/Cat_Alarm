package android.cs.pusan.ac.myapplication;

import android.net.Uri;
import android.util.Log;

import java.net.URI;

public class ListViewItem {
    private String contentStr ;
    private String titleStr ;
    private Uri uri_;

    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setContent(String content) {
        contentStr = content ;
    }
    public void setImage(Uri uri) { uri_ = uri ; }


    public String getContent() {
        return this.contentStr ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public Uri getImage() {
        Log.d("getImage_uri", uri_.toString());
        return this.uri_ ;
    }
}
