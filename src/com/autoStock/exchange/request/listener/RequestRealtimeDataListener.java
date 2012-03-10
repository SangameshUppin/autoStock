/**
 * 
 */
package com.autoStock.exchange.request.listener;

import com.autoStock.exchange.request.base.RequestHolder;
import com.autoStock.exchange.results.ExResultMarketData;
import com.autoStock.exchange.results.ExResultHistoricalData.ExResultSetHistoricalData;
import com.autoStock.exchange.results.ExResultMarketData.ExResultSetMarketData;
import com.autoStock.exchange.results.ExResultRealtimeData.ExResultSetRealtimeData;

/**
 * @author Kevin Kowalewski
 *
 */
public interface RequestRealtimeDataListener {
		public void failed(RequestHolder requestHolder);
		public void completed(RequestHolder requestHolder, ExResultSetRealtimeData exResultSetRealtimeData);
}