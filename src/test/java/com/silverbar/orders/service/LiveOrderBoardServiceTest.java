package com.silverbar.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.silverbar.orders.dao.OrderDAO;
import com.silverbar.orders.domain.Order;
import com.silverbar.orders.exception.OrderNotFoundException;

public class LiveOrderBoardServiceTest {

	private LiveOrderBoardService orderBoard;

	@Before
	public void before() {
		orderBoard = new LiveOrderBoardService(new OrderDAO());
	}

	@Test
	public void registerShouldAddOrderToOrderBoard() {
		Order order = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isNotEmpty().hasSize(1);
		assertThat(liveOrders).extracting("userId", "quantity", "price", "orderType")
				.contains(tuple(1L, 1.0, 1.2, "BUY"));
	}

	@Test
	public void registerShouldAddMoreOrderToOrderBoard() {
		Order order1 = new Order(1L, 1.0, 1.2, "BUY");
		Order order2 = new Order(2L, 1.0, 1.2, "BUY");
		orderBoard.register(order1);
		orderBoard.register(order2);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isNotEmpty().hasSize(2);
		assertThat(liveOrders).extracting("userId", "quantity", "price", "orderType")
				.contains(tuple(1L, 1.0, 1.2, "BUY"), tuple(2L, 1.0, 1.2, "BUY"));
	}

	@Test
	public void registerExactlyTheSameOrderShouldOverrideOldOrder() {
		Order order1 = new Order(1L, 1.0, 1.2, "BUY");
		Order order2 = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order1);
		orderBoard.register(order2);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isNotEmpty().hasSize(1);
		assertThat(liveOrders).extracting("userId", "quantity", "price", "orderType")
				.contains(tuple(1L, 1.0, 1.2, "BUY"));
	}
	
	@Test
	public void cancelOrderShouldRemoveOrder() {
		Order order = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).isNotEmpty().hasSize(1);
		orderBoard.cancelOrder(order);
		liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isEmpty();
	}

	@Test
	public void cancelOrderShouldRemoveOnlyTheCancelledOrder() {
		Order order = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order);
		Order order2 = new Order(1L, 2.0, 1.2, "SELL");
		orderBoard.register(order2);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).isNotEmpty().hasSize(2);
		orderBoard.cancelOrder(order);
		liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isNotEmpty()
				.extracting("userId", "quantity", "price", "orderType").containsOnly(tuple(1L, 2.0, 1.2, "SELL"));
	}
	
	@Test
	public void cancelOneOrderShouldRemoveOnlyTheMatchingCancelledOrder() {
		Order order1 = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order1);
		Order order2 = new Order(1L, 2.0, 1.2, "SELL");
		orderBoard.register(order2);
		Order order3 = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order3);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).isNotEmpty().hasSize(2);
		orderBoard.cancelOrder(order1);
		liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isNotEmpty()
				.extracting("userId", "quantity", "price", "orderType").containsOnly(tuple(1L, 2.0, 1.2, "SELL"));
	}

	@Test
	public void cancelNonExistentOrderShouldThrowOrderNotFoundException() {
		Order order = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order);
		Order nonExistentOrder = new Order(-999L, 1.0, 1.2, "SELL");
		assertThatExceptionOfType(OrderNotFoundException.class).isThrownBy(() -> orderBoard.cancelOrder(nonExistentOrder))
				.withMessage("No matching order found");
	}

	@Test
	public void getLiveOrdersShouldReturnAllLiveOrders() {
		Order order1 = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order1);
		Order order2 = new Order(2L, 2.0, 3.6, "BUY");
		orderBoard.register(order2);
		Set<Order> liveOrders = orderBoard.getLiveOrders();
		assertThat(liveOrders).as("# of live orders").isNotEmpty().hasSize(2);
		assertThat(liveOrders).extracting("userId", "quantity", "price", "orderType")
				.contains(tuple(1L, 1.0, 1.2, "BUY"), tuple(2L, 2.0, 3.6, "BUY"));
	}

	@Test
	public void getBuyOrderSummaryShouldReturnSummaryOfOrdersGroupedByPriceInDescOrderOfPrice() {
		Order order1 = new Order(1L, 3.5, 306.0, "BUY");
		orderBoard.register(order1);
		Order order2 = new Order(2L, 1.2, 310.0, "BUY");
		orderBoard.register(order2);
		Order order3 = new Order(3L, 1.5, 307.0, "BUY");
		orderBoard.register(order3);
		Order order5 = new Order(3L, 2.0, 306.0, "BUY");
		orderBoard.register(order5);

		Map<Double, Double> liveOrders = orderBoard.getOrderSummary("BUY");
		assertThat(liveOrders).as("Live BUY orders summary in descending order of price").isNotEmpty().hasSize(3)
				.containsExactly(entry(310.0, 1.2), entry(307.0, 1.5), entry(306.0, 5.5));
	}

	@Test
	public void getBuyOrderSummaryShouldReturnSummaryOfOrdersGroupedByPriceAndSortedInDescOrderOfPrice() {
		Order order1 = new Order(1L, 1.0, 1.2, "BUY");
		orderBoard.register(order1);
		Order order2 = new Order(2L, 2.0, 3.6, "BUY");
		orderBoard.register(order2);
		Order order3 = new Order(3L, 3.0, 1.2, "BUY");
		orderBoard.register(order3);
		Order order4 = new Order(4L, 4.0, 6.0, "BUY");
		orderBoard.register(order4);
		Order order5 = new Order(2L, 2.0, 5.0, "SELL");
		orderBoard.register(order5);
		Map<Double, Double> liveOrders = orderBoard.getOrderSummary("BUY");
		assertThat(liveOrders).as("Live BUY orders summary in descending order of price")
				.containsExactly(entry(6.0, 4.0), entry(3.6, 2.0), entry(1.2, 4.0));
	}

	@Test
	public void getSellOrderSummaryShouldReturnSummaryOfOrdersGroupedByAmountAndSortedInAscOrderOfPrice() {
		Order order1 = new Order(1L, 1.0, 1.2, "SELL");
		orderBoard.register(order1);
		Order order2 = new Order(2L, 2.0, 3.6, "SELL");
		orderBoard.register(order2);
		Order order3 = new Order(3L, 3.0, 1.2, "SELL");
		orderBoard.register(order3);
		Order order4 = new Order(4L, 4.0, 6.0, "SELL");
		orderBoard.register(order4);
		Order order5 = new Order(2L, 2.0, 5.0, "BUY");
		orderBoard.register(order5);
		Map<Double, Double> liveOrders = orderBoard.getOrderSummary("SELL");
		assertThat(liveOrders).as("Live SELL orders summary in ascending order of price")
				.containsExactly(entry(1.2, 4.0), entry(3.6, 2.0), entry(6.0, 4.0));
	}

	@Test
	public void getSellOrderSummaryShouldReturnSummaryOfOrdersGroupedByPriceInDescOrderOfPrice() {
		Order order1 = new Order(1L, 3.5, 306.0, "SELL");
		orderBoard.register(order1);
		Order order2 = new Order(2L, 1.2, 310.0, "SELL");
		orderBoard.register(order2);
		Order order3 = new Order(3L, 1.5, 307.0, "SELL");
		orderBoard.register(order3);
		Order order5 = new Order(3L, 2.0, 306.0, "SELL");
		orderBoard.register(order5);

		Map<Double, Double> liveOrders = orderBoard.getOrderSummary("SELL");
		assertThat(liveOrders).as("Live SELL orders summary in descending order of price").isNotEmpty().hasSize(3)
				.containsExactly(entry(306.0, 5.5), entry(307.0, 1.5), entry(310.0, 1.2));
	}

	@Test
	public void getAllOrderSummaryShouldReturnSummaryOfOrdersGroupedByAmountAndSorted() {
		Order order1 = new Order(1L, 1.0, 1.2, "SELL");
		orderBoard.register(order1);
		Order order2 = new Order(2L, 2.0, 3.6, "BUY");
		orderBoard.register(order2);
		Order order3 = new Order(3L, 3.0, 1.2, "SELL");
		orderBoard.register(order3);
		Order order4 = new Order(4L, 4.0, 6.0, "SELL");
		orderBoard.register(order4);
		Order order5 = new Order(2L, 2.0, 5.0, "BUY");
		orderBoard.register(order5);
		Order order6 = new Order(1L, 9.0, 3.5, "BUY");
		orderBoard.register(order6);
		Map<String, Map<Double, Double>> liveOrders = orderBoard.getAllOrderSummary();
		//assertThat(liveOrders.get("SELL")).as("SELL orders sorted in ascending price").extracting("price", "quantity")
		//		.containsExactly(tuple(1.2, 4.0), tuple(6.0, 4));
		//assertThat(liveOrders.get("BUY")).as("BUY orders sorted in descending price").extracting("price", "quantity")
		//		.containsExactly(tuple(5.0, 2.0), tuple(3.6, 2), tuple(3.5, 9));
	}

	@Test
	public void getDisplayShouldReturnTheDisplayString() {
		Order order1 = new Order(1L, 1.0, 1.2, "SELL");
		orderBoard.register(order1);
		Map<Double, Double> orderLines = orderBoard.getOrderSummary("SELL");
		// assertThat(orderLines.get(0).getDisplayText()).as("Display Text
		// format").isEqualTo("1.0 kg for £1.2");
	}
}
