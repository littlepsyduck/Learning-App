package com.example.learning_app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> mLessons;
    private final Context context;
    private final OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(Context context, OnLessonClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setLessons(List<Lesson> lessons) {
        this.mLessons = lessons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        if (mLessons != null) {
            Lesson lesson = mLessons.get(position);

            if (lesson.isLocked) {
                holder.imgLesson.setImageResource(R.drawable.btn_lesson_locked);
                holder.itemView.setAlpha(0.6f);
                holder.itemView.setEnabled(false);
            } else {
                holder.imgLesson.setImageResource(R.drawable.btn_lesson_active);
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setEnabled(true);
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onLessonClick(lesson);
                });
            }
            
            if (position > 0) {
                holder.lineTop.setVisibility(android.view.View.VISIBLE);
            } else {
                holder.lineTop.setVisibility(android.view.View.GONE);
            }
            
            if (position < mLessons.size() - 1) {
                holder.lineBottom.setVisibility(android.view.View.VISIBLE);
            } else {
                holder.lineBottom.setVisibility(android.view.View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mLessons != null ? mLessons.size() : 0;
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgLesson;
        private final android.view.View lineTop;
        private final android.view.View lineBottom;
        
        private LessonViewHolder(View itemView) {
            super(itemView);
            imgLesson = itemView.findViewById(R.id.imgLesson);
            lineTop = itemView.findViewById(R.id.lineTop);
            lineBottom = itemView.findViewById(R.id.lineBottom);
        }
    }
}

