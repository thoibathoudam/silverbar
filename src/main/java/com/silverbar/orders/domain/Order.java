package com.silverbar.orders.domain;

import com.silverbar.orders.util.ORDER_TYPE;

public class Order {

	private Long userId;
	private Double quantity;
	private Double price;

	private ORDER_TYPE orderType;
	
	public Order(Long userId, Double quantity, Double price, ORDER_TYPE orderType) {
		super();
		this.userId = userId;
		this.quantity = quantity;
		this.price = price;
		this.orderType = orderType;
	}

	public Long getUserId() {
		return userId;
	}

	public Double getQuantity() {
		return quantity;
	}

	public Double getPrice() {
		return price;
	}

	public ORDER_TYPE getOrderType() {
		return orderType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orderType == null) ? 0 : orderType.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (orderType == null) {
			if (other.orderType != null)
				return false;
		} else if (!orderType.equals(other.orderType))
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}
