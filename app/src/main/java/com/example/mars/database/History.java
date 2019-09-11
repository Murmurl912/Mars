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
        this.source = source;
        this.target = target;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
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

        if (time != history.time) return false;
        return source != null ? source.equals(history.source) : history.source == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public Object clone() {
        return new History(time, isFavorite, source, target);
    }
}
