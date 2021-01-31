package com.example.test_watchface;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.List;

public class AccelerometerService extends Service {

//    private final Preferences Preference = new Preferences();     // Gets an instance from the preferences module.
//    private final SystemInformation SystemInformation = new SystemInformation();

    private float x,y,z;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private List<Sensor> listSensor; // used to list all available SENSORS, For debugging
    DecimalFormat decimalformat = new DecimalFormat("#.##");
    private StringBuilder stringBuilder;
    public static final int REQUEST_CODE=101;

    public AccelerometerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

//        WifiManager wMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        WifiManager.WifiLock wifiLock = wMgr.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
//        wifiLock.acquire();
//        PowerManager pMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
////        PowerManager.WakeLock wakeLock = pMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
//        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
//        wakeLock.acquire();

        // Alarm manager initializations
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.i("Alarm setup", "Current system time = " + System.currentTimeMillis());
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + 10000, 10000, pendingIntent);

        // Sensor manager initializations
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stringBuilder = new StringBuilder();

//        listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        for(int i=0; i<listSensor.size();i++){
//            Log.i("Sensor List", "Sensor"+i+listSensor.get(i));
//        }

        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        Thread streamThread = new Thread(new StreamThread()); //Thread for streaming sensor data to PC
        streamThread.start();

        return START_STICKY;
    }

    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];

            String sx = decimalformat.format(x);     // Limits the length of the double to 4 digits
            String sy = decimalformat.format(y);     // Limits the length of the double to 4 digits
            String sz = decimalformat.format(z);     // Limits the length of the double to 4 digits
            final String accelerometerValues = sx + "," + sy + "," + sz;
            stringBuilder.append(accelerometerValues);
            stringBuilder.append("\n");
//            Log.i("Acc service", stringBuilder.toString());
//            Log.i("Acc service", "x="+x+" y="+y+" z="+z);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public int PORT = 12345;
    private String serverIpAddress = "191.168.0.107";
    private boolean connected = false;
    PrintWriter out;

    private int sendLatency = 250; // communication latency in milli-seconds

    // Streaming data to PC
    public class StreamThread implements Runnable {
        Socket socket;
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                socket = new Socket(serverAddr, PORT);
                connected = true;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                while (connected) {
//                    out.printf("%10.2f, 10.2f, 10.2f\n", x,y,z);
                    out.printf(stringBuilder.toString());
                    out.flush();
                    stringBuilder.setLength(0);
                    Log.i("Socket", "streamed data");
                    Thread.sleep(sendLatency);
                }
            } catch(Exception e) {
                Log.e("Error while streaming", e.getMessage(), e);
            } finally {
                try{
                    connected = false;
                    socket.close();
                } catch (Exception a){
                    Log.e("Error at the end of streaming", a.getMessage(), a);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent restartService = new Intent("RestartService");
        sendBroadcast(restartService);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
