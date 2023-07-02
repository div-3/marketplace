package ru.inno.market.model;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Order {
    private int id;
    private Map<Item, Integer> cart;
    private Client client;

    private double totalPrice;
    private boolean discountApplied;

    public Order(int id, Client client) {
        if (client == null) throw new NoSuchElementException("Попытка создать заказ для клиента NULL!");  //Добавлена защита от передачи клиента NULL
        if (id < 0) throw new NoSuchElementException("Попытка создать заказ c id меньше 0!");  //Добавлена защита от передачи клиента NULL
        this.id = id;
        this.client = client;
        cart = new HashMap<>();
        totalPrice = 0;
        discountApplied = false;
    }

    public Map<Item, Integer> getItems() {
        return cart;
    }

    public int getId() {
        return id;
    }

    public void addItem(Item item) {
        if (item == null)
            throw new NoSuchElementException("Попытка добавить в заказ товар NULL!");  //Добавлена защита от передачи товара NULL
        int counter = cart.getOrDefault(item, 0);
        cart.put(item, ++counter);
        totalPrice += item.getPrice();
    }

    public void applyDiscount(double discount) {
        if (discount < 0 || discount > 1.0) throw new NoSuchElementException("Попытка применить некорректную скидку!");  //Добавлена защита от неправильной скидки
        if (!discountApplied) {
            totalPrice *= (1- discount);
            discountApplied = true;
        }
    }

    public Client getClient() {
        return client;
    }

    public Map<Item, Integer> getCart() {
        return cart;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public boolean isDiscountApplied() {
        return discountApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return getId() == order.getId() && Double.compare(order.getTotalPrice(), getTotalPrice()) == 0 && isDiscountApplied() == order.isDiscountApplied() && Objects.equals(getCart(), order.getCart()) && Objects.equals(getClient(), order.getClient());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCart(), getClient(), getTotalPrice(), isDiscountApplied());
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", cart=" + cart +
                ", client=" + client +
                ", totalPrice=" + totalPrice +
                ", discountApplied=" + discountApplied +
                '}';
    }
}

