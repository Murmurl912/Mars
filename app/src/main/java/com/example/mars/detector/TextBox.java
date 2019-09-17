package com.example.mars.detector;

import android.view.View;

import org.opencv.core.Point;
import org.opencv.core.Size;

public class TextBox {
    private Point[] points;
    private Size size;
    protected TextBox(Point[] points, Size size) {
        this.points = points;
        this.size = size;
    }

    public Point[] getPoints() {
        return points;
    }

    public Size getSize() {
        return size;
    }

    public void translate() {

    }
}
