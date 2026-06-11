package cn.zjicm.transaction.route;

import cn.zjicm.transaction.model.PaymentOrder;
import cn.zjicm.transaction.model.PaymentStatus;
import cn.zjicm.transaction.model.PaymentTargetType;
import cn.zjicm.transaction.model.Product;
import cn.zjicm.transaction.model.ProductCategory;
import cn.zjicm.transaction.model.ProductStatus;
import cn.zjicm.transaction.repository.PaymentOrderRepository;
import cn.zjicm.transaction.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentFulfillmentTest {

    @Autowired
    private MarketplaceRoutes marketplaceRoutes;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Test
    void markPaidUpdatesProductStatusToSold() {
        Product product = new Product();
        product.setTitle("测试商品");
        product.setDescription("用于支付联动测试");
        product.setCategory(ProductCategory.DAILY);
        product.setPrice(new BigDecimal("9.99"));
        product.setLocation("测试地点");
        product.setSellerName("测试同学");
        product.setContact("微信 test");
        product.setStatus(ProductStatus.ON_SALE);
        product = productRepository.save(product);

        PaymentOrder order = paymentOrderRepository.save(new PaymentOrder(
                null,
                PaymentTargetType.PRODUCT,
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                "/products/" + product.getId()
        ));

        marketplaceRoutes.markPaid(order.getOrderNo());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ProductStatus.SOLD);
        assertThat(paymentOrderRepository.findById(order.getOrderNo()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.PAID);
    }
}
