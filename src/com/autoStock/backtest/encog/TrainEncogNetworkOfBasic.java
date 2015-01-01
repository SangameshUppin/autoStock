/**
 * 
 */
package com.autoStock.backtest.encog;

import org.encog.engine.network.activation.ActivationTANH;
import org.encog.mathutil.randomize.NguyenWidrowRandomizer;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.CalculateScore;
import org.encog.neural.networks.training.genetic.NeuralGeneticAlgorithm;
import org.encog.neural.networks.training.pso.NeuralPSO;
import org.encog.neural.pattern.FeedForwardPattern;

import com.autoStock.Co;
import com.autoStock.signal.signalMetrics.SignalOfEncog;

/**
 * @author Kevin
 *
 */
public class TrainEncogNetworkOfBasic extends TrainEncogBase {
	private BasicNetwork network;
	private MLTrain train;
	private int expectedIterations;
	
	public TrainEncogNetworkOfBasic(CalculateScore calculateScore, String networkName, int expectedIterations){
		super(calculateScore, networkName);
		network = getMLNetwork();
		this.expectedIterations = expectedIterations;
		
		if (expectedIterations == 0){throw new IllegalArgumentException("Can't hangle 0 expected iterations for Hybrid learning strategy");}
//		train = new NeuralPSO(network, new NguyenWidrowRandomizer(), calculateScore, 128);
		//train.addStrategy(new HybridStrategy(new NeuralSimulatedAnnealing(network, calculateScore, 10, 2, 128), 0.10, (int)expectedIterations/3, (int)expectedIterations/6));
		//train.addStrategy(new HybridStrategy(new NeuralGeneticAlgorithm(network, new NguyenWidrowRandomizer(), calculateScore, 128, 0.10f, 0.25f), 0.10, (int)expectedIterations/3, (int)expectedIterations/6));
		//mlTrainMain.addStrategy(new StopTrainingStrategy(0.01, 10));
		
		new NguyenWidrowRandomizer().randomize(network);
	}
	
	@Override
	public void train(int count, double score){
		train = new NeuralPSO(network, new NguyenWidrowRandomizer(), calculateScore, 256);
		NeuralGeneticAlgorithm neuralGeneticAlgorithm = new NeuralGeneticAlgorithm(network, new NguyenWidrowRandomizer(), calculateScore, 256, 0.10f, 0.40f);
		neuralGeneticAlgorithm.setThreadCount(Runtime.getRuntime().availableProcessors() * 2);
		train.addStrategy(new HybridStrategy(neuralGeneticAlgorithm, 0.01, (int)expectedIterations/2, (int)expectedIterations/2));
		
		for (int i = 0; i < count; i++) {
			train.iteration();
			Co.println("--> Training... " + i + ", " + train.getError());			
			bestScore = Math.max(train.getError(), bestScore);
		}
		
		train.finishTraining();
	}
	
	public BasicNetwork getMLNetwork(){
//		ElmanPattern pattern = new ElmanPattern();
//		JordanPattern pattern = new JordanPattern();
		
		int inputWindow = SignalOfEncog.getInputWindowLength();
		
		FeedForwardPattern pattern = new FeedForwardPattern();
		pattern.setInputNeurons(SignalOfEncog.getInputWindowLength());
		pattern.addHiddenLayer(inputWindow/2);
		pattern.addHiddenLayer(inputWindow/3);
		pattern.addHiddenLayer(inputWindow/4);
		pattern.setOutputNeurons(4);
		pattern.setActivationFunction(new ActivationTANH());
		return (BasicNetwork) pattern.generate();
	}

	@Override
	public boolean saveNetwork() {
		return encogNetworkProvider.saveNetwork(network, networkName);
	}
}
