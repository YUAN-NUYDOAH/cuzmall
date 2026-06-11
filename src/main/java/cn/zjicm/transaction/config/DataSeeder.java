package cn.zjicm.transaction.config;

import cn.zjicm.transaction.model.ChatMessage;
import cn.zjicm.transaction.model.Product;
import cn.zjicm.transaction.model.ProductCategory;
import cn.zjicm.transaction.model.ProductStatus;
import cn.zjicm.transaction.model.SubstitutePost;
import cn.zjicm.transaction.repository.ChatMessageRepository;
import cn.zjicm.transaction.repository.ProductRepository;
import cn.zjicm.transaction.repository.SubstitutePostRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final SubstitutePostRepository substitutePostRepository;
    private final ChatMessageRepository chatMessageRepository;

    public DataSeeder(ProductRepository productRepository,
                      SubstitutePostRepository substitutePostRepository,
                      ChatMessageRepository chatMessageRepository) {
        this.productRepository = productRepository;
        this.substitutePostRepository = substitutePostRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.save(buildProduct(
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
        productRepository.save(buildProduct(
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
        productRepository.save(buildProduct(
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
        productRepository.save(buildProduct(
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

        SubstitutePost morningClass = substitutePostRepository.save(buildSubstitutePost(
                "媒介经营与管理",
                "周三 08:30-10:00",
                "钱塘校区教学楼 B204",
                new BigDecimal("35"),
                "老师会点名，需要能按时到教室听完整节课。",
                "刘同学",
                LocalDateTime.now().minusHours(4)
        ));
        substitutePostRepository.save(buildSubstitutePost(
                "大学英语视听说",
                "周五 18:30-20:00",
                "桐乡校区 3 号教学楼 301",
                new BigDecimal("45"),
                "需要帮忙签到并记录课堂作业。",
                "周同学",
                LocalDateTime.now().minusDays(1)
        ));

        ChatMessage message = new ChatMessage();
        message.setSubstitutePostId(morningClass.getId());
        message.setSenderName("王同学");
        message.setContent("我周三上午有空，可以确认一下座位和老师要求吗？");
        message.setCreatedAt(LocalDateTime.now().minusHours(3));
        chatMessageRepository.save(message);
    }

    private Product buildProduct(String title, String description, ProductCategory category, BigDecimal price,
                                 String location, String sellerName, String contact, ProductStatus status,
                                 LocalDateTime createdAt) {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setLocation(location);
        product.setSellerName(sellerName);
        product.setContact(contact);
        product.setStatus(status);
        product.setCreatedAt(createdAt);
        return product;
    }

    private SubstitutePost buildSubstitutePost(String courseName, String classTime, String location,
                                               BigDecimal price, String description, String publisherName,
                                               LocalDateTime createdAt) {
        SubstitutePost post = new SubstitutePost();
        post.setCourseName(courseName);
        post.setClassTime(classTime);
        post.setLocation(location);
        post.setPrice(price);
        post.setDescription(description);
        post.setPublisherName(publisherName);
        post.setCreatedAt(createdAt);
        return post;
    }
}
