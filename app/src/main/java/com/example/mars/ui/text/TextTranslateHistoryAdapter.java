package com.example.mars.ui.text;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mars.MainApplication;
import com.example.mars.R;
import com.example.mars.database.History;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextTranslateHistoryAdapter extends RecyclerView.Adapter<TextTranslateHistoryAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton speakSourceContent;
        ImageButton favorite;
        TextView sourceText;
        ImageButton speakTargetContent;
        TextView targetText;
        ImageButton copy;
        ImageButton more;
        PopupMenu popupMenu;

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
            popupMenu = new PopupMenu(view.getContext(), more);
            popupMenu.getMenuInflater().inflate(R.menu.menu_main_text_translate_more, popupMenu.getMenu());
        }
    }

    Context context;
    List<History> histories;
    boolean flag = false;
    Activity activity;
    public TextTranslateHistoryAdapter(Context context, Activity activity) {
        this.context = context;
        histories = new ArrayList<>();
        this.activity = activity;
    }

    public void setHistories(List<History> histories) {
        if(histories == null) {
            return;
        }

        this.histories.clear();

        for(History h : histories) {
            this.histories.add((History)h.clone());
        }
        activity.runOnUiThread(this::notifyDataSetChanged);
    }

    public void add(History history) {
        histories.add((History)history.clone());
        activity.runOnUiThread(()->{
            notifyItemInserted(histories.size() - 1);
        });
    }

    public void add(int index, History history) {
        histories.add(index, (History)history.clone());
        activity.runOnUiThread(()->{
            notifyItemInserted(histories.size() - index);
        });
    }

    public History remove(int index) {
        History history = histories.remove(index);
        activity.runOnUiThread(()->{
            notifyItemRemoved(index);
        });
        return history;
    }

    public void update(int index, History history) {
        if(histories.contains(history)) {
            int i = histories.indexOf(history);
            histories.remove(history);
            histories.add(i, (History)history.clone());
        }
        activity.runOnUiThread(()->{
            notifyItemChanged(index);
        });
    }

    public int find(History history) {
        if(history == null) {
            return -1;
        }
        return histories.indexOf(history);
    }

    @NonNull
    @Override
    public TextTranslateHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                     int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.adapter_text_translate_history, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.copy.setOnClickListener(v->{
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("copy", viewHolder.targetText.getText().toString());
            Objects.requireNonNull(cm).setPrimaryClip(mClipData);
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();

        });

        viewHolder.more.setOnClickListener(v->{
            viewHolder.popupMenu.show();
        });

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

        holder.favorite.setOnClickListener(v->{
            history.setFavorite(!history.isFavorite());
            holder.favorite.setImageResource(history.isFavorite() ?
                    R.drawable.ic_fav_light : R.drawable.ic_fav_border_light);
            ((MainApplication)activity.getApplication()).databaseManager.update(history);
        });

        holder.speakSourceContent.setOnClickListener(v->{
            TextToSpeech tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if(i == TextToSpeech.SUCCESS) {
                        flag = true;
                    } else {
                        flag = false;
                        Toast.makeText(context, "TTS Failed To Start!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if(flag) {
                if(tts.isSpeaking()) {
                    tts.stop();
                }
                tts.speak(holder.sourceText.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(context, "Speaking!", Toast.LENGTH_SHORT).show();

            }
        });
        holder.speakTargetContent.setOnClickListener(v->{
            TextToSpeech tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if(i == TextToSpeech.SUCCESS) {
                        flag = true;
                    } else {
                        flag = false;
                        Toast.makeText(context, "TTS Failed To Start!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if(flag) {
                if(tts.isSpeaking()) {
                    tts.stop();
                }
                tts.speak(holder.targetText.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(context, "Speaking!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}
