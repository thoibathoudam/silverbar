package com.silverbar.orders.service;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.silverbar.orders.dao.OrderDAO;
import com.silverbar.orders.domain.Order;

public class LiveOrderBoardService {

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

	public Map<Double, Double> getOrderSummary(String orderType) {
		Comparator<Order> priceComparator = orderType.equalsIgnoreCase("BUY")
				? comparingDouble(Order::getPrice).reversed() : comparingDouble(Order::getPrice);

		return orderDAO.findAllLiveOrders().stream().filter(o -> o.getOrderType().equalsIgnoreCase(orderType))
				.sorted(priceComparator).collect(groupingBy(Order::getPrice, LinkedHashMap::new, summingDouble(Order::getQuantity)));
	}

	public Map<String, Map<Double, Double>> getAllOrderSummary() {
		Map<String, Map<Double, Double>> collect = orderDAO.findAllLiveOrders().stream().collect(
				groupingBy(Order::getOrderType, groupingBy(Order::getPrice, summingDouble(Order::getQuantity))));

		return null;
	}

}
