package mat.agent.reactive.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Order {
	private final LinkedList<Good> goods = new LinkedList<>();
	private int currentPointer = 0;
	private final String id = String.valueOf(OrderIdCounter.increment());
	
	public Order(Good ...goods) {
		this.goods.addAll(Arrays.asList(goods));
	}

	public int count() {
		return goods.size() - currentPointer;
	}

	public boolean isCompleted() {
		return goods.size() - 1 == currentPointer;
	}

	public String getId() {
		return id;
	}

	public Optional<Good> getLastDroppedGood() {
		if (count() > 0) {
			Good goodToPop = goods.get(currentPointer - 1);
			return Optional.of(goodToPop);
		}
		return Optional.empty();
	}

	public Optional<Good> drop() {
		if (goods.size() > 0) {
			Good goodToPop = goods.get(currentPointer);
			currentPointer++;
			return Optional.of(goodToPop);
		}
		return Optional.empty();
	}

	public Optional<Good> getNextGood() {
		if (count() > 0) {
			return Optional.of(goods.get(currentPointer));
		}
		return Optional.empty();
	}

	public List<Good> getNextGoods() {
		return goods.stream().skip(currentPointer).collect(Collectors.toList());
	}

	public Optional<Coordinate> getNextCoordinate() {
		if (count() > 0) {
			return Optional.of(goods.get(currentPointer).getCoordinate());
		}
		return Optional.empty();
	}

	public List<Coordinate> getCoordinates() {
		return goods.stream().map(Good::getCoordinate).collect(Collectors.toList());
	}

	public List<Good> getGoods() {
		return goods;
	}

	public void add(Good good) {
		good.setOrder(this);
		goods.add(good);
	}
}