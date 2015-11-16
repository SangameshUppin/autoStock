package com.autoStock.signal.signalMetrics;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import com.autoStock.Co;
import com.autoStock.algorithm.AlgorithmBase;
import com.autoStock.backtest.encog.TrainEncogSignal.EncogNetworkType;
import com.autoStock.position.PositionDefinitions.PositionType;
import com.autoStock.signal.SignalBase;
import com.autoStock.signal.SignalDefinitions.SignalMetricType;
import com.autoStock.signal.SignalDefinitions.SignalParameters;
import com.autoStock.signal.SignalDefinitions.SignalPointType;
import com.autoStock.signal.SignalPoint;
import com.autoStock.signal.extras.EncogFrame;
import com.autoStock.signal.extras.EncogInputWindow;
import com.autoStock.signal.extras.EncogNetworkCache;
import com.autoStock.signal.extras.EncogNetworkProvider;
import com.autoStock.signal.extras.EncogSubframe;
import com.autoStock.tools.MathTools;
import com.autoStock.trading.types.Position;
import com.google.common.util.concurrent.MoreExecutors;
import com.rits.cloning.Cloner;

/**
 * @author Kevin Kowalewski
 * 
 */
public class SignalOfEncog extends SignalBase {
	public static EncogNetworkType encogNetworkType = EncogNetworkType.basic;
	public static final int INPUT_LENGTH = 153;
	private static final double NEURON_THRESHOLD = 0.95;
	public static final int INPUT_WINDOW_PS = 30;
	private static final boolean HAS_DELTAS = false;
	private String networkName;
	private MLRegression basicNetwork;
	private EncogInputWindow encogInputWindow;

	public SignalOfEncog(SignalParameters signalParameters, AlgorithmBase algorithmBase) {
		super(SignalMetricType.metric_encog, signalParameters, algorithmBase);
	}
	
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}
	
	public void setNetwork(MLRegression network, int which) {
		this.basicNetwork = network;
		EncogNetworkCache.getInstance().remove(networkName);
	}

	@Override
	public SignalPoint getSignalPoint(Position position) {
		SignalPoint signalPoint = new SignalPoint();
		
		if (basicNetwork == null){
			readNetwork();
		}
		
		if (encogInputWindow == null || basicNetwork == null){
//			Co.println("--> No network! " + (encogInputWindow == null) + ", " + (basicNetwork == null));
			return signalPoint;
		}
		
		double[] inputWindow = encogInputWindow.getAsWindow(true);
		MLData input = new BasicMLData(inputWindow);
		
		//Co.println(encogInputWindow.describeContents());

		if (inputWindow.length != basicNetwork.getInputCount()) {
			Co.println(encogInputWindow.describeContents());
			try {Thread.sleep(50);}catch(InterruptedException e){}
			throw new IllegalArgumentException("Input sizes don't match, supplied, needed: " + inputWindow.length + ", " + basicNetwork.getInputCount());
		}

//		for (int i = 0; i < inputWindow.length; i++) {
//			//Co.print(" " + inputWindow[i]);
//			input.add(i, inputWindow[i]);
//		}
	
		MLData output = basicNetwork.compute(input);

		double valueForLongEntry = output.getData(0);
		double valueForShortEntry = output.getData(1);
//		double valueForReeentry = output.getData(2);
		double valueForLongExit = output.getData(2);
		double valueForShortExit = output.getData(3);
		double valueForNoAction = output.getData(4);
		
//		Co.println("--> Values: " + valueForLongEntry + ", " + valueForShortEntry + ", " + valueForLongExit + ", " + valueForShortExit);
		
		int count = 0;

		if (valueForLongEntry >= NEURON_THRESHOLD) {
			signalPoint.signalPointType = SignalPointType.long_entry;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
			count++;
		} else if (valueForShortEntry >= NEURON_THRESHOLD) {
			signalPoint.signalPointType = SignalPointType.short_entry;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
			count++;
		}
//		else if (valueForReeentry >= NEURON_THRESHOLD) {
//			signalPoint.signalPointType = SignalPointType.reentry;
//			signalPoint.signalMetricType = SignalMetricType.metric_encog;
//			count++;
//		} 
		else if (valueForLongExit >= NEURON_THRESHOLD) {
			signalPoint.signalPointType = SignalPointType.long_exit;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
			count++;
		} else if (valueForShortExit >= NEURON_THRESHOLD) {
			signalPoint.signalPointType = SignalPointType.short_exit;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
			count++;
		} else if (valueForNoAction >= NEURON_THRESHOLD){
			//pass
		}
		
		if (count > 1){signalPoint = new SignalPoint();} 
		
		return signalPoint;
	}

	private void readNetwork() {
		MLRegression cachedNetwork = (MLRegression) EncogNetworkCache.getInstance().get(networkName, true);
		
		if (cachedNetwork == null){
			if (encogNetworkType == EncogNetworkType.basic){
				basicNetwork = new EncogNetworkProvider().getBasicNetwork(networkName);
			}else if (encogNetworkType == EncogNetworkType.neat){
				basicNetwork = new EncogNetworkProvider().getNeatNetwork(networkName);
			}
			EncogNetworkCache.getInstance().put(networkName, basicNetwork);
		}else{
			basicNetwork = cachedNetwork;
		}
	}

	public void setInput(EncogInputWindow encogInputWindow) {
		if (encogInputWindow == null){throw new IllegalArgumentException("Can't set a null EncogInputWindow");}
		this.encogInputWindow = encogInputWindow;
	}
	
	public EncogInputWindow getInputWindow(){
		return encogInputWindow;
	}
	
	public static int getInputWindowLength(){
		return INPUT_LENGTH;
	}

	@Override
	public void setInput(double value) {
		throw new NoSuchMethodError("Don't call this");
	}
	
	@Override
	public void reset() {
		super.reset();
		encogInputWindow = null;
	}

	public boolean isLongEnough(SignalBase... arrayOfSignalBase) {
		for (SignalBase signalBase : arrayOfSignalBase){
			if (signalBase.listOfNormalizedValuePersist.size() <= INPUT_WINDOW_PS + (HAS_DELTAS ? 1 : 0)){
				//Co.println("--> Not long enough: " + signalBase.getClass().getSimpleName());
				return false;
			}
		}
		
		return true;
	}
}
