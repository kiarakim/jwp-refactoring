package kitchenpos.application;

import kitchenpos.domain.menu.MenuGroupRepository;
import kitchenpos.domain.menu.MenuProductRepository;
import kitchenpos.domain.product.ProductRepository;
import kitchenpos.domain.menu.Menu;
import kitchenpos.domain.menu.MenuGroup;
import kitchenpos.domain.menu.MenuProduct;
import kitchenpos.domain.product.Product;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.MenuGroupFixture;
import kitchenpos.fixture.MenuProductFixture;
import kitchenpos.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MenuGroupRepository menuGroupRepository;

    @Autowired
    private MenuProductRepository menuProductRepository;

    private Menu menu;

    @BeforeEach
    void setUp() {
        Product product = productRepository.save(ProductFixture.아메리카노());
        MenuProduct menuProduct = menuProductRepository.save(MenuProductFixture.메뉴_재고(1L, product.getId(), 3));
        MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.음료());
        menu = MenuFixture.아메리카노(menuGroup.getId(), List.of(menuProduct));
    }

    @Test
    void 메뉴를_등록한다() {
        // when
        Menu savedMenu = menuService.create(menu);

        // then
        assertThat(savedMenu).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("id", "menuProducts.seq")
                .isEqualTo(menu);
    }

    @Test
    void 메뉴_가격이_null이면_등록할_수_없다() {
        // given
        menu.setPrice(null);

        // when & then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> menuService.create(menu));
    }

    @Test
    void 메뉴_그룹이_존재하지_않으면_등록할_수_없다() {
        // given
        menu.setMenuGroupId(-1L);

        // when & then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> menuService.create(menu));
    }

    @ParameterizedTest
    @ValueSource(ints = {-5, -1, 18000, 20000})
    void 메뉴_가격이_0보다_작거나_재고_가격보다_크면_등록할_수_없다(int price) {
        // given
        menu.setPrice(BigDecimal.valueOf(price));

        // when & then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> menuService.create(menu));
    }

    @Test
    void 메뉴_목록을_조회한다() {
        // given
        Menu savedMenu = menuService.create(menu);

        // when
        List<Menu> menus = menuService.list();

        // then
        assertThat(menus.get(menus.size() - 1))
                .usingRecursiveComparison()
                .ignoringFields("id", "menuProducts.seq")
                .isEqualTo(savedMenu);
    }
}
