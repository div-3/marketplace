package ru.inno.market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.inno.market.core.Catalog;
import ru.inno.market.core.MarketService;
import ru.inno.market.model.Client;
import ru.inno.market.model.Item;
import ru.inno.market.model.PromoCodes;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

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

        assertTrue(marketService.getOrderInfo(clientId).getItems().keySet().contains(item));
    }

    @Test
    @DisplayName("Проверить, что скидка применяется к заказу")
    public void shouldApplyDiscountToOrder(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        marketService.applyDiscountForOrder(clientId, PromoCodes.FIRST_ORDER);

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                        marketService.getOrderInfo(clientId).getTotalPrice());  //Скидка в заказе применилась правильно
    }

    @Test
    @Tag("Negative")
    @DisplayName("Проверить, что скидка не применяется к заказу второй раз")
    public void shouldNotApplyDiscountToOrderMoreThanOnce(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        marketService.applyDiscountForOrder(clientId, PromoCodes.FIRST_ORDER);
        marketService.applyDiscountForOrder(clientId, PromoCodes.FIRST_ORDER);  //Повторное применение скидки

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                marketService.getOrderInfo(clientId).getTotalPrice());
    }

    private double add3ItemsToOrder() {
        Item item1 = catalog.getItemById(1);
        Item item2 = catalog.getItemById(2);
        Item item3 = catalog.getItemById(3);
        marketService.addItemToOrder(item1, clientId);
        marketService.addItemToOrder(item2, clientId);
        marketService.addItemToOrder(item3, clientId);
        return item1.getPrice() + item2.getPrice() + item3.getPrice();
    }

    @Test
    @Tag("Negative")
    @DisplayName("Проверить, что в заказ нельзя добавить товар в количестве, превышающем остаток на складе.")
    public void shouldNotAddMoreThanTotalItemQuantityToOrder(){
        int itemNumber = 1;
        addTotalAmountOfItemToOrder(itemNumber);    //Добавляем весь объём одного товара в заказ

        //Добавление несуществующей единицы товара должно вызывать исключение
        assertThrows(NoSuchElementException.class,
                () -> marketService.addItemToOrder(catalog.getItemById(itemNumber), clientId));
    }

    @Test
    @DisplayName("Проверить, что в заказ можно добавить всё количество определённого товара.")
    public void shouldAddTotalItemQuantityToOrder(){
        int itemNumber = 1;
        itemAndItemCountRecord result = addTotalAmountOfItemToOrder(itemNumber);

        //Сравниваем количество товара в заказе и исходное количество товара на складе
        assertEquals(result.itemCount(), marketService.getOrderInfo(clientId).getItems().get(result.item1()));
    }

    private itemAndItemCountRecord addTotalAmountOfItemToOrder(int itemNumber) {
        Item item1 = catalog.getItemById(itemNumber);       //Получили товар с индексом 1 и забрали 1 единицу со склада
        int itemCount = catalog.getCountForItem(item1) + 1; //Исходное количество товара на складе
        marketService.addItemToOrder(item1, clientId);      //Добавление первой единицы товара в заказ

        //Добавление всего склада
        for (int i = 0; i < itemCount - 1; i++) {
            marketService.addItemToOrder(catalog.getItemById(itemNumber), clientId);
        }
        return new itemAndItemCountRecord(item1, itemCount);
    }

    private record itemAndItemCountRecord(Item item1, int itemCount) {
    }


}
