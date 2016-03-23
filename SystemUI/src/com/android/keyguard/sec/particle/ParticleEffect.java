package com.android.keyguard.sec.particle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;

public class ParticleEffect extends View
{
  private String TAG = "ParticleEffect";
  private int dotMaxLimit = 300;
  private int dotUnlockSpeed = 6;
  private int drawingDelayTime = 1;
  private boolean isDrawing;
  Handler mHandler = new Handler()
  {
    public void handleMessage(Message paramMessage)
    {
      invalidate();
      if (isDrawing)
        mHandler.sendEmptyMessageDelayed(0, drawingDelayTime);
    }
  };
  private ArrayList<Particle> particleList = new ArrayList();

  public ParticleEffect(Context paramContext)
  {
    super(paramContext);
  }


  private void startDrawing()
  {
      if(!isDrawing)
      {
          isDrawing = true;
          mHandler.sendEmptyMessageDelayed(0, drawingDelayTime);
      }
  }


  private void stopDrawing()
  {
    Log.d(TAG, "stop drawing");
    isDrawing = false;
  }

  public void addDots(int i, float f, float f1, int j)
  {
      if(particleList.size() <= dotMaxLimit)
      {
          for(int k = 0; k < i; k++)
          {
              float af[] = new float[3];
              Color.RGBToHSV(Color.red(j), Color.green(j), Color.blue(j), af);
              af[1] = (float)((double)af[1] * (1.0D - 0.69999999999999996D * Math.random()));
              af[2] = (float)((double)(1.0F + af[2]) - (double)af[2] * Math.random());
              Particle particle = new Particle(f, f1, Color.HSVToColor(af));
              particleList.add(particle);
          }
  
          startDrawing();
      }
  }

  public void clearEffect()
  {
  }

  public void destroy()
  {
  }

  protected void onDraw(Canvas canvas)
  {
      super.onDraw(canvas);
      if(particleList.size() <= 0)
      {
          stopDrawing();
      } else
      {
          int i = 0;
          while(i < particleList.size()) 
          {
              Particle particle = (Particle)particleList.get(i);
              if(particle != null && particle.isAlive())
              {
                  particle.move();
                  particle.draw(canvas);
              } else
              {
                  particleList.remove(i);
                  i--;
              }
              i++;
          }
      }
  }


  public void unlockDots()
  {
    for (int i = 0; i < particleList.size(); i++)
      ((Particle)particleList.get(i)).unlock(dotUnlockSpeed);
  }
}
