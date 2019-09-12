package com.example.mars;

import android.app.Application;

import com.example.mars.database.DatabaseManager;

public class MainApplication extends Application {

    public DatabaseManager databaseManager;
    public MainApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseManager = new DatabaseManager(getBaseContext());

    }
}
