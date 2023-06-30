package ru.inno.market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.inno.market.core.Catalog;
import ru.inno.market.core.MarketService;
import ru.inno.market.model.Category;
import ru.inno.market.model.Client;
import ru.inno.market.model.Item;
import ru.inno.market.model.PromoCodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarketServiceTest {
    private MarketService marketService;
    private Client client;
    int clientId;
    int itemId = 1;
    Catalog catalog;

    @BeforeEach
    public void setUp(){
        catalog = new Catalog();
        marketService = new MarketService();
        client = new Client(1, "Mike");
        clientId = marketService.createOrderFor(client);
    }

    @Test
    @DisplayName("Проверить, что клиент правильно записался в заказ")
    public void shouldCreateOrder(){
        assertEquals(client, marketService.getOrderInfo(clientId).getClient());
    }

    @Test
    @DisplayName("Проверить, что товар добавляется к заказу")
    public void shouldAddItemToOrder(){
        Item item = catalog.getItemById(itemId);
        marketService.addItemToOrder(item, clientId);

        assertEquals(true, marketService.getOrderInfo(clientId).getItems().keySet().contains(item));
    }

    @Test
    @DisplayName("Проверить, что скидка применяется к заказу")
    public void shouldApplyDiscountToOrder(){
        Item item1 = catalog.getItemById(1);
        Item item2 = catalog.getItemById(2);
        Item item3 = catalog.getItemById(3);
        double totalPrice = item1.getPrice() + item2.getPrice() + item3.getPrice();
        marketService.addItemToOrder(item1, clientId);
        marketService.addItemToOrder(item2, clientId);
        marketService.addItemToOrder(item3, clientId);
        marketService.applyDiscountForOrder(clientId, PromoCodes.FIRST_ORDER);

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                        marketService.getOrderInfo(clientId).getTotalPrice());
    }

    @Test
    @DisplayName("Проверить, что в заказ можно добавить всё количество определённого товара.")
    public void shouldAddTotalItemQuantityToOrder(){
        Item item1 = catalog.getItemById(1);
        int itemCount = catalog.getCountForItem(item1);
        for (int i = 0; i < itemCount; i++) {
            marketService.addItemToOrder(item1, clientId);
        }

        System.out.println("Количество товара" + itemCount);

        assertEquals(itemCount, marketService.getOrderInfo(clientId).getItems().get(item1));
    }




}
