package com.ebike.powermonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;

import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;

import com.welie.blessed.PhyOptions;
import com.welie.blessed.PhyType;
import com.welie.blessed.ScanFailure;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import timber.log.Timber;

import static com.welie.blessed.BluetoothBytesParser.asHexString;


class BluetoothHandler {
    // UUIDs for the Device Information service (DIS)
    private static final UUID DIS_SERVICE_UUID = BluetoothUUID.fromString("180A");
    private static final UUID MANUFACTURER_NAME_CHARACTERISTIC_UUID = BluetoothUUID.fromString("2A29");
    private static final UUID MODEL_NUMBER_CHARACTERISTIC_UUID = BluetoothUUID.fromString("2A24");

    // Local variables
    public BluetoothCentralManager central;
    private static BluetoothHandler instance = null;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public final CharacteristicList characteristicList = new CharacteristicList();

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            // Request a higher MTU, iOS always asks for 185
            Timber.d ("ServicesDiscovered %s", peripheral.getName() );

            peripheral.requestMtu(185);

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);
            peripheral.setPreferredPhy(PhyType.LE_2M, PhyType.LE_2M, PhyOptions.S2);

            // Read manufacturer and model number from the Device Information Service
            peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID);
            peripheral.readCharacteristic(DIS_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID);

            peripheral.readPhy();
            characteristicList.servicesDiscovered(peripheral);
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                final boolean isNotifying = peripheral.isNotifying(characteristic);
                Timber.i("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid());
            } else {
                Timber.e("ERROR: Changing notification state failed for %s (%s)", characteristic.getUuid(), status);
            }
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Timber.i("SUCCESS: Writing <%s> to <%s>", asHexString(value), characteristic.getUuid());
            } else {
                Timber.i("ERROR: Failed writing <%s> to <%s> (%s)", asHexString(value), characteristic.getUuid(), status);
            }
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            BluetoothBytesParser parser = new BluetoothBytesParser(value);

            Timber.d("%s", characteristicUUID);
            characteristicList.handelCharacteristicUpdates(characteristicUUID, value);
        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            Timber.i("new MTU set: %d", mtu);
        }
    };

    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            Timber.i("connected to '%s'", peripheral.getName());
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Timber.e("connection '%s' failed with status %s", peripheral.getName(), status);
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Timber.i("disconnected '%s' with status %s", peripheral.getName(), status);

            // Reconnect to this device when it becomes available again
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    central.autoConnectPeripheral(peripheral, peripheralCallback);
                }
            }, 5000);
        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            Timber.i("Found peripheral '%s'", peripheral.getName());
            central.stopScan();
            central.connectPeripheral(peripheral, peripheralCallback);
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Timber.i("bluetooth adapter changed state to %d", state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                central.startPairingPopupHack();
                startScan();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            Timber.i("scanning failed with error %s", scanFailure);
        }
    };

    public static synchronized BluetoothHandler getInstance(Context context, View measurementView) {
        if (instance == null) {
            instance = new BluetoothHandler(context.getApplicationContext(),measurementView);
        }
        return instance;
    }

    private BluetoothHandler(Context context,  View mainView) {
        this.context = context;

        // Plant a tree
        Timber.plant(new Timber.DebugTree());

        characteristicList.InitDataView(mainView);
        // Create BluetoothCentral
        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));

        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
    }

    public void startScan() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                central.scanForPeripheralsWithServices(new UUID[] {
                        BluetoothUUID.fromString("180F")
                });
                //central.scanForPeripheralsWithNames(new String[]{"HELTH-Temper-Device"});
            }
        },1000);
    }
    protected void destroy() {
        characteristicList.unInitialize();
    }

}
