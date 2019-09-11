package com.example.mars.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mars.R;
import com.example.mars.database.History;

import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class TextTranslationFragment extends Fragment {
    private ImageButton speakSource;
    private ImageButton clearContent;
    private EditText inputSource;
    private ImageButton translate;
    private ImageButton speakTarget;
    private ImageButton addFavorite;
    private TextView targetText;
    private ImageButton copy;
    private ImageButton more;
    private RecyclerView recyclerView;
    private View view;

    private TextToSpeech tts;
    private boolean isTTSSuccess;
    private boolean isFavorite;

    private TextTranslateHistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_translation, container, false);
        speakSource = view.findViewById(R.id.main_text_tab_speak_source_button);
        clearContent = view.findViewById(R.id.main_text_tab_clear_source_content_button);
        inputSource = view.findViewById(R.id.main_text_tab_source_edit_text);
        translate = view.findViewById(R.id.main_text_tab_translate_button);
        speakTarget = view.findViewById(R.id.main_text_tab_speak_target_button);
        addFavorite = view.findViewById(R.id.main_text_tab_favorite);
        targetText = view.findViewById(R.id.main_text_tab_target_text);
        copy = view.findViewById(R.id.main_text_tab_copy_translation);
        more = view.findViewById(R.id.main_text_tab_more);
        recyclerView = view.findViewById(R.id.main_text_tab_recycler_view);
        this.view = view;

        new Thread(this::setupListener).start();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTTSSuccess = false;
        isFavorite = false;
        adapter = new TextTranslateHistoryAdapter(getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListener() {

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        speakSource.setOnClickListener(v->{
            cancel();
            if(tts == null) {
                tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if(i == TextToSpeech.SUCCESS) {
                            Toast.makeText(getContext(), "Speaking",Toast.LENGTH_SHORT).show();
                            isTTSSuccess = true;
                        }
                    }
                });
            }

            if(!isTTSSuccess) {
                Toast.makeText(getContext(), "TTS Failed",Toast.LENGTH_SHORT).show();
            }

            if(tts.isSpeaking()) {
                tts.stop();
            }
            tts.speak(inputSource.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        });

        clearContent.setOnClickListener(v->{
            inputSource.setText("");
        });

        translate.setOnClickListener(v->{
            targetText.setText("Translated: " + inputSource.getText());
            adapter.add(new History(System.currentTimeMillis(), false, inputSource.getText().toString(), targetText.getText().toString()));
            cancel();
        });

        speakTarget.setOnClickListener(v->{
            cancel();
            if(tts == null) {
                tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if(i == TextToSpeech.SUCCESS) {
                            Toast.makeText(getContext(), "Speaking",Toast.LENGTH_SHORT).show();
                            isTTSSuccess = true;
                        }
                    }
                });
            }

            if(!isTTSSuccess) {
                Toast.makeText(getContext(), "TTS Failed",Toast.LENGTH_SHORT).show();
            }

            if(tts.isSpeaking()) {
                tts.stop();
            }
            tts.speak(targetText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        });

        addFavorite.setOnClickListener(v->{
            if(isFavorite) {
                addFavorite.setImageResource(R.drawable.ic_fav_border_light);
                isFavorite = false;
            } else {
                addFavorite.setImageResource(R.drawable.ic_fav_light);
                isFavorite = true;
            }
        });
    }

    private void cancel() {
        inputSource.clearFocus();
        InputMethodManager inputMethodManager =
                (InputMethodManager) Objects.requireNonNull(getContext())
                        .getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


    }

    public void scrollToStart() {
        ((NestedScrollView)view.findViewById(R.id.main_text_tab_nested_scroll_view)).smoothScrollTo(0, 0);
    }
}
