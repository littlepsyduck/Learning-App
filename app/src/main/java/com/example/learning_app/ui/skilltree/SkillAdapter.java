package com.example.learning_app.ui.skilltree;

import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.learning_app.R;
import com.example.learning_app.data.local.entity.SkillEntity;
import java.util.*;

public class SkillAdapter extends RecyclerView.Adapter<SkillAdapter.NodeHolder> {

    private List<SkillEntity> data = new ArrayList<>();
    private OnClick cb;

    public interface OnClick { void onClick(SkillEntity s); }

    public SkillAdapter(OnClick cb) {
        this.cb = cb;
    }

    public void setData(List<SkillEntity> list) {
        data = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NodeHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skill_node, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NodeHolder h, int pos) {
        SkillEntity s = data.get(pos);

        // Set icon based on unlock
        if (!s.unlocked) {
            h.circle.setBackgroundResource(R.drawable.bg_skill_node_locked);
            h.icon.setImageResource(R.drawable.ic_placeholder);
        } else {
            h.circle.setBackgroundResource(R.drawable.bg_skill_node_unlocked);
            h.icon.setImageResource(R.drawable.img);
        }

        // Zig-zag
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) h.itemView.getLayoutParams();
        if (pos % 2 == 0) { p.leftMargin = 80; p.rightMargin = 0; }
        else { p.rightMargin = 80; p.leftMargin = 0; }

        h.itemView.setLayoutParams(p);

        h.itemView.setOnClickListener(v -> {
            if (s.unlocked) cb.onClick(s);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class NodeHolder extends RecyclerView.ViewHolder {
        ImageView circle, icon;
        NodeHolder(View v) {
            super(v);
            circle = v.findViewById(R.id.imgCircle);
            icon = v.findViewById(R.id.imgIcon);
        }
    }
}
