package com.ebike.powermonitor;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Build;

import android.view.View;
import android.widget.TextView;

import com.welie.blessed.BluetoothPeripheral;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

public class ServiceCharacteristics {
    final public UUID serviceUUID;

    protected BluetoothPeripheral blePeripheral;
    protected String peripheralName;
    final protected UUID characteristicUUID;
    final protected String msgAction;

    protected Serializable charData;

    protected Intent BroadcastIntent;
    protected View characteristicView;
    protected Context applContext;

    public ServiceCharacteristics(String serviceUUIDs, String characteristicUUIDs) {
        characteristicUUID = BluetoothUUID.fromString(characteristicUUIDs);
        serviceUUID = BluetoothUUID.fromString((serviceUUIDs));
        msgAction = serviceUUIDs + characteristicUUIDs;
        characteristicView = null;
        peripheralName = null;
        charData = null;
    }

    protected void servicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
        final BluetoothGattService service = peripheral.getService(serviceUUID);
        if (service != null) {
            blePeripheral = peripheral;
            peripheralName = peripheral.getName();
            peripheral.setNotify(serviceUUID, characteristicUUID, true);
        }
    }

    public void InitDataView(Context context) {
        InitDataView(context, null);
    }
    public void InitDataView(View dataView) {
        InitDataView(dataView.getContext(), dataView);
    }
    public void InitDataView(Context context, View dataView) {
        applContext = context;
        characteristicView = dataView;

        String packageName = applContext.getPackageName();
        BroadcastIntent = new Intent(msgAction);
        BroadcastIntent.setPackage(packageName);
        if (BroadcastIntent != null) {
            IntentFilter rcvFiler = new IntentFilter(msgAction);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? applContext.RECEIVER_NOT_EXPORTED : 0;
                applContext.registerReceiver(serviceDataReceiver, rcvFiler, flags);
            } else {
                applContext.registerReceiver(serviceDataReceiver, rcvFiler);
            }
        }
    }

    public void unInitialize() {
        applContext.unregisterReceiver(serviceDataReceiver);
    }

    private final BroadcastReceiver serviceDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayData();
        }
    };

    protected void displayData() {
        if ((charData != null) && (characteristicView != null)) {
            String displayText = String.format(Locale.ENGLISH, "%s", charData);
            TextView dataView = (TextView) characteristicView;
            dataView.setText(displayText);
        }
    }

    public void handelCharacteristicUpdate(UUID updateUUID, @NotNull byte[] value) {
        if (updateUUID.equals(characteristicUUID)) {
            getCharacteristicData(value);
            if (BroadcastIntent != null) {
                applContext.sendBroadcast(BroadcastIntent);
            }
            //Timber.d("%s", charData);
        }
    }

    public String getDataString() {
        if (charData == null) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%s\n\nfrom %s", charData, peripheralName);
    }

    @MustBeInvokedByOverriders
    protected void getCharacteristicData(byte[] value) {
    }
}
