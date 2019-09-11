package com.example.mars;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mars.ui.CameraTranslationFragment;
import com.example.mars.ui.TextTranslationFragment;
import com.example.mars.ui.VoiceTranslationFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";

    Toolbar mainToolbar;

    TabLayout mainTabLayout;
    ViewPager mainViewPager;

    CameraTranslationFragment cameraTranslationFragment;
    TextTranslationFragment textTranslationFragment;
    VoiceTranslationFragment voiceTranslationFragment;
    MainTabFragmentAdapter mainTabFragmentAdapter;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

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

        // set up drawer
        drawerLayout = findViewById(R.id.main_drawer_layout);

        // set up navigation view
        navigationView = findViewById(R.id.main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void closeDrawer() {
        if(drawerLayout == null) {
            return;
        }

        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        Log.d(TAG, "closeDrawer()");
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
        if(tab.getPosition() == 0) {
            Log.d(TAG, "scrollToStart Called");
            textTranslationFragment.scrollToStart();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_navigation_menu_item_home:
                closeDrawer();
                break;
            case R.id.main_navigation_menu_item_favorite:
                closeDrawer();
                break;
            case R.id.main_navigation_menu_item_offline:
                closeDrawer();
                break;
            case R.id.main_navigation_menu_item_setting:
                closeDrawer();
                break;
            case R.id.main_navigation_menu_item_about:
                closeDrawer();
                break;
            case R.id.main_navigation_menu_item_help:
                closeDrawer();
                break;
            default:
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG,
                "onOptionsItemSelected: id = "
                        + item.getItemId() + ", title = "
                        + item.getTitle());
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
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
