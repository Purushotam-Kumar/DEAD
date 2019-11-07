//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.connect.dead;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionsActivity extends AppCompatActivity {
    private static final String TAG = ActionsActivity.class.getSimpleName();
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList();
    BluetoothGatt bluetoothGatt;
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000L;
    int deviceIndex = 0;
    Boolean btScanning = false;
    private Toolbar toolbar;
    private boolean isConnect = false;
    Button mobilizeButton;
    Button imMobilizeButton;
    Button start;
    Button stop;
    TextView statusText;
    private TextView actionStatusText;
    private String deviceAddress = "F6:32:99:AD:86:C1";

    @SuppressLint("ResourceType")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Toast.makeText(this.getApplicationContext(), "Searching for Device ID :" + deviceAddress, 1).show();
        this.statusText = (TextView)this.findViewById(R.id.device_status);
        this.actionStatusText = (TextView)this.findViewById(R.id.current_action);

        this.start = (Button)this.findViewById(R.id.connect);
        this.start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ActionsActivity.this.connectToDeviceSelected();
            }
        });
        this.stop = (Button)this.findViewById(R.id.disconnect);
        this.stop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ActionsActivity.this.disconnectDeviceSelected();
            }
        });
        this.mobilizeButton = (Button)this.findViewById(R.id.unlock);
        this.mobilizeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ActionsActivity.this.triggerMobilizeCommand();
            }
        });
        this.imMobilizeButton = (Button)this.findViewById(R.id.lock);
        this.imMobilizeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ActionsActivity.this.triggerImMobilizeCommand();
            }
        });

        this.btManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        this.btAdapter = this.btManager.getAdapter();
        this.btScanner = this.btAdapter.getBluetoothLeScanner();
        if (this.btAdapter != null && !this.btAdapter.isEnabled()) {
            Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            this.startActivityForResult(enableIntent, 1);
        } else {
            this.startScanning();
        }

        if (this.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
            Builder builder = new Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(17039370, null);
            builder.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    ActionsActivity.this.requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 1);
                }
            });
            builder.show();
        }

    }

    private ScanCallback leScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            if (deviceAddress.equalsIgnoreCase(result.getDevice().getAddress())) {
                ActionsActivity.this.devicesDiscovered.add(result.getDevice());
                ++ActionsActivity.this.deviceIndex;
                ActionsActivity.this.connectToDeviceSelected();
            }

        }
    };
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            ActionsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println(newState);
            switch(newState) {
                case 0:
                    ActionsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ActionsActivity.this.getApplicationContext(), "device disconnected\n", Toast.LENGTH_SHORT).show();
                            ActionsActivity.this.deviceStatusSetup(false);
                        }
                    });
                    break;
                case 2:
                    ActionsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ActionsActivity.this.getApplicationContext(), "device connected\n", Toast.LENGTH_SHORT).show();
                            ActionsActivity.this.deviceStatusSetup(true);
                        }
                    });
                    ActionsActivity.this.bluetoothGatt.discoverServices();
                    break;
                default:
                    ActionsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ActionsActivity.this.getApplicationContext(), "we encounterned an unknown state\n",  Toast.LENGTH_SHORT).show();
                        }
                    });
            }

        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            ActionsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ActionsActivity.this.getApplicationContext(), "device services have been discovered\n",  Toast.LENGTH_SHORT).show();
                }
            });
            ActionsActivity.this.displayGattServices(ActionsActivity.this.bluetoothGatt.getServices());
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                ActionsActivity.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", characteristic);
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            try {
                if (status == 0) {
                    String commandStr = new String(characteristic.getValue());
                    if (!TextUtils.isEmpty(commandStr)) {
                        if (commandStr.equalsIgnoreCase("#OP11?GoCode#@!>>")) {
                            ActionsActivity.this.updateActionStatus(true);
                        } else if (commandStr.equalsIgnoreCase("#OP10?GoCode#@!>>")) {
                            ActionsActivity.this.updateActionStatus(false);
                        }
                    }
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    public ActionsActivity() {
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                this.startScanning();
            } else {
                Toast.makeText(this.getApplicationContext(), "Please allow the permssion before start scanning !!", Toast.LENGTH_SHORT).show();
                this.onBackPressed();
            }
        }

    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    private void deviceStatusSetup(boolean isConnect) {
        this.isConnect = isConnect;
        if (isConnect) {
            this.statusText.setText("Device Connected ");
            this.statusText.setTextColor(this.getResources().getColor(android.R.color.holo_green_light));
        } else {
            this.statusText.setText("Device Dis-Connected ");
            this.statusText.setTextColor(this.getResources().getColor(android.R.color.holo_red_light));
        }

    }

    private void updateActionStatus(boolean isMobile) {
        if (isMobile) {
            this.actionStatusText.setText("Mobilize");
        } else {
            this.actionStatusText.setText("IM-Mobilize");
        }
    }

    private boolean triggerImMobilizeCommand() {
        UUID serviceID = UUID.fromString("00004300-0000-1000-8000-00805f9b34fb");
        UUID writeCharacteristics = UUID.fromString("00004400-0000-1000-8000-00805f9b34fb");
        if (this.bluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        } else {
            BluetoothGattService Service = this.bluetoothGatt.getService(serviceID);
            if (Service == null) {
                Log.e(TAG, "service not found!");
                return false;
            } else {
                BluetoothGattCharacteristic charac = Service.getCharacteristic(writeCharacteristics);
                if (charac == null) {
                    Log.e(TAG, "char not found!");
                    return false;
                } else {
                    byte[] value = HexaUtils.IM_MOBOLIZE;
                    if (true) {
                        charac.setValue("#OP11?GoCode#@!>>");
                    } else {
                        charac.setValue("#OP10?GoCodes$$>>");
                    }
                    boolean status = this.bluetoothGatt.writeCharacteristic(charac);
                    return status;
                }
            }
        }
    }

    private boolean triggerMobilizeCommand() {
        UUID serviceID = UUID.fromString("00004300-0000-1000-8000-00805f9b34fb");
        UUID writeCharacteristics = UUID.fromString("00004400-0000-1000-8000-00805f9b34fb");
        if (this.bluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        } else {
            BluetoothGattService Service = this.bluetoothGatt.getService(serviceID);
            if (Service == null) {
                Log.e(TAG, "service not found!");
                return false;
            } else {
                BluetoothGattCharacteristic charac = Service.getCharacteristic(writeCharacteristics);
                if (charac == null) {
                    Log.e(TAG, "char not found!");
                    return false;
                } else {
                    byte[] value = HexaUtils.MOBOLIZE;
                    if (true) {
                        charac.setValue("#OP10?GoCode#@!>>");
                    } else {
                        charac.setValue("#OP11?GoCodes$$>>");
                    }
                    boolean status = this.bluetoothGatt.writeCharacteristic(charac);
                    return status;
                }
            }
        }
    }

    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        System.out.println(characteristic.getUuid());
    }

    public void connectToDeviceSelected() {
        try {
            Toast.makeText(this.getApplicationContext(), "Trying to connect to device name: " + deviceAddress + "\n", Toast.LENGTH_SHORT).show();
            this.bluetoothGatt = ((BluetoothDevice)this.devicesDiscovered.get(0)).connectGatt(this, false, this.btleGattCallback);
        } catch (NumberFormatException var2) {
            var2.printStackTrace();
        }

    }

    public void disconnectDeviceSelected() {
        Toast.makeText(this.getApplicationContext(), "Disconnecting from device\n", Toast.LENGTH_SHORT).show();
        this.bluetoothGatt.disconnect();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices != null) {
            Iterator var2 = gattServices.iterator();

            while(var2.hasNext()) {
                BluetoothGattService gattService = (BluetoothGattService)var2.next();
                final String uuid = gattService.getUuid().toString();
                System.out.println("Service discovered: " + uuid);
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(ActionsActivity.this.getApplicationContext(), "Service disovered: " + uuid + "\n", Toast.LENGTH_SHORT).show();
                    }
                });
                new ArrayList();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                Iterator var6 = gattCharacteristics.iterator();

                while(var6.hasNext()) {
                    BluetoothGattCharacteristic gattCharacteristic = (BluetoothGattCharacteristic)var6.next();
                    String charUuid = gattCharacteristic.getUuid().toString();
                    System.out.println("Characteristic discovered for service: " + charUuid);
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                        }
                    });
                }
            }

        }
    }

    @SuppressLint("ResourceType")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 1:
                if (grantResults[0] == 0) {
                    System.out.println("coarse location permission granted");
                } else {
                    Builder builder = new Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover device when in the background.");
                    builder.setPositiveButton(17039370, null);
                    builder.setOnDismissListener(new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }

                return;
            default:
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        this.btScanning = true;
        this.deviceIndex = 0;
        this.devicesDiscovered.clear();
        Toast.makeText(this.getApplicationContext(), "Started Scanning\n", Toast.LENGTH_SHORT).show();
        AsyncTask.execute(new Runnable() {
            public void run() {
                ActionsActivity.this.btScanner.startScan(ActionsActivity.this.leScanCallback);
            }
        });
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                ActionsActivity.this.stopScanning();
            }
        }, 10000L);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        Toast.makeText(this.getApplicationContext(), "Stopped Scanning\n", Toast.LENGTH_SHORT).show();
        AsyncTask.execute(new Runnable() {
            public void run() {
                ActionsActivity.this.btScanner.stopScan(ActionsActivity.this.leScanCallback);
            }
        });
    }


    public void showToast(String message){
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
