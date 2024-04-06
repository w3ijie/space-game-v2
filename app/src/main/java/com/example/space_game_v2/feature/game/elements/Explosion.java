package com.example.space_game_v2.feature.game.elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import com.example.space_game_v2.R;

public class Explosion {
    private Bitmap spriteSheet;
    private int frameCount;
    private int frameWidth;
    private int frameHeight;
    private int currentFrame;
    private long lastFrameChangeTime;
    private long frameDuration;
    private boolean isActive;
    private float x;
    private float y;

    public Explosion(Context context, float x, float y) {
        this.spriteSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion_sprite);
        this.frameCount = 12;
        this.frameWidth = this.spriteSheet.getWidth() / frameCount;
        this.frameHeight = this.spriteSheet.getHeight();
        this.currentFrame = 0;
        this.lastFrameChangeTime = System.currentTimeMillis();
        this.isActive = true;
        this.x = x;
        this.y = y;
        this.frameDuration = 10;
    }

    public void update() {
        long time = System.currentTimeMillis();
        if (time > lastFrameChangeTime + frameDuration) {
            currentFrame++;
            lastFrameChangeTime = time;
            if (currentFrame >= frameCount) {
                isActive = false;
            }
        }
    }

    public void draw(Canvas canvas) {
        if (!isActive) {
            return;
        }
        int frameX = currentFrame * frameWidth;
        Rect frameToDraw = new Rect(frameX, 0, frameX + frameWidth, frameHeight);
        RectF whereToDraw = new RectF(x, y, x + frameWidth, y + frameHeight);
        canvas.drawBitmap(spriteSheet, frameToDraw, whereToDraw, null);
    }

    public boolean getStatus() {
        return isActive;
    }

}