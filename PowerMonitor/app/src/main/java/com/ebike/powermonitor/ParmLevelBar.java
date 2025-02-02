package com.ebike.powermonitor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ParmLevelBar extends  RelativeLayout {
    private TextView valueText;
    private ProgressBar valueLevel;
    private int valueOffs;
    public ParmLevelBar(Context context) {
        super(context);

        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(rlParams);
        //setBackgroundColor(Color.YELLOW);

        valueLevel = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        valueLevel.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        valueLevel.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(150,220,190)));

        valueLevel.setScaleY(8f);
        valueLevel.setMax(100);
        valueLevel.setProgress(0);

        RelativeLayout.LayoutParams pbParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        valueLevel.setLayoutParams(pbParams);

        valueText = new TextView(context);
        valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(30,0,0,0);
        valueText.setLayoutParams(tvParams);

        addView(valueLevel);
        addView(valueText);

    }

    public void  setText(String text)  {
        valueText.setText( (text));
    }
    public void setMinMax (int min, int max) {
        valueOffs = - min;
        valueLevel.setMax(max-min);
    }
    public void setProgess (int progress) {

        valueLevel.setProgress(progress+valueOffs);
    }
}


