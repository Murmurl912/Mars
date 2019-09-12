package com.example.mars.database;

import androidx.annotation.NonNull;

public class History {
    private boolean isFavorite;
    private long time;
    private String source;
    private String target;

    public History() {
        isFavorite = false;
        time = System.currentTimeMillis();
        source = "";
        target = "";
    }

    public History(long time, boolean isFavorite, String source, String target) {
        this.time = time;
        this.isFavorite = isFavorite;
        this.source = source == null ? "" : source;
        this.target = target == null ? "" : target;

    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSource(String source) {
        this.source = source == null ? "" : source;
    }

    public void setTarget(String target) {
        this.target = target == null ? "" : target;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public long getTime() {
        return time;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        History history = (History) o;

        return source.equals(history.source);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @NonNull
    @Override
    public Object clone() {
        return new History(time, isFavorite, source, target);
    }
}
