package basic;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.EncoderMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

enum Mode {
	None,
	BackingFromCollision,
	SteeringFromCollision,
	TooCloseLeft,
	TooFarLeft,
	Travelling
}

public class Main {
	
	EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S2);
	EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S3);
	EV3IRSensor irSensor = new EV3IRSensor(SensorPort.S4);
	
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
		SampleProvider color = colorSensor.getRedMode();
		SampleProvider ir = irSensor.getDistanceMode();
		float[] touchSample = new float[touch.sampleSize()];
		float[] colorSample = new float[color.sampleSize()];
		float[] irSample = new float[ir.sampleSize()]; 
		
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		leftMotor.forward();
		rightMotor.forward();
		operationMode = Mode.Travelling;
		
		while (true) {
				touch.fetchSample(touchSample, 0);
				color.fetchSample(colorSample, 0);
				ir.fetchSample(irSample, 0);
				
				if (touchSample[0] > 0 || leftMotor.isStalled() || colorSample[0] > 0) {
					onFrontCollision();
				}
				
				onIrUpdate(irSample[0]);
				onUpdate();
		}
	}
	
	private void onFrontCollision() {
		if (operationMode == Mode.BackingFromCollision || operationMode == Mode.SteeringFromCollision) {
			return;
		}
		
		leftMotor.backward();
		rightMotor.backward();
		operationMode = Mode.BackingFromCollision;
		lastTimeMark = System.currentTimeMillis();
	}
	
	private void onIrUpdate(float sample) {
		if (sample <= 40) {
			float decrease = (40 - sample) * 5;
			rightMotor.setSpeed(200 - (int)decrease);
			operationMode = Mode.TooCloseLeft;
		} else {
			if (sample > 55) {
				leftMotor.setSpeed(150);
				operationMode = Mode.TooFarLeft;
			} else {
				operationMode = Mode.Travelling;
			}
		}
	}
	
	private void onUpdate() {
		if (Button.ESCAPE.isDown()) {
			System.exit(0);
		}
		
		switch (operationMode) {
			case BackingFromCollision:
				if (System.currentTimeMillis() - lastTimeMark > 1000) {
					operationMode = Mode.SteeringFromCollision;
					leftMotor.flt();
					lastTimeMark = System.currentTimeMillis();
				}
				break;
			
			case SteeringFromCollision:
				if (System.currentTimeMillis() - lastTimeMark > 1000) {
					leftMotor.forward();
					rightMotor.forward();
					operationMode = Mode.Travelling;
				}
				break;
				
			case Travelling:
				if (leftMotor.getSpeed() != 200) {
					leftMotor.setSpeed(200);
				}

				if (rightMotor.getSpeed() != 200) {
					rightMotor.setSpeed(200);
				}
				break;
		}
	}
}