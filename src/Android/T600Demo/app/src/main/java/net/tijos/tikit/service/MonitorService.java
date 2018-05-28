package net.tijos.tikit.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;

import net.tijos.tikit.MainActivity;
import net.tijos.gas.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Mars on 2017/10/24.
 */

public class MonitorService extends Service implements MqttCallback {

    private boolean isConnect = false;
    private boolean isAlarm = false;

    private static final int NOTIFY_ID = 1;

    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String[] TOPICS = new String[]{"temp", "humidity", "led", "relay"};

    private MqttClient client;

    private List<ChangedListener> changedListeners = new ArrayList<>();

    private String temperature;
    private String humidity;

    private int ledState ;
    private int relayState;

    public String getHumidity() {
        return this.humidity;
    }

    public String getTemperature() {
        return this.temperature;
    }

    public int getLedState() {
        return this.ledState;
    }

    public int getRelayState() {
        return this.relayState;
    }


    public interface ChangedListener {
        void onHumidityChanged(String humidity);
        void onTemperatureChanged(String temperature);
        void onReleyChange(String state);
        void onLedChange(String state);
    }


    public void registerChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void unregisterChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }

    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public MonitorService getService() {
            return MonitorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.err.println("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.err.println("onStartCommand");


        return START_STICKY;
    }


    public boolean isConnect() {
        return client.isConnected() ? isConnect : false;
    }

    public boolean connect(String broker, String username, String passwprd) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        client = new MqttClient(broker, CLIENT_ID, persistence);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(username);
        options.setPassword(passwprd.toCharArray());
        client.setCallback(this);
        client.connect(options);
        client.subscribe(TOPICS);

        isConnect = true;
        return true;
    }

    public void ledControl(int state) throws MqttException {
        if (client != null) {
            MqttMessage message = new MqttMessage();
            byte [] ctrl = new  byte[1];
            ctrl[0] = (byte)state;

            message.setPayload(ctrl);
            message.setQos(1);
            message.setRetained(false);

            client.publish("led", message);

        }
    }

    public void relayControl(int state) throws MqttException {
        if (client != null) {
            MqttMessage message = new MqttMessage();
            byte [] ctrl = new  byte[1];
            ctrl[0] = (byte)state;

            message.setPayload(ctrl);
            message.setQos(1);
            message.setRetained(false);

            client.publish("relay", message);

        }
    }



    @Override
    public void onDestroy() {

        System.err.println("onDestroy");

        isConnect = false;
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.err.println(topic + " - " + new String(message.getPayload()));
        String state = "";
        switch (topic) {
            case "temp":
                this.temperature = new String(message.getPayload());
                for (ChangedListener listener : changedListeners) {
                    listener.onTemperatureChanged(this.temperature);
                }
                break;
            case "humidity":
                this.humidity = new String(message.getPayload());
                for (ChangedListener listener : changedListeners) {
                    listener.onHumidityChanged(this.humidity);
                }
                break;
            case "led":
                this.ledState = message.getPayload()[0];
                if(ledState == 0 || ledState == '0') {
                    state = "OFF";
                    ledState = 0;
                }
                if(ledState == 1 || ledState == '1') {
                    state = "ON";
                    ledState = 1;
                }

                for (ChangedListener listener : changedListeners) {
                    listener.onLedChange(state);
                }

                break;
            case "relay":
                this.relayState  = message.getPayload()[0];
                state = "";
                if(relayState == 0 || relayState == '0') {
                    state = "OFF";
                    relayState = 0;
                }
                if(relayState == 1 || relayState == '1') {
                    state = "ON";
                    relayState = 1;
                }
                for (ChangedListener listener : changedListeners) {
                    listener.onReleyChange(state);
                }
                break;
            default:
                break;

        }

        notifyChange();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private void notifyChange() {
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder//.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("T600Demo working!") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("Listening...\nTemp: " + this.temperature + "℃\nHumi: " + this.humidity + "%") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        builder.setStyle(new Notification.BigTextStyle().bigText("Listening...\nTemp: " + this.temperature + "℃\nHumi: " + this.humidity + "%"));
        Notification notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_ALL; //设置为默认的声音
        startForeground(NOTIFY_ID, notification);// 开始前台服务
    }

}
