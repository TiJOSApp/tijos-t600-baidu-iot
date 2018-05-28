import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.devicecenter.TiUART;
import tijos.framework.platform.TiSettings;
import tijos.framework.platform.peripheral.ITiKeyboardListener;
import tijos.framework.platform.peripheral.TiKeyboard;
import tijos.framework.sensor.button.ITiButtonEventListener;
import tijos.framework.sensor.button.TiButton;
import tijos.framework.sensor.dht.TiDHT;
import tijos.framework.transducer.led.TiLED;
import tijos.framework.transducer.oled.TiOLED_UG2864;
import tijos.framework.transducer.relay.TiRelay1CH;
import tijos.framework.util.Delay;

class TouchListener implements ITiButtonEventListener {
	TiRelay1CH _relay;
	TiLED _led;
	TiOLED_UG2864 _oled;

	public TouchListener(TiRelay1CH relay, TiLED led, TiOLED_UG2864 oled) {
		this._relay = relay;
		this._led = led;
		this._oled = oled;
	}

	@Override
	public void onPressed(TiButton button) {
		try {
			this._relay.turnOn();
			this._led.turnOn();
			this._oled.print(3, 0, "touch:onPressed ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("touch:onPressed");
	}

	@Override
	public void onReleased(TiButton button) {
		try {
			this._relay.turnOff();
			this._led.turnOff();
			this._oled.print(3, 0, "touch:onReleased");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("touch:onReleased");
	}
}

public class TiKit600 {

	TiDHT dht11;
	TiLED led;
	TiOLED_UG2864 oled;
	TiRelay1CH relay;

	public void init() throws IOException {
		TiGPIO gpio0 = TiGPIO.open(0, 2, 3, 4, 5);
		TiI2CMaster i2cm0 = TiI2CMaster.open(0);

		relay = new TiRelay1CH(gpio0, 2);
		dht11 = new TiDHT(gpio0, 3);
		led = new TiLED(gpio0, 5);
		TiButton touch = new TiButton(gpio0, 4, true);
		oled = new TiOLED_UG2864(i2cm0, 0x3c);

		touch.setEventListener(new TouchListener(relay, led, oled));

		oled.turnOn();
		oled.clear();

		oled.print(0, 0, "TiKit-T600 Demo");
	}

	public void measure() throws IOException {
		dht11.measure();
		oled.print(1, 0, "Temp:" + dht11.getTemperature() + " C");
		oled.print(2, 0, "Humi:" + dht11.getHumidity() + " RH");
	}

	public int getLEDState() {
		return led.isTurnedOn() ? 1 : 0;
	}

	public int getRelayState() {
		return relay.isTurnedOn() ? 1 : 0;
	}

	public double getTemperature() {
		return dht11.getTemperature();
	}

	public double getHumidity() {
		return dht11.getHumidity();
	}

	public void ledControl(boolean state) throws IOException {
		if (state) {
			print("led turn on     ");
			this.led.turnOn();
		} else {
			print("led turn off    ");
			this.led.turnOff();
		}
	}

	public void relayControl(boolean state) throws IOException {
		if (state) {
			print("relay turn on    ");
			this.relay.turnOn();
		} else {
			print("relay turn off    ");
			this.relay.turnOff();
		}
	}

	public void print(String msg) throws IOException {
		oled.print(3, 0, msg);
	}
}
