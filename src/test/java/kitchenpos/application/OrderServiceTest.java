package kitchenpos.application;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderTable;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.MenuGroupFixture;
import kitchenpos.fixture.OrderFixture;
import kitchenpos.fixture.OrderLineItemFixture;
import kitchenpos.fixture.OrderTableFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private MenuGroupDao menuGroupDao;

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private OrderLineItemDao orderLineItemDao;

    @Autowired
    private OrderTableDao orderTableDao;

    private Order order;
    private OrderLineItem orderLineItem;

    @BeforeEach
    void setUp() {
        MenuGroup menuGroup = menuGroupDao.save(MenuGroupFixture.메뉴그룹_생성("그룹"));
        Menu menu = menuDao.save(MenuFixture.메뉴_생성("아메리카노", new BigDecimal(1000), menuGroup.getId(), null));
        OrderTable orderTable = orderTableDao.save(OrderTableFixture.주문테이블(null, 0, false));
        order = orderDao.save(OrderFixture.주문_상품_없이_생성(orderTable.getId(), "COOKING", now(), null));
        orderLineItem = orderLineItemDao.save(OrderLineItemFixture.메뉴와_수량으로_주문_생성(order.getId(), menu.getId(), 3));
        order.setOrderLineItems(List.of(orderLineItem));
    }

    @Test
    void 주문을_등록한다() {
        // when
        Order savedOrder = orderService.create(order);

        // then
        assertSoftly(softly -> {
            softly.assertThat(savedOrder.getOrderStatus()).isNotNull();
            softly.assertThat(savedOrder.getOrderLineItems()).hasSize(1);
            softly.assertThat(savedOrder.getOrderLineItems().get(0).getQuantity())
                    .isEqualTo(orderLineItem.getQuantity());
        });
    }

    @Test
    void 주문_항목이_없는_주문을_등록하면_예외가_발생한다() {
        // given
        order.setOrderLineItems(null);

        // when & then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문_항목의_메뉴id가_존재하지_않는_메뉴면_예외가_발생한다() {
        // given
        order.getOrderLineItems().get(0).setMenuId(100L);

        // when & then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문_항목의_주문테이블id가_존재하지_않는_주문테이블이면_예외가_발생한다() {
        // given
        order.setOrderTableId(100L);

        // when & then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문_목록을_조회한다() {
        // given
        int originSize = orderService.list().size();
        orderService.create(order);

        // when
        List<Order> orders = orderService.list();

        // then
        assertThat(orders).hasSize(originSize + 1);
    }

    @Test
    void 주문_상태를_변경한다() {
        // given
        Order savedOrder = orderService.create(order);

        // when
        Order changedOrder = orderService.changeOrderStatus(savedOrder.getId(), OrderFixture.주문_상태_변경(savedOrder, "MEAL"));

        // then
        assertThat(changedOrder.getOrderStatus()).isEqualTo("MEAL");
    }

    @Test
    void 주문_상태_변경시_존재하지_않는_주문일_경우_예외가_발생한다() {
        // given
        Order savedOrder = orderService.create(order);

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(100L, OrderFixture.주문_상태_변경(savedOrder, "MEAL")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
