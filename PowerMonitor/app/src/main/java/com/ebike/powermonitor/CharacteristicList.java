package com.ebike.powermonitor;

import android.content.Context;
import android.view.View;

import com.welie.blessed.BluetoothPeripheral;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CharacteristicList {

    //public ServiceCharacteristics BatteryLevel = new BatteryLevelCharacteristic();
    //public ServiceCharacteristics BatteryVoltage = new BatteryVoltageCharacteristic();
    public ServiceCharacteristics PowerMonitor = new PowerMonitorCharacteristic();
    private final ServiceCharacteristics charList[] = {
            PowerMonitor
    };

    public void InitDataView(Context context, View mainView) {
        for (ServiceCharacteristics sChar : charList) {
            sChar.InitDataView(context, mainView);
        }
    }
    public void InitDataView(View mainView) {
        for (ServiceCharacteristics sChar : charList) {
            sChar.InitDataView(mainView);
        }
    }
    public void unInitialize()  {
        for (ServiceCharacteristics sChar : charList) {
            sChar.unInitialize();
        }
    }
    public void servicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
        for (ServiceCharacteristics sChar : charList) {
            sChar.servicesDiscovered(peripheral);
        }
    }

    public void handelCharacteristicUpdates(UUID updateUUID, @NotNull byte[] value) {
        for (ServiceCharacteristics sChar : charList) {
            sChar.handelCharacteristicUpdate(updateUUID, value);
        }
    }
}

