package com.silverbar.orders.service;

import static com.silverbar.orders.util.ORDER_TYPE.BUY;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.silverbar.orders.dao.OrderDAO;
import com.silverbar.orders.domain.Order;
import com.silverbar.orders.util.ORDER_TYPE;

//This class and its dependencies could be managed by Spring in real project.
public class LiveOrderBoardService {

	private static final String DISPLAY_TEXT = " kg for £";
	
	private OrderDAO orderDAO;

	public LiveOrderBoardService(OrderDAO orderDAO) {
		this.orderDAO = orderDAO;
	}

	public void register(Order order) {
		orderDAO.createOrder(order);
	}

	public Set<Order> getLiveOrders() {
		return orderDAO.findAllLiveOrders();
	}

	public void cancelOrder(Order order) {
		orderDAO.delete(order);
	}

	public Map<Double, Double> getOrderSummary(ORDER_TYPE orderType) {
		Comparator<Order> priceComparator = orderType == BUY
				? comparingDouble(Order::getPrice).reversed() : comparingDouble(Order::getPrice);

		return orderDAO.findAllLiveOrders().stream().filter(o -> o.getOrderType() == orderType)
				.sorted(priceComparator)
				.collect(groupingBy(Order::getPrice, LinkedHashMap::new, summingDouble(Order::getQuantity)));
	}

	public Map<ORDER_TYPE, Map<Double, Double>> getAllOrderSummary() {
		Map<ORDER_TYPE, Map<Double, Double>> ordersByType = orderDAO.findAllLiveOrders().stream().collect(
				groupingBy(Order::getOrderType, groupingBy(Order::getPrice, summingDouble(Order::getQuantity))));

		return ordersByType;
	}

	public List<String> getOrderSummaryAsDisplayText(ORDER_TYPE orderType) {
		Map<Double, Double> orderSumm = getOrderSummary(orderType);
		return orderSumm.entrySet().stream().map(e -> e.getValue() + DISPLAY_TEXT + e.getKey())
				.collect(Collectors.toList());
	}

}
