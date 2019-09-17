package com.example.mars.ui.camera;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOverlay;

import com.example.mars.detector.TextBox;

import java.util.List;

public class CameraGraphicsOverlay {

    private View view;
    private ViewOverlay overlay;

    public CameraGraphicsOverlay(View view) {
        this.view = view;
        overlay = view.getOverlay();
    }

    public void add(Drawable drawable) {
        overlay.add(drawable);
    }

    public void clear() {
        overlay.clear();
    }

    public void remove(Drawable drawable) {
        overlay.remove(drawable);
    }

    public void add(List<TextBox> boxList) {
        for(TextBox b : boxList) {

            overlay.add(new TextDrawable("", b.getPoints(), b.getSize()));
        }
    }
}
