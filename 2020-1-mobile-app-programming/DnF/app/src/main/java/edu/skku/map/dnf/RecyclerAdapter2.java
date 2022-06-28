package edu.skku.map.dnf;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerAdapter2 extends RecyclerView.Adapter<RecyclerAdapter2.MyViewHolder> {

    private ArrayList<MyItem> itemlist;
    private LayoutInflater mInflate;
    private Context mContext;

    public RecyclerAdapter2(Context context, ArrayList<MyItem> itemlist){
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
                    itemlist.get(position).setContents_pic(bitmap);
                    holder.imageView.setImageBitmap(itemlist.get(position).getContents_pic());
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

        try{
            final LoginUser postuser = new LoginUser();
            String username = (String) itemlist.get(position).getUsername();

            postuser.setUsername(username);
            postuser.setupVal();
            holder.contents.setText(itemlist.get(position).getContents());
            holder.titleName.setText(itemlist.get(position).getTitle());
            holder.username.setText(itemlist.get(position).getUsername());
            holder.characterName.setText(itemlist.get(position).getCharacterName());
            holder.serverName.setText(itemlist.get(position).getServerName());

            Log.d("asdsa", String.format("%d", position));
        }
        catch (Exception e){
            Log.d("TAG", "err");
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView contents;
        public ImageView imageView;
        public TextView titleName;
        public TextView characterName;
        public TextView serverName;

        MyViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            characterName = itemView.findViewById(R.id.characterName);
            contents = itemView.findViewById(R.id.contents_contents);
            imageView = itemView.findViewById(R.id.contents_img);
            titleName = itemView.findViewById(R.id.titleName);
            serverName = itemView.findViewById(R.id.serverName);
        }
    }
}
