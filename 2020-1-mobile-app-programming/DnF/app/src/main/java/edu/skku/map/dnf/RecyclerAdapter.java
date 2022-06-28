package edu.skku.map.dnf;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    private ArrayList<SkillData> listData = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.skill_item, parent, false);
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

    void addItem(SkillData data) {
        listData.add(data);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView skillNameTv;
        private TextView skillTypeTv;
        private TextView costTypeTv;
        private TextView requiredLevelTv;

        ItemViewHolder(View itemView) {
            super(itemView);

            skillNameTv = itemView.findViewById(R.id.skillName);
            skillTypeTv = itemView.findViewById(R.id.skillType);
            costTypeTv = itemView.findViewById(R.id.costType);
            requiredLevelTv = itemView.findViewById(R.id.requiredLevel);

        }

        void onBind(SkillData data) {
            skillNameTv.setText(data.getSkillName());
            skillTypeTv.setText(data.getSkillType());
            costTypeTv.setText(data.getCostType());
            requiredLevelTv.setText(data.getRequiredLevel());

        }
    }
}
