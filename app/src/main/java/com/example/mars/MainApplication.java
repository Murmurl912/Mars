package com.example.mars;

import android.app.Application;

import com.example.mars.database.DatabaseManager;
import com.example.mars.translate.TranslateService;

public class MainApplication extends Application {

    public DatabaseManager databaseManager;
    public TranslateService translateService;

    public MainApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseManager = new DatabaseManager(getBaseContext());
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TranslateService getTranslateService() {
        return translateService;
    }

}
