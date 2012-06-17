package com.autoStock.backtest;

import com.autoStock.finance.Account;
import com.autoStock.signal.SignalControl;

/**
 * @author Kevin Kowalewski
 * 
 */
public class BacktestUtils {

	public static String getCurrentBacktestValueGroup() {
		return "--------> Best backtest results: " + 
			SignalControl.pointToSignalLongEntry + "\n" + 
			SignalControl.pointToSignalLongExit + "\n" + 
			SignalControl.pointToSignalShortEntry + "\n" +
			SignalControl.pointToSignalShortExit + "\n" +
			SignalControl.periodLength + "\n" + 
			SignalControl.periodWindow + "\n" + 
			SignalControl.periodAverageForPPC + "\n" + 
			SignalControl.periodAverageForDI + "\n" + 
			SignalControl.periodAverageForCCI + "\n" + 
			SignalControl.periodAverageForMACD + "\n" + 
			SignalControl.periodAverageForTRIX + "\n" +
			SignalControl.weightForPPC + "\n" +
			SignalControl.weightForDI + "\n" + 
			SignalControl.weightForCCI + "\n" + 
			SignalControl.weightForMACD + "\n" + 
			SignalControl.weightForTRIX + "\n" +
			Account.instance.getTransactions() + "\n" + 
			Account.instance.getBankBalance() + "\n";
	}
}