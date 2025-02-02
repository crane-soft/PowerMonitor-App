package com.ebike.powermonitor;

import android.widget.TextView;

import java.util.Locale;

public class ParmValueView {
    ParmValueView(String ex) {
        unit = ex;
        Value = 0;
        pLevel = null;
        decimalPlaces = 0;
    }

    ParmValueView(String ex, int decplaces) {
        unit = ex;
        Value = 0;
        pLevel = null;
        decimalPlaces = decplaces;
    }

    public int Value;
    public ParmLevelBar pLevel;
    private String unit;
    private int decimalPlaces;
    public boolean hasLevelBar = false;
    public void setMinMax(int min, int max) {
        if (pLevel != null) {
            int factor = (int) Math.pow(10,decimalPlaces);
            pLevel.setMinMax(min*factor,max*factor);
            hasLevelBar = (max > 0);
        }
    }

    public void UpdateView() {
        if (pLevel != null) {
            switch (decimalPlaces) {
                case 1:
                    pLevel.setText(String.format(Locale.ENGLISH, "%,.1f %s", (double) Value / 10, unit));
                    break;
                case 2:
                    pLevel.setText(String.format(Locale.ENGLISH, "%,.2f %s", (double) Value / 100, unit));
                    break;
                default:
                    pLevel.setText(String.format(Locale.ENGLISH, "%d %s", Value, unit));
                    break;
            }

            if (hasLevelBar) {
                pLevel.setProgess(Value);
            }
        }
    }
}
