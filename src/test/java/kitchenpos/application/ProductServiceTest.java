package kitchenpos.application;

import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import kitchenpos.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = ProductFixture.상품_생성("아메리카노", BigDecimal.valueOf(5600));
    }

    @Test
    void 상품을_등록한다() {
        // when
        Product savedProduct = productService.create(product);

        // then
        assertThat(savedProduct).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("id")
                .isEqualTo(product);
    }

    @Test
    void 상품_가격이_null이면_등록할_수_없다() {
        // given
        product.setPrice(null);

        // when & then
        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품_가격이_0보다_작으면_등록할_수_없다() {
        // given
        product.setPrice(BigDecimal.valueOf(-1));

        // when & then
        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품_목록을_조회한다() {
        // given
        Product savedProduct = productService.create(product);

        // when
        Iterable<Product> products = productService.list();

        // then
        assertThat(products).contains(savedProduct);
    }
}
