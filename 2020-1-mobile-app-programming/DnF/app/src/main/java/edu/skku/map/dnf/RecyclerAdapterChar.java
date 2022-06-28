package edu.skku.map.dnf;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerAdapterChar extends RecyclerView.Adapter<RecyclerAdapterChar.ItemViewHolder> {

    private ArrayList<CharacterInfo> listData = new ArrayList<>();
    private LayoutInflater mInflate;
    private Context mContext;

    public RecyclerAdapterChar(Context context, ArrayList<CharacterInfo> itemlist){
        this.mContext = context;
        this.mInflate = LayoutInflater.from(context);
        this.listData = itemlist;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.character_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView characterNameTv;
        private TextView levelTv;
        private TextView jobTv;
        private TextView jobGrowTv;
        private TextView server;
        private ImageView characterImg;

        ItemViewHolder(View itemView) {
            super(itemView);

            characterNameTv = itemView.findViewById(R.id.characterName);
            levelTv = itemView.findViewById(R.id.level);
            jobTv = itemView.findViewById(R.id.job);
            jobGrowTv = itemView.findViewById(R.id.jobGrow);
            server = itemView.findViewById(R.id.server);
            characterImg = itemView.findViewById(R.id.characterImg);

        }

        void onBind(CharacterInfo data) {
            characterNameTv.setText(data.getCharacterName());
            levelTv.setText(data.getLevel());
            jobTv.setText(data.getJobName());
            jobGrowTv.setText(data.getJobGrowName());
            server.setText(data.getServerId());
            characterImg.setImageBitmap(data.getCharacterPic());
        }
    }
}
