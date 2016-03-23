package com.android.keyguard.sec;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.keyguard.sec.particle.ParticleEffect;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.android.systemui.R;
import android.os.FileUtils;

@SuppressLint({"NewApi"})
public class ParticleSpaceEffect extends FrameLayout
{
  static final File LOCKSCREEN_WALLPAPER_DIR = new File("/data/data/com.android.systemui/mtk");
  static final File LOCKSCREEN_WALLPAPER_FILE = new File(LOCKSCREEN_WALLPAPER_DIR, "lockScreenWallpaper");
  private int CREATED_DOTS_AMOUNT_DOWN = 15;
  private int CREATED_DOTS_AMOUNT_MOVE = 3;
  private int CREATED_DOTS_SCREEN_ON = 25;
  private float centerX;
  private float centerY;
  private float currentX;
  private float currentY;
  private float downX = 0.0F;
  private float downY = 0.0F;
  private float gapX = 0.0F;
  private float gapY = 0.0F;
  private boolean isUnlockFinished = false;
  private boolean isUnlocking = false;
  private long lastSoundTime = 0L;
  private Bitmap mBgBitmap;
  private ContentResolver mContentResolver;
  private Context mContext;
  
  private int mParticleDragSoundId;
  private int mParticleTapSoundId;
  private SoundPool mSoundPool;
  private ParticleEffect particleEffect;
  private float releaseGapX = 0.0F;
  private float releaseGapY = 0.0F;
  private float stageHeight;
  private float stageWidth;  
  
  private Handler mHandler = new Handler()
  {
    public void handleMessage(Message paramMessage)
    {
      switch (paramMessage.what)
      {
      default:
         break;
      case 0:
        stopSounds();
        clearEffect();      
         break;
      case 1:
        if (ParticleSpaceEffect.this.particleEffect != null)
        {
           int i = getColor(ParticleSpaceEffect.this.centerX, ParticleSpaceEffect.this.centerY);
           particleEffect.addDots(CREATED_DOTS_SCREEN_ON, centerX, centerY, i);      
        }
         break;
      }
        return;
    }
  };


  public ParticleSpaceEffect(Context paramContext)
  {
    super(paramContext);
    mContext = paramContext;
    particleSpaceInit();
  }

    private int getColor(float f, float f1)
    {
        int k;
        if(mBgBitmap == null)
        {
            k = -1;
        } 
        else
        {
            float f2 = f / stageWidth;
            float f3 = f1 / stageHeight;
            int i = (int)(f2 * (float)mBgBitmap.getWidth());
            int j = (int)(f3 * (float)mBgBitmap.getHeight());
            if(i < 0 || i > mBgBitmap.getWidth())
                i = 0;
            if(j < 0 || j > mBgBitmap.getHeight())
                j = 0;
            k = mBgBitmap.getPixel(i, j);
        }
        return k;
    }

  private Bitmap getDefaultWallpaper()
  {
    return BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.default_lockscreen_wallpaper);
  }

  private void particleSpaceInit()
  {
    this.mContentResolver = this.mContext.getContentResolver();
    this.mSoundPool = new SoundPool(1, 1, 0);
    this.mParticleTapSoundId = this.mSoundPool.load(this.mContext, R.raw.waterdroplet_tap, 1);
    this.mParticleDragSoundId = this.mSoundPool.load(this.mContext, R.raw.waterdroplet_unlock, 1);
    DisplayMetrics localDisplayMetrics = getResources().getDisplayMetrics();
    this.stageWidth = localDisplayMetrics.widthPixels;
    this.stageHeight = localDisplayMetrics.heightPixels;
    this.centerX = (this.stageWidth / 2.0F);
    this.centerY = (this.stageHeight / 2.0F);
    this.particleEffect = new ParticleEffect(this.mContext);
    addView(this.particleEffect);
    update();
  }

  public void clearEffect()
  {
    this.currentX = this.centerX;
    this.currentY = this.centerY;
    this.isUnlocking = false;
    this.isUnlockFinished = false;
    this.gapX = 0.0F;
    this.gapY = 0.0F;
    this.releaseGapX = 0.0F;
    this.releaseGapY = 0.0F;
    stopSounds();
    this.particleEffect.clearEffect();
  }

  public Bitmap getLockscreenWallpaper()
  {
  	Bitmap bitmap;
    try
    {
      ParcelFileDescriptor localParcelFileDescriptor = ParcelFileDescriptor.open(LOCKSCREEN_WALLPAPER_FILE, 0x10000000);
      bitmap = BitmapFactory.decodeFileDescriptor(localParcelFileDescriptor.getFileDescriptor(), null, null);
      if (bitmap == null)
      {
        bitmap = getDefaultWallpaper();
      }
      else 
      {
          //try
          {

              if ((((Bitmap)bitmap).getWidth() > 720) && (((Bitmap)bitmap).getHeight() == 1280))
              {  
                  Bitmap localBitmap1 = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888);
                  Canvas localCanvas = new Canvas(localBitmap1);
                  Rect localRect1 = new Rect(0, 0, ((Bitmap)bitmap).getWidth(), ((Bitmap)bitmap).getHeight());
                  Rect localRect2 = new Rect(0, 0, 720, 1280);
                  int i = (localRect1.width() - localRect2.width()) / 2;
                  localRect1.inset(Math.max(0, i), 0);
                  localRect2.inset(Math.max(0, -i), 0);
                  localCanvas.drawBitmap((Bitmap)bitmap, localRect1, localRect2, null);
                  ((Bitmap)bitmap).recycle();
                  bitmap = localBitmap1;
              }
          }
          //catch (IOException localIOException)
          //{
            // localIOException.printStackTrace();
          //}
      }
      try {
        localParcelFileDescriptor.close();
      }
      catch (IOException localIOException)
      {
             localIOException.printStackTrace();
      }    
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
        //grape_S6 lockscreenwallpaper
        if (!LOCKSCREEN_WALLPAPER_DIR.exists()) { 
            LOCKSCREEN_WALLPAPER_DIR.mkdir();
            FileUtils.setPermissions(
                        LOCKSCREEN_WALLPAPER_DIR.getPath(),
                        FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IRWXO,
                        -1, -1);            
        }
        //end
        bitmap = getDefaultWallpaper();
    }    
    return bitmap;    
  }

  public long getUnlockDelay()
  {
    return 250L;
  }

  public boolean handleTouchEvent(View paramView, MotionEvent paramMotionEvent)
  {
    boolean i = false;
    if (isUnlocking)
       return false;
       
    if (isUnlockFinished)
    {
        if (paramMotionEvent.getActionMasked() == 1)
           clearEffect();
    }
    currentX = paramMotionEvent.getRawX();
    currentY = paramMotionEvent.getRawY();
    int j = getColor(currentX, currentY);
    long l = System.currentTimeMillis();
    switch (paramMotionEvent.getActionMasked())
    {
    default:
       break;
    case 0: //down
      this.downX = paramMotionEvent.getX();
      this.downY = paramMotionEvent.getY();
      playSounds(this.mParticleTapSoundId);
      this.lastSoundTime = l;
      this.particleEffect.addDots(this.CREATED_DOTS_AMOUNT_DOWN, this.currentX, this.currentY, j);  
      return true;   
    case 2://move
      if (l - this.lastSoundTime > 1500L)
      {
        playSounds(this.mParticleDragSoundId);
        this.lastSoundTime = l;
      }
      if (paramMotionEvent.getActionIndex() == 0)
        particleEffect.addDots(this.CREATED_DOTS_AMOUNT_MOVE, this.currentX, this.currentY, j);   
        return true; 
    case 1://up and cancel
    case 3:
      this.currentX = this.centerX;
      this.currentY = this.centerY;
      this.releaseGapX = this.gapX;
      this.releaseGapY = this.gapY;
      stopSounds();
      return true;    
    }
    
    return false;
 
  }


    public void handleUnlock(View view, MotionEvent motionevent)
    {
        if(!isUnlocking)
        {
            isUnlocking = true;
            particleEffect.unlockDots();
            mHandler.sendEmptyMessageDelayed(0, 800L);
        }
    }

  public void playSounds(int paramInt)
  {
    if (Settings.System.getInt(this.mContentResolver, "lockscreen_sounds_enabled", 1) == 1)
      this.mSoundPool.play(paramInt, 1.0F, 1.0F, 1, 0, 1.0F);
  }

  public void screenTurnedOff()
  {
    clearEffect();
  }

  public void screenTurnedOn()
  {
    this.mHandler.sendEmptyMessageDelayed(1, 1200L);
  }

  public void stopSounds()
  {
    if (this.mParticleTapSoundId != 0)
      this.mSoundPool.stop(this.mParticleTapSoundId);
    if (this.mParticleDragSoundId != 0)
      this.mSoundPool.stop(this.mParticleDragSoundId);
  }

  public void update()
  {
    Bitmap localBitmap = this.mBgBitmap;
    this.mBgBitmap = getLockscreenWallpaper();
    if (this.mBgBitmap != null)
      setBackground(new BitmapDrawable(this.mBgBitmap));
    if (localBitmap != null)
      localBitmap.recycle();
  }
}