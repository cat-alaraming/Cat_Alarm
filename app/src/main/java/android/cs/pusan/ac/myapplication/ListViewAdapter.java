package android.cs.pusan.ac.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.net.URI;
import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private ImageView imageView;
    private TextView titleTextView;
    private TextView contentTextView;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();
    Context mContext;

    public ListViewAdapter(Context context){
        this.mContext = context;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        titleTextView = (TextView) convertView.findViewById(R.id.title1);
        imageView = (ImageView) convertView.findViewById(R.id.icon1);
        contentTextView = (TextView) convertView.findViewById(R.id.text1);

        ListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        titleTextView.setText(listViewItem.getTitle());

//        Log.d("favorite_uri1", listViewItem.getImage().toString());
        try {
            Glide.with(context).load( listViewItem.getImage() ).into(imageView);
//            Log.d("favorite_uri1", listViewItem.getImage().toString());
            Log.d("favorite_uri", "uri 갸져오기 성공");
        }catch (Exception e){
            e.printStackTrace();
            Log.d("favorite_uri", "uri 갸져오기 실패");
        }

        contentTextView.setText(listViewItem.getContent());

        LinearLayout cmdArea= (LinearLayout)convertView.findViewById(R.id.cmdArea);
        cmdArea.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //해당 리스트 클릭시 이벤트
                Intent intent = new Intent(context, showCatInfo.class);
                intent.putExtra("catName", listViewItem.getTitle());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 지정한 위치(position)에 있는 데이터 리턴
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수 -> image가져옴
    public void addItem(String title, Uri image , String content) {
        ListViewItem item = new ListViewItem();

        item.setTitle(title);
        item.setImage(image);
        item.setContent(content);
        listViewItemList.add(item);
    }
}
