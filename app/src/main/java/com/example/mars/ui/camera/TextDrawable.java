package com.example.mars.ui.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import org.opencv.core.Point;
import org.opencv.core.Size;;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextDrawable extends Drawable {

    private String text;
    private Paint paint;
    private int color;
    private Paint background;
    private Paint foreground;
    private Point[] points;
    private double angle;
    private Size size;

    public TextDrawable(String text, Point[] points, Size size) {
        this.text = text == null ? "" : text;
        this.points = points;
        background = new Paint();
        background.setColor(Color.parseColor("#80ff0000"));
        foreground = new Paint();
        foreground.setColor(Color.parseColor("#8000ff00"));

        this.size = size;
    }

    private double getAngle() {
        if(points.length != 4) {
            return 0;
        }

        double a = points[0].x - points[2].x;
        double b = points[0].y - points[2].y;
        angle = Math.atan(a/b);
        return this.angle;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(points.length != 4) {
            return;
        }

        Rect rect = getBounds();
        if(rect.isEmpty()) {
            rect = canvas.getClipBounds();
        }
        int height = rect.height();
        int width = rect.width();

        double x = (float)canvas.getWidth() / size.height;
        double y = (float)canvas.getHeight() / size.width;
        Point ratio = new Point(x, x);

//        canvas.drawLine((float)(points[0].x * ratio.x), (float)(points[0].y * ratio.y), (float)(points[1].x * ratio.x), (float)(points[1].y * ratio.y), foreground);
//        canvas.drawLine((float)(points[1].x * ratio.x), (float)(points[1].y * ratio.y), (float)(points[2].x * ratio.x), (float)(points[2].y * ratio.y), foreground);
//        canvas.drawLine((float)(points[2].x * ratio.x), (float)(points[2].y * ratio.y), (float)(points[3].x * ratio.x), (float)(points[3].y * ratio.y), foreground);
//        canvas.drawLine((float)(points[3].x * ratio.x), (float)(points[3].y * ratio.y), (float)(points[0].x * ratio.x), (float)(points[0].y * ratio.y), foreground);
//
//        canvas.drawRect((float)(points[0].x * ratio.x), (float)(points[0].y * ratio.y), (float)(points[2].x * ratio.x), (float)(points[2].y * ratio.y), background);

        canvas.drawLine((float)(points[0].y * ratio.y), (float)(points[0].x * ratio.x),
                (float)(points[1].y * ratio.y), (float)(points[1].x * ratio.x),
                foreground);
        canvas.drawLine((float)(points[1].y * ratio.y), (float)(points[1].x * ratio.x),
                (float)(points[2].y * ratio.y), (float)(points[2].x * ratio.x),
                foreground);
        canvas.drawLine((float)(points[2].y * ratio.y), (float)(points[2].x * ratio.x),
                (float)(points[3].y * ratio.y), (float)(points[3].x * ratio.x),
                foreground);
        canvas.drawLine((float)(points[3].y * ratio.y), (float)(points[3].x * ratio.x),
                (float)(points[0].y * ratio.y), (float)(points[0].x * ratio.x),
                foreground);

        canvas.drawRect((float)(points[0].y * ratio.y), (float)(points[0].x * ratio.x), (float)(points[2].y * ratio.y), (float)(points[2].x * ratio.x), background);

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
