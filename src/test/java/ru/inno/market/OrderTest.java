package ru.inno.market;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.inno.market.core.Catalog;
import ru.inno.market.model.Client;
import ru.inno.market.model.Item;
import ru.inno.market.model.PromoCodes;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты класса MarketService:")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderTest {
    private ru.inno.market.model.Order order;
    private Client client;
    int orderId = 1;
    int itemId = 1;
    Catalog catalog;

    @BeforeEach
    public void setUp(){
        catalog = new Catalog();
        client = new Client(1, "Mike");
        order = new ru.inno.market.model.Order(orderId,client);
    }

    @Test
    @Order(1)
    @DisplayName("Создание заказа.")
    public void shouldCreateOrder(){
        assertNotNull(order);
        assertEquals(client, order.getClient());
        assertEquals(orderId, order.getId());
    }

    @Test
    @Order(2)
    @DisplayName("Получение id заказа.")
    public void shouldGetOrderId(){
        assertEquals(orderId, order.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Получение клиента из заказа.")
    public void shouldGetClientFromOrder(){
        assertEquals(client, order.getClient());
    }

    @Test
    @Order(4)
    @DisplayName("Добавление одного товара к заказу.")
    public void shouldAddItemToOrder(){
        ItemAndItemCountRecord ir = getGoodItem(0);
        order.addItem(ir.item());
        assertTrue(order.getItems().containsKey(ir.item()));  //Проверка, что добавился нужный товар
        assertEquals(1, order.getCart().get(ir.item()));    //Проверка, что добавилась только 1 единица товара
    }

    @Test
    @Order(5)
    @DisplayName("Добавление двух разных товаров к заказу.")
    public void shouldAddTwoItemsToOrder(){
        ItemAndItemCountRecord ir1 = getGoodItem(0);
        ItemAndItemCountRecord ir2 = getGoodItem(ir1.item().getId());
        order.addItem(ir1.item());
        order.addItem(ir2.item());
        assertTrue(order.getItems().containsKey(ir1.item()));  //Проверка, что добавился нужный товар
        assertTrue(order.getItems().containsKey(ir2.item()));  //Проверка, что добавился нужный товар
        assertEquals(1, order.getCart().get(ir1.item()));    //Проверка, что добавилась только 1 единица товара
        assertEquals(1, order.getCart().get(ir2.item()));    //Проверка, что добавилась только 1 единица товара
    }

    //Получение первой единицы товара со склада с остатком более 1 шт.
    private ItemAndItemCountRecord getGoodItem(int currentItemId) {
        Item item;
        int count;
        if (currentItemId < 1000) {
            for (int i = currentItemId + 1; i < 1000; i++) {
                try {
                    item = catalog.getItemById(i);
                    count = catalog.getCountForItem(item) + 1;
                    if ( count > 0) {
                        itemId = i;
                        return new ItemAndItemCountRecord(item, count);
                    }
                } catch (Exception e) {}    //Если товара нет на складе, проверять следующий id
            }
        }
        return null;
    }

    @Test
    @Order(6)
    @DisplayName("Проверить, что в заказ можно добавить всё количество определённого товара.")
    public void shouldAddTotalItemQuantityToOrder(){
        int itemNumber = 0;
        ItemAndItemCountRecord itemRec = getGoodItem(itemNumber);
        ItemAndItemCountRecord result = addTotalAmountOfItemToOrder(itemRec.item());

        //Сравниваем количество товара в заказе и исходное количество товара на складе
        assertEquals(result.itemCount(), order.getCart().get(result.item()));
    }

    //Добавление всего объёма для одного товара в заказ. Возвращает Record(Item, int itemCount).
    private ItemAndItemCountRecord addTotalAmountOfItemToOrder(Item item1) {
        int itemCount = catalog.getCountForItem(item1) + 1; //Исходное количество товара на складе
        order.addItem(item1);                               //Добавление первой единицы товара в заказ

        //Добавление всего склада
        for (int i = 0; i < itemCount - 1; i++) {
            order.addItem(catalog.getItemById(item1.getId()));
        }
        return new ItemAndItemCountRecord(item1, itemCount);
    }

    @Test
    @Order(7)
    @DisplayName("Проверить, что скидка применяется к заказу")
    public void shouldApplyDiscountToOrder(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        order.applyDiscount(PromoCodes.FIRST_ORDER.getDiscount());

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                order.getTotalPrice());  //Скидка в заказе применилась правильно
    }

    private double add3ItemsToOrder() {
        double totalPrice = 0;
        int itemId = 0;
        for (int i = 1; i < 4; i++) {
            Item item = getGoodItem(itemId).item();
            itemId = item.getId();
            order.addItem(item);
            totalPrice += item.getPrice();
        }
        return totalPrice;
    }

    @Test
    @Order(8)
    @DisplayName("Получение корзины из заказа")
    public void shouldGetCartFromOrder(){
        int startItemId = 0;
        ItemAndItemCountRecord ir1 = getGoodItem(startItemId);
        ItemAndItemCountRecord ir2 = getGoodItem(ir1.item().getId());
        ItemAndItemCountRecord ir3 = getGoodItem(ir2.item().getId());
        order.addItem(ir1.item());
        order.addItem(ir2.item());
        order.addItem(ir3.item());

        //Проверка, что в корзине лежит нужный товар, в нужном количестве
        assertTrue(order.getCart().containsKey(ir1.item()));
        assertEquals(1, order.getCart().get(ir1.item()));

        assertTrue(order.getCart().containsKey(ir2.item()));
        assertEquals(1, order.getCart().get(ir2.item()));

        assertTrue(order.getCart().containsKey(ir3.item()));
        assertEquals(1, order.getCart().get(ir3.item()));

        //Проверка, что в корзине лежит только 3 товара.
        assertEquals(3, order.getCart().keySet().size());
    }

    @Test
    @Order(9)
    @DisplayName("Получение общей стоимости корзины в заказе")
    public void shouldGetTotalAmountOfOrder(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара

        assertEquals(totalPrice, order.getTotalPrice());
    }

    @Test
    @Order(10)
    @DisplayName("Получение признака применённой скидки")
    public void shouldGetDiscountStatusFromOrder(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        order.applyDiscount(PromoCodes.FIRST_ORDER.getDiscount());

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()),
                order.getTotalPrice());  //Скидка в заказе применилась правильно
        assertTrue(order.isDiscountApplied());
    }

    @Test
    @Order(11)
    @DisplayName("Проверить функцию equals в классе Order")
    public void shouldProperlyOrderEquals(){
        int startItemId = 0;
        ItemAndItemCountRecord ir1 = getGoodItem(startItemId);
        ItemAndItemCountRecord ir2 = getGoodItem(ir1.item().getId());
        ItemAndItemCountRecord ir3 = getGoodItem(ir2.item().getId());
        order.addItem(ir1.item());
        order.addItem(ir2.item());
        order.addItem(ir3.item());

        //Создание дубликата заказа
        ru.inno.market.model.Order order2 = new ru.inno.market.model.Order(orderId, client);
        order2.addItem(ir1.item());
        order2.addItem(ir2.item());
        order2.addItem(ir3.item());

        assertEquals(order, order2);
    }

    @Test
    @Order(12)
    @Tag("Negative")
    @DisplayName("Проверить, что не создаётся заказ для клиента null.")
    public void shouldNotCreateOrderForNullClient(){
        assertThrows(NoSuchElementException.class, () -> new ru.inno.market.model.Order(2, null));
    }

    @Test
    @Order(13)
    @Tag("Negative")
    @DisplayName("Создание заказа с id < 0.")
    public void shouldNotCreateOrderWithWrongId(){
        assertThrows(NoSuchElementException.class, () -> new ru.inno.market.model.Order(-2, client));
    }

    @Test
    @Order(14)
    @Tag("Negative")
    @DisplayName("Добавление в заказ товара NULL.")
    public void shouldNotAddNullItemToOrder(){
        assertThrows(NoSuchElementException.class, () -> order.addItem(null));
    }

    @Order(15)
    @Tag("Negative")
    @ParameterizedTest(name = "Скидка = {0}")
    @MethodSource("getWrongDiscount")
    @DisplayName("Применение скидки меньше 0 или больше 1")
    public void shouldNotApplyWrongDiscountToOrder(double discount){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара

        assertThrows(NoSuchElementException.class, () -> order.applyDiscount(discount));
    }

    private static double[] getWrongDiscount(){
        return new double[]{-0.1, 1.1};
    }

    @Test
    @Order(16)
    @Tag("Negative")
    @DisplayName("Повторное применение скидки.")
    public void shouldNotApplyDiscountToOrderMoreThanOnce(){
        double totalPrice = add3ItemsToOrder();     //Добавляем 3 разных товара
        order.applyDiscount(PromoCodes.FIRST_ORDER.getDiscount());
        order.applyDiscount(PromoCodes.FIRST_ORDER.getDiscount());  //Повторное применение скидки

        assertEquals(totalPrice * (1 - PromoCodes.FIRST_ORDER.getDiscount()), order.getTotalPrice());
    }

    @Test
    @Order(17)
    @Tag("Negative")
    @DisplayName("Проверить, что в заказ нельзя добавить товар в количестве, превышающем остаток на складе.")
    public void shouldNotAddMoreThanTotalItemQuantityToOrder(){
        int itemNumber = 0;
        ItemAndItemCountRecord itemRec = getGoodItem(itemNumber);
        ItemAndItemCountRecord result = addTotalAmountOfItemToOrder(itemRec.item());    //Добавляем к заказу весь объём товара

        //Добавляем ещё одну единицу товара
        assertThrows(NoSuchElementException.class, () -> order.addItem(catalog.getItemById(itemRec.item().getId())));
    }
}
