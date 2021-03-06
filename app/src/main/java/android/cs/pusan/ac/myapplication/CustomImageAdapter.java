package android.cs.pusan.ac.myapplication;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;

public class CustomImageAdapter extends RecyclerView.Adapter<CustomImageAdapter.ViewHolder> {
    private OnItemClickListener mListener = null;
    int itemLayout;
    int contents;
    Context mContext;
    ArrayList<Uri> mArrayUri;
    ArrayList<String> searchedUriName;
    Object[] IndexArray;
    boolean searched = false;

    public CustomImageAdapter(int contents, int itemLayout, Context context, ArrayList<Uri> mArrayUri){
        this.contents = contents;
        this.itemLayout = itemLayout;
        this.mContext = context;
        this.mArrayUri = mArrayUri;
    }

    public void setIndexArray(Object[] IndexArray) { this.IndexArray = IndexArray; }
    public void setArrayUri(ArrayList<Uri> mArrayUri){
        this.mArrayUri = mArrayUri;
    }
    public void setSearchedArrayUriName(ArrayList<String> searchedUriName){
        this.searchedUriName = searchedUriName;
    }
    public void setSearched(boolean b){
        this.searched = b;
    }

    @Override public int getItemCount() {
        return mArrayUri.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView);
            if( contents == 2 ) textView = itemView.findViewById(R.id.tv_catName);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition() ;
                if( pos != RecyclerView.NO_POSITION ){
                    if( mListener != null ) {
                        mListener.onItemClick(v, pos);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public CustomImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomImageAdapter.ViewHolder holder, int position) {
        if( contents == 2 ){
            if( searched ){
                holder.textView.setText(searchedUriName.get(position));
            }
            else{
                holder.textView.setText(IndexArray[position].toString());
            }
            Glide.with(mContext).load(mArrayUri.get(position))
                    .transform(new FitCenter(), new RoundedCorners(convertDPtoPX(11))).into(holder.image);
            Log.d("test_uri", mArrayUri.get(position).toString() );
        }
        else{
            Glide.with(mContext).load(mArrayUri.get(position)).into(holder.image);
        }
    }

    public int convertDPtoPX(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }


}