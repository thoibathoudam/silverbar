package com.silverbar.orders.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.silverbar.orders.domain.Order;
import com.silverbar.orders.exception.OrderNotFoundException;

public class OrderDAO {
	
	// using HashSet to allow faster Order addition and deletion for larger dataset
	private Set<Order> orders = Collections.synchronizedSet(new HashSet<Order>());
	
	public void createOrder(Order order) {
		orders.add(order);
	}

	public Set<Order> findAllLiveOrders() {
		return orders;
	}

	public void delete(Order order) {
		boolean isRemoved = orders.remove(order);
		if(!isRemoved){
			throw new OrderNotFoundException();
		}
	}
}
