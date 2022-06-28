package edu.skku.map.pa2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.ArrayTable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private ArrayList<MyItem> itemlist;
    private LayoutInflater mInflate;
    private Context mContext;

    public RecyclerAdapter(Context context, ArrayList<MyItem> itemlist){
        this.mContext = context;
        this.mInflate = LayoutInflater.from(context);
        this.itemlist = itemlist;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.inflate(R.layout.listview_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return itemlist.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        String pic_link = (String) itemlist.get(position).getPicLink();
        if (pic_link.getBytes().length > 0) {
            StorageReference contentRef = FirebaseStorage.getInstance().getReference("Images").child(pic_link);
            final long ONE_MEGABYTE = 1024 * 1024;
            contentRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    itemlist.get(position).setpic(bitmap);
                    holder.imageView.setImageBitmap(itemlist.get(position).getpic());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        else{
            holder.imageView.setVisibility(View.GONE);
        }

        final LoginUser postuser = new LoginUser();
        String id_val = (String) itemlist.get(position).getusername();

        postuser.setUsername(id_val);
        retData retdata3 = new retData() {
            @Override
            public void retDataFunc(LoginUser postuser) {
            retData retdata4 = new retData() {
                @Override
                public void retDataFunc(LoginUser postuser) {
                    itemlist.get(position).setIcon(postuser.getIcon());
                    holder.icon.setImageBitmap(itemlist.get(position).getIcon());
                }
            };
            postuser.setupicon(retdata4);
            }
        };
        postuser.setupVal(retdata3);
        holder.contents.setText(itemlist.get(position).getcontents());
        holder.tags.setText(itemlist.get(position).gettags());
        holder.username.setText(itemlist.get(position).getusername());

        Log.d("asdsa", String.format("%d", position));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView tags;
        public TextView contents;
        public ImageView imageView;
        public ImageView icon;

        MyViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.contents_id_val);
            tags = itemView.findViewById(R.id.contents_tags);
            contents = itemView.findViewById(R.id.contents_contents);
            imageView = itemView.findViewById(R.id.contents_img);
            icon = itemView.findViewById(R.id.contents_profile_img);
        }
    }
}
