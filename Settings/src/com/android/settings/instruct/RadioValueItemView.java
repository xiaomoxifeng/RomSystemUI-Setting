package com.android.settings.instruct;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import java.util.Map;
import java.util.Stack;
import com.android.settings.R;

public abstract class RadioValueItemView
{
  private final String KEY;
  private final String defValue;
  private RadioGroup.OnCheckedChangeListener mChangeListener = new RadioGroup.OnCheckedChangeListener()
  {
    public void onCheckedChanged(RadioGroup paramRadioGroup, int paramInt)
    {
      if (!TextUtils.isEmpty(KEY))
      {
        String str = (String)paramRadioGroup.getTag(paramInt);
        if (!changeedGroup) {  
	        changeedGroup = true;
	        if (paramRadioGroup == mRadioGroup)
	        {
	              mRadioGroup2.clearCheck(); 
                mRadioGroup3.clearCheck();
                mRadioGroup4.clearCheck();
            }
	        else if (paramRadioGroup == mRadioGroup2)
	        {
	            mRadioGroup.clearCheck(); 
                mRadioGroup3.clearCheck();
                mRadioGroup4.clearCheck();                
            }
	        else if (paramRadioGroup == mRadioGroup3)
	        {
	            mRadioGroup.clearCheck(); 
                mRadioGroup2.clearCheck();
                mRadioGroup4.clearCheck();                
            } 
	        else
            {
	              mRadioGroup.clearCheck(); 
                mRadioGroup2.clearCheck();
                mRadioGroup3.clearCheck();                
            }              
        SystemProperties.set(KEY, str);
        onValueChanged(str);
	        changeedGroup = false;
        }
      }
    }
  };
  private RadioButton mRadioFirst;
  private RadioGroup mRadioGroup;
  private RadioButton mRadioLast;
  private RadioButton mRadioMiddle1;
  private RadioButton mRadioMiddle2;
  private RadioButton mRadioMiddle3;
  private RadioButton mRadioMiddle4;
  private RadioButton mRadioMiddle5;
  private RadioButton mRadioMiddle6;
  private RadioButton mRadioMiddle7;
  private RadioButton mRadioMiddle8; 
  private RadioButton mRadioMiddle9;   
  private RadioButton mRadioMiddle10;
  private RadioButton mRadioMiddle11;
  private RadioButton mRadioMiddle12;
  private RadioButton mRadioMiddle13;
  private RadioButton mRadioMiddle14;
  private RadioButton mRadioMiddle15;
  private RadioButton mRadioMiddle16;
  private RadioButton mRadioMiddle17; 
  private RadioButton mRadioMiddle18;  
  private RadioButton mRadioMiddle19;    
  private RadioGroup mRadioGroup2;
  private RadioGroup mRadioGroup3;
  private RadioGroup mRadioGroup4;
  private View mView;
  private Boolean changeedGroup = false;  

  public RadioValueItemView(Context paramContext, String paramString)
  {
    this(paramContext, paramString, "");
  }

  public RadioValueItemView(Context context, String s, String s1)
  {
      KEY = s;
      defValue = s1;
      mView = LayoutInflater.from(context).inflate(R.layout.default_radio_value_item_view, null);
      mRadioGroup = (RadioGroup)mView.findViewById(R.id.default_radio_group);
      mRadioFirst = (RadioButton)mView.findViewById(R.id.default_radio_button_first);
      mRadioMiddle1 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle1);
      mRadioMiddle2 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle2);
      mRadioMiddle3 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle3);
      mRadioMiddle4 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle4);
      mRadioMiddle5 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle5);
      mRadioMiddle6 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle6);
      mRadioMiddle7 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle7);
      mRadioMiddle8 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle8);      
      mRadioMiddle9 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle9);
      mRadioMiddle10 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle10);
      mRadioMiddle11 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle11);
      mRadioMiddle12 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle12);
      mRadioMiddle13 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle13);
      mRadioMiddle14 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle14);
      mRadioMiddle15 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle15);
      mRadioMiddle16 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle16);
      mRadioMiddle17 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle17);      
      mRadioMiddle18 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle18);    
      mRadioMiddle19 = (RadioButton)mView.findViewById(R.id.default_radio_button_middle19);          
      mRadioLast = (RadioButton)mView.findViewById(R.id.default_radio_button_last);
      mRadioGroup.setOnCheckedChangeListener(mChangeListener);
      mRadioGroup2 = (RadioGroup)mView.findViewById(R.id.default_radio_group_2);
      mRadioGroup2.setOnCheckedChangeListener(mChangeListener);
      mRadioGroup3 = (RadioGroup)mView.findViewById(R.id.default_radio_group_3);
      mRadioGroup3.setOnCheckedChangeListener(mChangeListener); 
      mRadioGroup4 = (RadioGroup)mView.findViewById(R.id.default_radio_group_4);
      mRadioGroup4.setOnCheckedChangeListener(mChangeListener);           
      ((TextView)mView.findViewById(R.id.default_radio_button_text_view)).setText(getTitle());
      setItems();
  }

  private void setItems()
  {
    SingleItem localSingleItem = new SingleItem();
    onCreateSingleItem(localSingleItem);
    if (!TextUtils.isEmpty(KEY))
    {

      if (localSingleItem.mStack.size() > 19)
        setRadioButton4(mRadioMiddle19, (Map.Entry)localSingleItem.mStack.pop());       
      if (localSingleItem.mStack.size() > 18)
        setRadioButton4(mRadioMiddle18, (Map.Entry)localSingleItem.mStack.pop());   
      if (localSingleItem.mStack.size() > 17)
        setRadioButton4(mRadioMiddle17, (Map.Entry)localSingleItem.mStack.pop());   
      if (localSingleItem.mStack.size() > 16)
        setRadioButton4(mRadioMiddle16, (Map.Entry)localSingleItem.mStack.pop());         
      if (localSingleItem.mStack.size() > 15)
      {
        setRadioButton4(mRadioMiddle15, (Map.Entry)localSingleItem.mStack.pop());    
        mRadioGroup4.setVisibility(0);
      }


      if (localSingleItem.mStack.size() > 14)
        setRadioButton3(mRadioMiddle14, (Map.Entry)localSingleItem.mStack.pop());       
      if (localSingleItem.mStack.size() > 13)
        setRadioButton3(mRadioMiddle13, (Map.Entry)localSingleItem.mStack.pop());   
      if (localSingleItem.mStack.size() > 12)
        setRadioButton3(mRadioMiddle12, (Map.Entry)localSingleItem.mStack.pop());   
      if (localSingleItem.mStack.size() > 11)
        setRadioButton3(mRadioMiddle11, (Map.Entry)localSingleItem.mStack.pop());         
      if (localSingleItem.mStack.size() > 10)
      {
        setRadioButton3(mRadioMiddle10, (Map.Entry)localSingleItem.mStack.pop());    
        mRadioGroup3.setVisibility(0);
      }

    
      if (localSingleItem.mStack.size() > 9)
        setRadioButton2(mRadioMiddle9, (Map.Entry)localSingleItem.mStack.pop());       
      if (localSingleItem.mStack.size() > 8)
        setRadioButton2(mRadioMiddle8, (Map.Entry)localSingleItem.mStack.pop());   
      if (localSingleItem.mStack.size() > 7)
        setRadioButton2(mRadioMiddle7, (Map.Entry)localSingleItem.mStack.pop());   
      if (localSingleItem.mStack.size() > 6)
        setRadioButton2(mRadioMiddle6, (Map.Entry)localSingleItem.mStack.pop());         
      if (localSingleItem.mStack.size() > 5)
      {
        setRadioButton2(mRadioMiddle5, (Map.Entry)localSingleItem.mStack.pop());    
        mRadioGroup2.setVisibility(0);
      }

      
      setRadioButton(mRadioLast, (Map.Entry)localSingleItem.mStack.pop());
      if (localSingleItem.mStack.size() > 4)
        setRadioButton(mRadioMiddle4, (Map.Entry)localSingleItem.mStack.pop());
      if (localSingleItem.mStack.size() > 3)
        setRadioButton(mRadioMiddle3, (Map.Entry)localSingleItem.mStack.pop());
      if (localSingleItem.mStack.size() > 2)
        setRadioButton(mRadioMiddle2, (Map.Entry)localSingleItem.mStack.pop());
      if (localSingleItem.mStack.size() > 1)
        setRadioButton(mRadioMiddle1, (Map.Entry)localSingleItem.mStack.pop());
      if (localSingleItem.mStack.size() == 1)
        setRadioButton(mRadioFirst, (Map.Entry)localSingleItem.mStack.pop());
    }
  }




  private void setRadioButton(RadioButton paramRadioButton, Map.Entry<String, String> paramEntry)
  {
    if ((paramRadioButton != null) && (paramEntry != null))
    {
      paramRadioButton.setText((CharSequence)paramEntry.getKey());
      paramRadioButton.setVisibility(0);
      mRadioGroup.setTag(paramRadioButton.getId(), paramEntry.getValue());
      if (SystemProperties.get(KEY, defValue).equals(paramEntry.getValue()))
        mRadioGroup.check(paramRadioButton.getId());
    }
  }


  private void setRadioButton2(RadioButton paramRadioButton, Map.Entry<String, String> paramEntry)
  {
    if ((paramRadioButton != null) && (paramEntry != null))
    {
      paramRadioButton.setText((CharSequence)paramEntry.getKey());
      paramRadioButton.setVisibility(0);
      mRadioGroup2.setTag(paramRadioButton.getId(), paramEntry.getValue());
      if (SystemProperties.get(KEY, defValue).equals(paramEntry.getValue()))
        mRadioGroup2.check(paramRadioButton.getId());
    }
  }


  private void setRadioButton3(RadioButton paramRadioButton, Map.Entry<String, String> paramEntry)
  {
    if ((paramRadioButton != null) && (paramEntry != null))
    {
      paramRadioButton.setText((CharSequence)paramEntry.getKey());
      paramRadioButton.setVisibility(0);
      mRadioGroup3.setTag(paramRadioButton.getId(), paramEntry.getValue());
      if (SystemProperties.get(KEY, defValue).equals(paramEntry.getValue()))
        mRadioGroup3.check(paramRadioButton.getId());
    }
  }

  private void setRadioButton4(RadioButton paramRadioButton, Map.Entry<String, String> paramEntry)
  {
    if ((paramRadioButton != null) && (paramEntry != null))
    {
      paramRadioButton.setText((CharSequence)paramEntry.getKey());
      paramRadioButton.setVisibility(0);
      mRadioGroup4.setTag(paramRadioButton.getId(), paramEntry.getValue());
      if (SystemProperties.get(KEY, defValue).equals(paramEntry.getValue()))
        mRadioGroup4.check(paramRadioButton.getId());
    }
  }


  protected abstract String getTitle();

  public View getView()
  {
    return mView;
  }

  protected abstract void onCreateSingleItem(SingleItem paramSingleItem);

  protected void onValueChanged(String paramString)
  {
  }

  public void setVisibility(int paramInt)
  {
    mView.setVisibility(paramInt);
  }


  public class SingleItem
  {
    private Stack<Map.Entry<String, String>> mStack = new Stack();

    private SingleItem()
    {
    }

    public final void add(String s, final String value)
    {
      final String key = s;
      final String value1 = value;
      java.util.Map.Entry entry = new Map.Entry()
      {
        public String getKey()
        {
          return key;
        }

        public String getValue()
        {
          return value1;
        }

        public String setValue(String paramString)
        {
          return null;
        }

        public Object setValue(Object obj)
        {
            return setValue((String)obj);
        }
      };
      mStack.add(entry);
    }
  }

  
}