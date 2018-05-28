package net.tijos.tikit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.tijos.gas.R;
import net.tijos.tikit.service.MonitorService;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends Activity implements MonitorService.ChangedListener {

    private MonitorService monitorService;

    private ImageView iv_logo;
    private LinearLayout layout_led;
    private TextView tv_led;
    private TextView tv_temp;
    private TextView tv_humi;

    private LinearLayout layout_relay;
    private TextView tv_relay;
    private ToggleButton toggleLed;
    private ToggleButton toggleRelay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.err.println("onCreate");

        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        layout_led = (LinearLayout) findViewById(R.id.layout_led);
        tv_led = (TextView) findViewById(R.id.tv_led);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_humi = (TextView) findViewById(R.id.tv_humi);
        tv_relay = (TextView) findViewById(R.id.tv_relay);

        toggleLed = (ToggleButton) findViewById(R.id.toggleButtonLED);
        toggleRelay = (ToggleButton) findViewById(R.id.toggleButtonRelay);

        MyApplication app = (MyApplication) getApplication();
        monitorService = app.getMonitorService();
        monitorService.registerChangedListener(this);

        tv_humi.setText(app.getMonitorService().getHumidity() + "%");
        tv_temp.setText(app.getMonitorService().getTemperature() + "℃");



        toggleLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int state = monitorService.getLedState() == 1? 0 : 1;
                    monitorService.ledControl(state);

                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        toggleRelay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    int state = monitorService.getRelayState() == 1? 0 : 1;
                    monitorService.relayControl(state);

                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        System.err.println("onDestroy");

        monitorService.unregisterChangedListener(this);

        super.onDestroy();
    }


    @Override
    public void onHumidityChanged(final String humidity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_humi.setText(humidity + "%");
            }
        });
    }

    @Override
    public void onTemperatureChanged(final String temperature) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_temp.setText(temperature + "℃");
            }
        });
    }

    @Override
    public void onReleyChange(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_relay.setText(state);
                if(state.equals("ON"))
                    toggleRelay.setChecked(true);
                else
                    toggleRelay.setChecked(false);
            }
        });
    }

    @Override
    public void onLedChange(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_led.setText(state);
                if(state.equals("ON"))
                    toggleLed.setChecked(true);
                else
                    toggleLed.setChecked(false);
            }
        });
    }
}
