package ru.inno.market;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.inno.market.core.Catalog;
import ru.inno.market.core.MarketService;
import ru.inno.market.model.Client;
import ru.inno.market.model.Item;
import ru.inno.market.model.PromoCodes;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты класса MarketService:")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MarketServiceTest {
    private MarketService marketService;
    private Client client;
    int orderId;
    int itemId = 1;
    Catalog catalog;

    @BeforeEach
    public void setUp(){
        catalog = new Catalog();
        marketService = new MarketService();
        client = new Client(1, "Mike");
        orderId = marketService.createOrderFor(client);
    }

    @Test
    @Order(1)
    @DisplayName("Проверить, что клиент правильно записался в заказ.")
    public void shouldCreateOrder(){
        assertEquals(client, marketService.getOrderInfo(orderId).getClient());
    }

    @Test
    @Order(2)
    @DisplayName("Проверить, что товар добавляется к заказу")
    public void shouldAddItemToOrder(){

        Item item = catalog.getItemById(itemId);
        marketService.addItemToOrder(item, orderId);

        assertTrue(marketService.getOrderInfo(orderId).getItems().containsKey(item));
        assertEquals(1, marketService.getOrderInfo(orderId).getItems().size());
    }

    @Test
    @Order(3)
    @DisplayName("Проверить, что скидка применяется к заказу")
    public void shouldApplyDiscountToOrder(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        marketService.applyDiscountForOrder(orderId, PromoCodes.FIRST_ORDER);

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                        marketService.getOrderInfo(orderId).getTotalPrice());  //Скидка в заказе применилась правильно
    }

    @Test
    @Order(4)
    @DisplayName("Проверить, что в заказ можно добавить всё количество определённого товара.")
    public void shouldAddTotalItemQuantityToOrder(){
        int itemNumber = 1;
        ItemAndItemCountRecord result = addTotalAmountOfItemToOrder(itemNumber);

        //Сравниваем количество товара в заказе и исходное количество товара на складе
        assertEquals(result.itemCount(), marketService.getOrderInfo(orderId).getItems().get(result.item()));
    }

    //Добавление всего объёма для одного товара в заказ. Возвращает Record(Item, int itemCount).
    private ItemAndItemCountRecord addTotalAmountOfItemToOrder(int itemNumber) {
        Item item1 = catalog.getItemById(itemNumber);       //Получили товар с индексом 1 и забрали 1 единицу со склада
        int itemCount = catalog.getCountForItem(item1) + 1; //Исходное количество товара на складе
        marketService.addItemToOrder(item1, orderId);      //Добавление первой единицы товара в заказ

        //Добавление всего склада
        for (int i = 0; i < itemCount - 1; i++) {
            marketService.addItemToOrder(catalog.getItemById(itemNumber), orderId);
        }
        return new ItemAndItemCountRecord(item1, itemCount);
    }

    @Test
    @Order(5)
    @Tag("Negative")
    @DisplayName("Проверить, что скидка не применяется к заказу второй раз")
    public void shouldNotApplyDiscountToOrderMoreThanOnce(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        marketService.applyDiscountForOrder(orderId, PromoCodes.FIRST_ORDER);
        marketService.applyDiscountForOrder(orderId, PromoCodes.FIRST_ORDER);  //Повторное применение скидки

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                marketService.getOrderInfo(orderId).getTotalPrice());
    }

    private double add3ItemsToOrder() {
        double totalPrice = 0;
        for (int i = 1; i < 4; i++) {
            Item item = catalog.getItemById(i);
            marketService.addItemToOrder(item, orderId);
            totalPrice += item.getPrice();
        }
        return totalPrice;
    }

    @Test
    @Order(6)
    @Tag("Negative")
    @DisplayName("Проверить, что в заказ нельзя добавить товар в количестве, превышающем остаток на складе.")
    public void shouldNotAddMoreThanTotalItemQuantityToOrder(){
        int itemNumber = 1;
        addTotalAmountOfItemToOrder(itemNumber);    //Добавляем весь объём одного товара в заказ

        //Добавление несуществующей единицы товара должно вызывать исключение
        assertThrows(NoSuchElementException.class,
                () -> marketService.addItemToOrder(catalog.getItemById(itemNumber), orderId));
    }

    @Test
    @Order(7)
    @Tag("Negative")
    @DisplayName("Проверить, что не создаётся заказ для клиента null.")
    public void shouldNotCreateOrderForNullClient(){
        assertThrows(NoSuchElementException.class, () -> marketService.createOrderFor(null));
    }

    @Test
    @Order(8)
    @Tag("Negative")
    @DisplayName("Проверить, что в заказ не добавляется товар null.")
    public void shouldNotAddNullItemToOrder(){
        assertThrows(NoSuchElementException.class, () -> marketService.addItemToOrder(null, orderId));
    }

    private static int[] getWrongOrders(){
        return new int[] {-1, 2};
    }

    @Order(9)
    @Tag("Negative")
    @ParameterizedTest(name = "Номер заказа = {0}")
    @MethodSource("getWrongOrders")
    @DisplayName("Проверить, что добавление в несуществующий заказ невозможно.")
    public void shouldNotAddToWrongOrder(int id){
        assertThrows(NoSuchElementException.class, () -> marketService.addItemToOrder(catalog.getItemById(1), id));
    }

    @Order(10)
    @Tag("Negative")
    @ParameterizedTest(name = "Номер заказа = {0}")
    @MethodSource("getWrongOrders")
    @DisplayName("Проверить, что нельзя применить скидку к несуществующему заказу.")
    public void shouldNotApplyDiscountToWrongOrder(int id){
        assertThrows(NoSuchElementException.class,
                () -> marketService.applyDiscountForOrder(id, PromoCodes.FIRST_ORDER));
    }

    @Order(11)
    @Tag("Negative")
    @ParameterizedTest(name = "Номер заказа = {0}")
    @MethodSource("getWrongOrders")
    @DisplayName("Проверить, что нельзя получить несуществующий заказ.")
    public void shouldNotGetInfoAboutWrongOrder(int id){
        assertThrows(NoSuchElementException.class, () -> marketService.getOrderInfo(id));
    }

    @Test
    @Order(12)
    @Tag("Negative")
    @DisplayName("Проверить, что нельзя применить скидку null к заказу.")
    public void shouldNotApplyNullDiscountToOrder(){
        assertThrows(NoSuchElementException.class, () -> marketService.applyDiscountForOrder(orderId, null));
    }

}
