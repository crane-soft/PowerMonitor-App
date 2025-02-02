package com.ebike.powermonitor;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.welie.blessed.BluetoothBytesParser;

import java.util.Locale;

public class PowerMonitorCharacteristic extends ServiceCharacteristics {

    ParmValueView batteryLevel = new ParmValueView("%");
    ParmValueView batteryVoltage = new ParmValueView("V",1);
    ParmValueView  batteryTemperature = new ParmValueView("°",1);;
    ParmValueView  internTemperature = new ParmValueView("°",1);;
    ParmValueView  externTemperature = new ParmValueView("°",1);;

    private TableLayout dataTable;
    private TextView ConnectStatus;
    private boolean connected;
    private PowerParams Motor = new  PowerParams();
    private PowerParams SolarPanel1 = new  PowerParams();
    private PowerParams SolarPanel2 = new  PowerParams();
    private TextView TimeView;
    private int hours, minutes, seconds;

    public PowerMonitorCharacteristic() {

        super("180F", "2A19");
        connected  = false;
    }

    @Override
    protected void getCharacteristicData(byte[] byteArray) {
        BluetoothBytesParser parser = new BluetoothBytesParser(byteArray);

        int flags = parser.getUInt8();
        
        hours = parser.getUInt8();
        minutes = parser.getUInt8();
        seconds  = parser.getUInt8();

        batteryTemperature.Value =  parser.getSInt16();
        internTemperature.Value =  parser.getSInt16();
        externTemperature.Value =  parser.getSInt16();

        batteryLevel.Value = parser.getSInt16();
        batteryVoltage.Value = parser.getSInt16();

        Motor.getData(parser);
        SolarPanel1.getData(parser);
        SolarPanel2.getData(parser);
    }

    @Override
    protected void displayData() {

        if ((connected == false) && (peripheralName) != null) {
            connected = true;
            ConnectStatus.setText(peripheralName+ " conected");
        }
        batteryTemperature.UpdateView();
        internTemperature.UpdateView();
        externTemperature.UpdateView();

        batteryLevel.UpdateView();
        batteryVoltage.UpdateView();
        Motor.displayData();
        SolarPanel1.displayData();
        SolarPanel2.displayData();

        TimeView.setText( String.format(Locale.ENGLISH, "Time %02d:%02d:%02d", hours,minutes,seconds));
    }

    public void InitDataView(View mainView)  {
        super.InitDataView(mainView.getContext(), null);

        ConnectStatus = mainView.findViewById(R.id.connectionStatus);

        dataTable = (TableLayout) mainView.findViewById(R.id.eBikePowerView);

        AddHeaderRow ("Temperatures");
        batteryTemperature.pLevel = AddParameterRow ("Battery");
        internTemperature.pLevel = AddParameterRow ("Intern");
        externTemperature.pLevel = AddParameterRow ("Extern");

        AddHeaderRow ("Battery");
        batteryLevel.pLevel = AddParameterRow  ("Level");
        batteryVoltage.pLevel = AddParameterRow ("Voltage");

        batteryLevel.setMinMax (0,100);
        batteryVoltage.setMinMax (28,42);

        Motor.InitDataView("Motor",250);
        SolarPanel1.InitDataView("Solar Panel 1",100);
        SolarPanel2.InitDataView("Solar Panel 2",100);

        TimeView = AddHeaderRow ("EnergyTime");

        displayData();
    }

    private TextView AddHeaderRow(String hederText)
    {
        TableRow.LayoutParams twParams = new TableRow.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        twParams.span = 2;
        twParams.topMargin = 3;

        TextView twName = new TextView(applContext);

        twName.setLayoutParams(twParams);
        twName.setGravity(Gravity.CENTER);
        twName.setText(hederText);
        twName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        twName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        twName.setTextColor(Color.BLACK);
        twName.setBackgroundColor(Color.LTGRAY);
        TableRow tr = new TableRow (applContext);
        tr.addView(twName);
        dataTable.addView(tr);
        return twName;
    }
    private ParmLevelBar AddParameterRow(String parmName) {
        // https://www.tutorialspoint.com/how-to-add-table-rows-dynamically-in-android-layout

        TableRow.LayoutParams twParams = new TableRow.LayoutParams();
        twParams.height = TableLayout.LayoutParams.WRAP_CONTENT;

        TextView twName = new TextView(applContext);
        twName.setLayoutParams(twParams);
        twName.setGravity(Gravity.RIGHT);
        twName.setText(parmName + ":");
        twName.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        twName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        twParams.setMargins(20,0,0,0);
        ParmLevelBar plValue = new ParmLevelBar(applContext);
        plValue.setLayoutParams(twParams);

        TableRow tr = new TableRow (applContext);
        tr.addView(twName);
        tr.addView(plValue);

        dataTable.addView(tr);
        return plValue;
    }
    private class PowerParams {

        private ParmValueView current = new ParmValueView("A",2);
        private ParmValueView power = new ParmValueView("W",1);
        private ParmValueView energy = new ParmValueView("Wh",1);

        public void InitDataView(String headerText,  int maxPower) {
            AddHeaderRow (headerText);
            current.pLevel = AddParameterRow  ("Current");
            power.pLevel = AddParameterRow  ("Power");
            energy.pLevel = AddParameterRow  ("Energy");
            power.setMinMax(0,maxPower);
        }
        public void getData(BluetoothBytesParser parser) {
            current.Value = parser.getSInt16();
            power.Value = parser.getSInt16();
            energy.Value = parser.getSInt16();
        }

        public void displayData() {
            current.UpdateView();
            power.UpdateView();
            energy.UpdateView();
        }
    }
}
