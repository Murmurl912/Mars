package com.example.mars.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private Context context;
    SQLiteDatabase database;
    private final String dbname = "history_database.db";
    private List<History> histories;

    public DatabaseManager(Context context) {
        this.context = context;
        openOrCreateDatabase();
        histories = new ArrayList<>();
    }

    private void openOrCreateDatabase() {
        database = context.openOrCreateDatabase("history_database.db", Context.MODE_PRIVATE, null);
        if(database.getVersion() == 0) {
            database.execSQL("create table History (" +
                    "source text, " +
                    "target text, " +
                    "time integer, " +
                    "is_favorite integer, " +
                    "primary key(source))");
            database.setVersion(1);
        }

    }

    public boolean contain(History history) {
        if(history == null) {
            return false;
        }
        if(histories.isEmpty()) {
            Cursor cursor = database.rawQuery("select * from History", null);
            while (cursor.moveToNext()) {
                histories.add(new History(cursor.getLong(2), cursor.getInt(3) != 0,
                        cursor.getString(0), cursor.getString(1)));
            }
            cursor.close();
        }
        return histories.contains(history);
    }

    public void add(History history) {
        if(history == null) {
            return;
        }

        if(contain(history)) {
            return;
        }
        database.execSQL("insert into History values("
                + "'" + escapeSingleQuote(history.getSource()) + "', "
                + "'" + escapeSingleQuote(history.getTarget()) + "', "
                + history.getTime()+ ", "
                + (history.isFavorite() ? 1 : 0) + ")");
        histories.add((History)history.clone());
    }

    public void remove(History history) {
        if(history == null || history.getSource().equals("")) {
            return;
        }

        if(contain(history)) {
            database.execSQL("delete from History where source = '" + escapeSingleQuote(history.getSource()) + "'");
        }
        histories.remove(history);
    }

    public void update(History value) {
        if(value == null) {
            return;
        }

        database.execSQL("update History set target = " +
                "'" + escapeSingleQuote(value.getTarget()) +
                "', time = " + value.getTime() +
                ", is_favorite = " + (value.isFavorite() ? 1 : 0) +
                " where source = '" + escapeSingleQuote(value.getSource()) + "'");
        History history = histories.get(histories.indexOf(value));
        history.setTime(value.getTime());
        history.setFavorite(value.isFavorite());
        history.setTarget(value.getTarget());
    }

    public void requestLoadHistoryRecord(List<History> container, OnHistoryRecordLoadComplete callback) {
        List<History> list = container == null ? new ArrayList<>() : container;
        if(histories.isEmpty()) {
            Cursor cursor = database.rawQuery("select * from History", null);
            while (cursor.moveToNext()) {
                histories.add(new History(cursor.getLong(2), cursor.getInt(3) != 0,
                        cursor.getString(0), cursor.getString(1)));
            }
            cursor.close();
        }
        for(History h : histories) {
            list.add((History)h.clone());
        }
        callback.onHistoryRecordLoadComplete(0, list);
    }

    public String escapeSingleQuote(String input) {
        StringBuilder output = new StringBuilder();
        if(input == null || input.equals("")) {
            return output.toString();
        }
        char[] chars = input.toCharArray();
        for(char c : chars) {
            if(c == '\'') {
                output.append('\\').append(c);
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public interface OnHistoryRecordLoadComplete {
        void onHistoryRecordLoadComplete(int status, List<History> data);
    }
}
