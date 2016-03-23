package com.android.keyguard.sec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.R;

@SuppressLint({"NewApi"})
public class KeyguardEffectViewMain extends FrameLayout
{
  private float downX = 0.0F;
  private float downY = 0.0F;
  private boolean handleUnlocked = false;
  private ParticleSpaceEffect mKeyguardEffectView = new ParticleSpaceEffect(getContext());
  private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
  private ViewMediatorCallback mViewMediatorCallback;
  private Animation screenOnAnimation;  
  
  
  private Handler mHandler = new Handler()
  {
    public void handleMessage(Message paramMessage)
    {
      switch (paramMessage.what)
      {
         default:
           break;
         case 0:
                if(mViewMediatorCallback != null)
                    mViewMediatorCallback.userActivity();
                if(mStatusBarKeyguardViewManager != null)
                    mStatusBarKeyguardViewManager.dismiss();      
           break;
      }
      return;
    }
  };


  public KeyguardEffectViewMain(Context paramContext)
  {
    super(paramContext);
    FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -1);
    addView(this.mKeyguardEffectView, 0, localLayoutParams);
    this.screenOnAnimation = AnimationUtils.loadAnimation(this.mContext, R.anim.tw_particle_lock);
    this.screenOnAnimation.setFillAfter(true);
  }


    public boolean handleTouchEvent(MotionEvent motionevent)
    {
        boolean flag;
        boolean flag1;
        flag = true;
        flag1 = false;
        if(mKeyguardEffectView == null) 
           return false;

        int i = motionevent.getAction();

        if(0 == i)
        {
            downX = motionevent.getX();
            downY = motionevent.getY();
            handleUnlocked = false;
            if(mViewMediatorCallback != null)
                mViewMediatorCallback.userActivity();
        }
        if(handleUnlocked) 
           return false;
           
        if (2 == i)
        {
        	  if(Math.abs(motionevent.getX() - downX) > 360F && Math.abs(motionevent.getY() - downY) > 360F)        	     
        	  {
        	  	 mKeyguardEffectView.handleUnlock(this, motionevent);
               handleUnlocked = true;
               mHandler.sendEmptyMessageDelayed(0, mKeyguardEffectView.getUnlockDelay());
               return true;
        	  }   
        }
        else if (1 == i)
        {
           if (Math.abs(motionevent.getX() - downX) > 360F || Math.abs(motionevent.getY() - downY) > 360F)
           {
               mKeyguardEffectView.handleUnlock(this, motionevent);
               handleUnlocked = true;
               mHandler.sendEmptyMessageDelayed(0, mKeyguardEffectView.getUnlockDelay());
               return true;
           }        		
        }   
        
        
        return mKeyguardEffectView.handleTouchEvent(mKeyguardEffectView, motionevent);
    }



  public void onLockScreenWallpaperSet()
  {
    if (this.mKeyguardEffectView != null)
      this.mKeyguardEffectView.update();
  }

  public void screenTurnedOff()
  {
    if (this.mKeyguardEffectView != null)
      this.mKeyguardEffectView.screenTurnedOff();
  }

  public void screenTurnedOn()
  {
    if (this.mKeyguardEffectView != null)
    {
      this.mKeyguardEffectView.screenTurnedOn();
      this.mKeyguardEffectView.startAnimation(this.screenOnAnimation);
    }
  }

  public void setKeyguardMediatorCallback(ViewMediatorCallback paramViewMediatorCallback)
  {
    this.mViewMediatorCallback = paramViewMediatorCallback;
  }

  public void setKeyguardViewManager(StatusBarKeyguardViewManager paramStatusBarKeyguardViewManager)
  {
    this.mStatusBarKeyguardViewManager = paramStatusBarKeyguardViewManager;
  }
}