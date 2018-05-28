
import tijos.framework.networkcenter.dns.TiDNS;
import tijos.framework.platform.wlan.TiWiFi;

import tijos.framework.networkcenter.mqtt.MqttClientListener;

import java.io.IOException;

import tijos.framework.networkcenter.mqtt.MqttClient;
import tijos.framework.networkcenter.mqtt.MqttConnectOptions;
import tijos.framework.networkcenter.mqtt.MqttException;
import tijos.framework.util.Delay;
import tijos.framework.util.logging.Logger;

/**
 * 
 * MQTT Client 例程, 在运行此例程时请确保MQTT Server地址及用户名密码正确
 * 
 * @author TiJOS
 */

/**
 * MQTT 事件监听
 * 
 */
class MqttEventLister implements MqttClientListener {

	@Override
	public void connectComplete(Object userContext, boolean reconnect) {
		Logger.info("MqttEventLister", "connectComplete");

	}

	@Override
	public void connectionLost(Object userContext) {
		Logger.info("MqttEventLister", "connectionLost");

	}

	@Override
	public void onMqttConnectFailure(Object userContext, int cause) {
		Logger.info("MqttEventLister", "onMqttConnectFailure cause = " + cause);

	}

	@Override
	public void onMqttConnectSuccess(Object userContext) {
		Logger.info("MqttEventLister", "onMqttConnectSuccess");

	}

	@Override
	public void messageArrived(Object userContext, String topic, byte[] payload) {
		Logger.info("MqttEventLister", "messageArrived topic = " + topic);
		TiKit600 t600 = (TiKit600) userContext;
		int state = payload[0];
		if (state == 0 || state == '0') {
			state = 0;
		} else if (state == 1 || state == '1') {
			state = 1;
		}

		try {
			t600.print(topic + " "+ state);
			if (topic.equals("led")) {
				t600.ledControl(state > 0 ? true : false);
			} else if (topic.equals("relay")) {
				t600.relayControl(state > 0 ? true : false);

			}
		} catch (Exception ie) {
			ie.printStackTrace();
		}

	}

	@Override
	public void publishCompleted(Object userContext, int msgId, String topic, int result) {
		Logger.info("MqttEventLister",
				"publishCompleted topic = " + topic + " result = " + result + "msgid = " + msgId);

	}

	@Override
	public void subscribeCompleted(Object userContext, int msgId, String topic, int result) {
		Logger.info("MqttEventLister",
				"subscribeCompleted topic = " + topic + " result " + result + "msgid = " + msgId);

	}

	@Override
	public void unsubscribeCompleted(Object userContext, int msgId, String topic, int result) {
		Logger.info("MqttEventLister",
				"unsubscribeCompleted topic = " + topic + "result " + result + "msgid = " + msgId);

	}

}

public class MqttClientDemo {

	public static void main(String args[]) {

		// 启动WLAN及DNS
		try {
			TiWiFi.getInstance().startup(10);
			TiDNS.getInstance().startup();
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		TiKit600 t600 = new TiKit600();

		// MQTT Server 地址,用户名, 密码
		final String broker = "tcp://tijos.mqtt.iot.gz.baidubce.com:1883";
		final String username = "tijos/t600";
		final String password = "OmY2mml5BDxSgZcK4lUGUk7ZDTCThBvyFUZ4ZJ+zWpk=";

		// ClientID
		final String clientId = "mqtt_test_java_tijos";

		// MQTT连接设置
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password);
		// 允许自动重新连接
		connOpts.setAutomaticReconnect(true);

		MqttClient mqttClient = new MqttClient(broker, clientId);

		int qos = 1;

		try {
			t600.init();

			mqttClient.SetMqttClientListener(new MqttEventLister());

			// 连接MQTT服务器
			mqttClient.connect(connOpts, t600);

			// 订阅topic

			// led control
			mqttClient.subscribe("led", qos);
			mqttClient.subscribe("relay", qos);

			Logger.info("MQTTClientDemo", "Subscribe to topic led and relay");

			byte[] ledState = new byte[1];
			byte[] relayState = new byte[1];

			// 发布topic
			while (true) {

				t600.measure();

				Double hum = t600.getHumidity();
				Double temp = t600.getTemperature();
				ledState[0] = (byte) t600.getLEDState();
				relayState[0] = (byte) t600.getRelayState();

				mqttClient.publish("humidity", hum.toString().getBytes(), qos, false);
				mqttClient.publish("temp", temp.toString().getBytes(), qos, false);
				mqttClient.publish("led", ledState, qos, false);
				mqttClient.publish("relay", relayState, qos, false);

				Delay.msDelay(10000);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				mqttClient.close();// release resource
			} catch (MqttException ex) {
				/* ignore */}
		}
	}
}
