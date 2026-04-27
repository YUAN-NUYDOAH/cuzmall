package cn.zjicm.transaction.repository;

import cn.zjicm.transaction.model.Product;
import cn.zjicm.transaction.model.ProductCategory;
import cn.zjicm.transaction.model.ProductStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {

    private final AtomicLong idSequence = new AtomicLong(1000);
    private final List<Product> products = new ArrayList<>();

    public ProductRepository() {
        seedProducts();
    }

    public synchronized List<Product> search(String keyword, ProductCategory category) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        return products.stream()
                .filter(product -> category == null || product.getCategory() == category)
                .filter(product -> normalizedKeyword.isEmpty()
                        || product.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || product.getDescription().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || product.getLocation().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                .toList();
    }

    public synchronized Optional<Product> findById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    public synchronized Product save(Product product) {
        product.setId(idSequence.incrementAndGet());
        product.setStatus(ProductStatus.ON_SALE);
        product.setCreatedAt(LocalDateTime.now());
        products.add(product);
        return product;
    }

    private void seedProducts() {
        products.add(new Product(
                idSequence.incrementAndGet(),
                "九成新 iPad Air 5",
                "上课记笔记和剪片都很流畅，附带保护壳和充电器，屏幕无划痕。",
                ProductCategory.DIGITAL,
                new BigDecimal("2899"),
                "桐乡校区生活区门口",
                "陈同学",
                "微信 zcumedia_chen",
                ProductStatus.ON_SALE,
                LocalDateTime.now().minusHours(2)
        ));
        products.add(new Product(
                idSequence.incrementAndGet(),
                "新闻传播学考研资料",
                "浙江传媒学院学长整理，包含真题、笔记和热点专题，适合备考参考。",
                ProductCategory.BOOK,
                new BigDecimal("68"),
                "钱塘校区图书馆一楼",
                "林同学",
                "QQ 294000123",
                ProductStatus.ON_SALE,
                LocalDateTime.now().minusDays(1)
        ));
        products.add(new Product(
                idSequence.incrementAndGet(),
                "宿舍折叠小桌",
                "可放床上使用，适合看剧、写作业，毕业搬宿舍低价出。",
                ProductCategory.DAILY,
                new BigDecimal("25"),
                "生活区 12 幢楼下",
                "王同学",
                "手机号 13800001111",
                ProductStatus.RESERVED,
                LocalDateTime.now().minusDays(2)
        ));
        products.add(new Product(
                idSequence.incrementAndGet(),
                "佳能相机三脚架",
                "传媒类作业拍摄用过几次，轻便稳定，适合短视频和课堂作业。",
                ProductCategory.DIGITAL,
                new BigDecimal("120"),
                "实验楼 A 座",
                "赵同学",
                "微信 camera_zjicm",
                ProductStatus.ON_SALE,
                LocalDateTime.now().minusDays(3)
        ));
    }
}
