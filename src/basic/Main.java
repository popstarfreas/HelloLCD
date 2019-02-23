package basic;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.EncoderMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

enum Mode {
	None,
	BackingFromCollision,
	SteeringFromCollision,
	Travelling
}

public class Main {
	EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S2);
	EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A); 
	EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	Mode operationMode = Mode.None;
	long lastTimeMark = System.currentTimeMillis();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main main = new Main();
		main.start();
	}
	
	public void start() {
		System.out.println("Running.");
		SampleProvider touch = touchSensor.getTouchMode();
		float[] sample = new float[touch.sampleSize()];
		leftMotor.setSpeed(50);
		rightMotor.setSpeed(50);
		operationMode = Mode.Travelling;
		leftMotor.forward();
		rightMotor.forward();
		
		while (true) {
				touch.fetchSample(sample, 0);
				if (sample[0] > 0 || leftMotor.isStalled()) {
					onTouch();
				}
				onUpdate();
		}
	}
	
	private void onTouch() {
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.backward();
		rightMotor.backward();
		operationMode = Mode.BackingFromCollision;
		lastTimeMark = System.currentTimeMillis();
	}
	
	private void onUpdate() {
		switch (operationMode) {
			case BackingFromCollision:
				if (System.currentTimeMillis() - lastTimeMark > 4000) {
					operationMode = Mode.SteeringFromCollision;
					leftMotor.stop();
					lastTimeMark = System.currentTimeMillis();
				}
				break;
			
			case SteeringFromCollision:
				if (System.currentTimeMillis() - lastTimeMark > 4000) {
					rightMotor.stop();
					leftMotor.forward();
					rightMotor.forward();
					operationMode = Mode.Travelling;
				}
				break;
		}
	}
}
