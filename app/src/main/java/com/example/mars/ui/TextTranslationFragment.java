package com.example.mars.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mars.MainApplication;
import com.example.mars.R;
import com.example.mars.database.DatabaseManager;
import com.example.mars.database.History;
import com.example.mars.translate.TranslateService;

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
    PopupMenu popupMenu;

    private boolean isTTSSuccess;

    private TextTranslateHistoryAdapter adapter;
    private History history;

    private TranslateService translateService;

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
        popupMenu = new PopupMenu(Objects.requireNonNull(getContext()), more);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main_text_translate_more, popupMenu.getMenu());

        new Thread(this::setupListener).start();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTTSSuccess = false;
        adapter = new TextTranslateHistoryAdapter(getContext(), getActivity());
        loadHistory();
    }

    private void loadHistory() {
        new Thread(()->{
            ((MainApplication) Objects
                    .requireNonNull(getActivity())
                    .getApplication())
                    .databaseManager
                    .requestLoadHistoryRecord(null, (status, data)->{
                        adapter.setHistories(data);
                    });
        }).start();
    }

    private void setupListener() {

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        speakSource.setOnClickListener(v->{
            hideKeyboard();
            speak(inputSource.getText().toString());
        });

        clearContent.setOnClickListener(v->{
            clear();
        });

        translate.setOnClickListener(v->{
           translate();
        });

        speakTarget.setOnClickListener(v->{
            hideKeyboard();
            speak(targetText.getText().toString());
        });

        addFavorite.setOnClickListener(v->{
            addFavorite(true);
        });

        more.setOnClickListener(v->{
            popupMenu.show();
        });

        copy.setOnClickListener((v)->{
            ClipboardManager cm = (ClipboardManager) Objects.requireNonNull(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("copy", targetText.getText().toString());
            Objects.requireNonNull(cm).setPrimaryClip(mClipData);
            Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
        });
    }

    private void speak(String text) {

        TextToSpeech tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS) {
                    isTTSSuccess = true;
                } else {
                    isTTSSuccess = false;
                    Toast.makeText(getContext(), "TTS Failed To Start!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        if(!isTTSSuccess) {
            return;
        }

        Toast.makeText(getContext(), "Speaking!",Toast.LENGTH_SHORT).show();

        if(tts.isSpeaking()) {
            tts.stop();
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void translate() {
        targetText.setText("");

        if(history == null) {
            history = new History(System.currentTimeMillis(), false, inputSource.getText().toString(), "");
        } else {
            history.setSource(inputSource.getText().toString());
            history.setTime(System.currentTimeMillis());
        }

        TranslateService.OnTranslateReturn translateCallback = (status, result) -> {

        };

        // here to request translate
        targetText.setText("Translated: " + inputSource.getText().toString());
        recordHistoryToDatabase();
    }

    private void recordHistoryToDatabase() {
        DatabaseManager manager = ((MainApplication) Objects.requireNonNull(getActivity()).getApplication())
                .databaseManager;
        history.setTarget(targetText.getText().toString());
        if(manager.contain(history)) {
            manager.update(history);
            if(adapter.find(history) != -1){
                adapter.update(adapter.find(history), history);
            }
        } else {
            manager.add(history);
            adapter.add(0, history);

        }
    }

    private void onKeyTypeListener() {

    }

    private void clear() {
        hideKeyboard();
        addFavorite.setImageResource(R.drawable.ic_fav_border_light);
        history = null;
        inputSource.setText("");
        targetText.setText("");
    }

    private void addFavorite(boolean isFavorite) {
        if(history == null) {
            return;
        }
        history.setFavorite(isFavorite);
        if(isFavorite) {
            addFavorite.setImageResource(R.drawable.ic_fav_light);
        } else {
            addFavorite.setImageResource(R.drawable.ic_fav_border_light);
        }
    }

    private void hideKeyboard() {
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
