// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.keyguard.sec.particle;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Random;

public class Particle
{

    private String TAG;
    private int bigRadius;
    private int dotAlpha;
    private float dx;
    private float dy;
    private float gravity;
    private boolean isAlive;
    private int life;
    private float maxSpeed;
    private Paint paint;
    private int rad;
    private int smallRadius;
    private float x;
    private float y;
    
    public Particle(float f, float f1, int i)
    {
        TAG = "ParticleEffect";
        bigRadius = 36;
        dotAlpha = 200;
        gravity = 4F;
        maxSpeed = 7F;
        paint = new Paint();
        smallRadius = 17;
        paint.setAntiAlias(true);
        Random random = new Random();
        life = 50 + random.nextInt(100);
        paint.setColor(i);
        isAlive = true;
        x = f;
        y = f1;
        if(random.nextInt(5) != 0);
        rad = (int)((double)bigRadius * Math.random());
        dx = (float)((double)maxSpeed * Math.random() - (double)(maxSpeed / 2.0F));
        dy = (float)((double)maxSpeed * Math.random() - (double)(maxSpeed / 2.0F)) - gravity;
    }

    public void draw(Canvas canvas)
    {
        if(life >= 30);
        int i = (dotAlpha * life) / 30;
        paint.setAlpha(i);
        canvas.drawCircle(x, y, rad, paint);
        if(life > 0)
            life = -1 + life;
        else
            isAlive = false;
    }

    public boolean isAlive()
    {
        return isAlive;
    }

    public void move()
    {
        x = x + dx;
        y = y + dy;
    }

    public void unlock(float f)
    {
        dx = f * dx;
        dy = f * dy;
        life = life / 2;
    }

}
