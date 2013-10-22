package com.autoStock.guage;

import java.util.Arrays;

import com.autoStock.Co;
import com.autoStock.signal.SignalDefinitions.SignalBounds;
import com.autoStock.tools.ArrayTools;
import com.autoStock.tools.MathTools;

/**
 * @author Kevin Kowalewski
 *
 */
public class GuageOfPeakAndTrough extends GuageBase {
	private double upperBounds = 8;
	private double lowerBounds = -10;
	private double currentValue;
	
	public GuageOfPeakAndTrough(SignalGuage signalGuage, double[] arrayOfValues) {
		super(signalGuage, arrayOfValues);
		currentValue = arrayOfValues[arrayOfValues.length-1];
	}

	@Override
	public boolean isQualified() {
		if (arrayOfValues.length >= 10){
			if (signalGuage.signalBounds == SignalBounds.bounds_upper){
				return hasPeaked();
			}else if (signalGuage.signalBounds == SignalBounds.bounds_lower){
				return hasTroughed();
			}
		}
		
		return false;
	}
	
	private boolean hasPeaked(){
		boolean hasPeaked = false;
		
		if (getIndexOfMax() == 7){
			if (MathTools.isDecreasing(Arrays.copyOfRange(arrayOfValues, 6, 10), 1, false)){
				if (currentValue >= upperBounds){
					return true;
				}
			}
		}
		
		return hasPeaked;
	}
	
	private boolean hasTroughed(){
		boolean hasTroughed = false;
		
		Co.println("--> Index: " + getIndexOfMin());
		
		if (getIndexOfMin() == 6){
			if (MathTools.isIncreasing(Arrays.copyOfRange(arrayOfValues, 6, 10), 1, false)){
				if (currentValue <= lowerBounds){
					return true;
				}
			}
		}	
		
		return hasTroughed;
	}
	
	private int getIndexOfMax(){
		int maxIndex = 0;
		double maxValue = Double.NEGATIVE_INFINITY;
		
		for (int i=0; i<arrayOfValues.length; i++){
			double value = arrayOfValues[i];
			
			if (value >= maxValue){
				maxIndex = i;
				maxValue = value;
			}
		}
		
		return maxIndex;
	}
	
	private int getIndexOfMin(){
		int minIndex = 0;
		double minValue = Double.POSITIVE_INFINITY;
		
		for (int i=0; i<arrayOfValues.length; i++){
			double value = arrayOfValues[i];
			
			if (value <= minValue){
				minIndex = i;
				minValue = value;
			}
		}
		
		return minIndex;
	}
}