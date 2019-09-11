package com.example.mars.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mars.R;
import com.example.mars.database.History;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TextTranslateHistoryAdapter extends RecyclerView.Adapter<TextTranslateHistoryAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton speakSourceContent;
        ImageButton favorite;
        TextView sourceText;
        ImageButton speakTargetContent;
        TextView targetText;
        ImageButton copy;
        ImageButton more;
        public ViewHolder(View view) {
            super(view);
            speakSourceContent =
                    view.findViewById(R.id.main_text_tab_history_speak_source_content_button);
            favorite = view.findViewById(R.id.main_text_tab_history_fav_button);
            sourceText = view.findViewById(R.id.main_text_tab_history_source_text);
            speakTargetContent =
                    view.findViewById(R.id.main_text_tab_history_speak_target_content_button);
            targetText = view.findViewById(R.id.main_text_tab_history_target_text);
            copy = view.findViewById(R.id.main_text_tab_history_copy_translation);
            more = view.findViewById(R.id.main_text_tab_history_more);


        }
    }

    Context context;
    List<History> histories;

    public TextTranslateHistoryAdapter(Context context) {
        this.context = context;
        histories = new ArrayList<>();
    }

    public void setHistories(List<History> histories) {
        if(histories == null) {
            return;
        }

        this.histories.clear();

        for(History h : histories) {
            histories.add((History)h.clone());
        }
        notifyDataSetChanged();
    }

    public void add(History history) {
        histories.add((History)history.clone());
        notifyItemInserted(histories.size() - 1);
    }

    public void add(int index, History history) {
        histories.add(index, (History)history.clone());
        notifyItemInserted(histories.size() - index);

    }

    public History remove(int index) {
        History history = histories.remove(index);
        notifyItemRemoved(index);
        return history;
    }

    public void update(int index, History history) {
        if(histories.contains(history)) {
            int i = histories.indexOf(history);
            histories.remove(history);
            histories.add(i, (History)history.clone());
        }
        notifyItemChanged(index);
    }

    @NonNull
    @Override
    public TextTranslateHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                     int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.adapter_text_translate_history, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TextTranslateHistoryAdapter.ViewHolder holder
            , int position) {
        History history = histories.get(position);
        holder.sourceText.setText(history.getSource());
        holder.targetText.setText(history.getTarget());
        holder.favorite.setImageResource(history.isFavorite() ?
                R.drawable.ic_fav_light : R.drawable.ic_fav_border_light);
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}
