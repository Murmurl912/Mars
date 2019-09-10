package com.example.mars;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;

import com.example.mars.ui.CameraTranslationFragment;
import com.example.mars.ui.TextTranslationFragment;
import com.example.mars.ui.VoiceTranslationFragment;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    public static final String TAG = "MainActivity";

    Toolbar mainToolbar;

    TabLayout mainTabLayout;
    ViewPager mainViewPager;

    CameraTranslationFragment cameraTranslationFragment;
    TextTranslationFragment textTranslationFragment;
    VoiceTranslationFragment voiceTranslationFragment;
    MainTabFragmentAdapter mainTabFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    private void setup() {
        // set up top toolbar
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_light);

        // set up tab layout and view pager
        mainTabLayout = findViewById(R.id.main_tab_layout);
        mainViewPager = findViewById(R.id.main_view_pager);

        textTranslationFragment = new TextTranslationFragment();
        cameraTranslationFragment = new CameraTranslationFragment();
        voiceTranslationFragment = new VoiceTranslationFragment();
        mainTabFragmentAdapter = new MainTabFragmentAdapter(getSupportFragmentManager(),
                textTranslationFragment, cameraTranslationFragment, voiceTranslationFragment);

        mainViewPager.setAdapter(mainTabFragmentAdapter);

        mainTabLayout.setupWithViewPager(mainViewPager);
        mainTabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d(TAG, "Tab Selected at Position: " + tab.getPosition() + " Name: " + tab.getText());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        Log.d(TAG, "Tab Unselected at Position: " + tab.getPosition() + " Name: " + tab.getText());

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Log.d(TAG, "Tab Reselected at Position: " + tab.getPosition() + " Name: " + tab.getText());

    }

    private class MainTabFragmentAdapter extends FragmentPagerAdapter {

        Fragment textFragment;
        Fragment voiceFragment;
        Fragment cameraFragment;

        public MainTabFragmentAdapter(FragmentManager fm,
                                      Fragment textFragment,
                                      Fragment cameraFragment,
                                      Fragment voiceFragment) {
            super(fm);

            this.textFragment = textFragment;
            this.cameraFragment = cameraFragment;
            this.voiceFragment = voiceFragment;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return textFragment;
                case 1: return cameraFragment;
                case 2: return voiceFragment;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Text";
                case 1: return "Camera";
                case 2: return "Voice";
                default: return "";
            }
        }
    }

}
