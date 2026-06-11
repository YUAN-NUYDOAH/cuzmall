package cn.zjicm.transaction.route;

import cn.zjicm.transaction.model.ChatMessage;
import cn.zjicm.transaction.model.Product;
import cn.zjicm.transaction.model.ProductCategory;
import cn.zjicm.transaction.model.PaymentOrder;
import cn.zjicm.transaction.model.PaymentStatus;
import cn.zjicm.transaction.model.PaymentTargetType;
import cn.zjicm.transaction.model.ProductStatus;
import cn.zjicm.transaction.model.SubstitutePost;
import cn.zjicm.transaction.model.UserAccount;
import cn.zjicm.transaction.payment.PaymentConfigurationException;
import cn.zjicm.transaction.payment.WechatNativePayClient;
import cn.zjicm.transaction.payment.WechatPayNotifyResult;
import cn.zjicm.transaction.upload.ImageUploadException;
import cn.zjicm.transaction.upload.ProductImageStorage;
import cn.zjicm.transaction.repository.ChatMessageRepository;
import cn.zjicm.transaction.repository.PaymentOrderRepository;
import cn.zjicm.transaction.repository.ProductRepository;
import cn.zjicm.transaction.repository.SubstitutePostRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Controller
public class MarketplaceRoutes {

    private final ProductRepository productRepository;
    private final SubstitutePostRepository substitutePostRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final WechatNativePayClient wechatNativePayClient;
    private final ProductImageStorage productImageStorage;

    public MarketplaceRoutes(ProductRepository productRepository,
                             SubstitutePostRepository substitutePostRepository,
                             ChatMessageRepository chatMessageRepository,
                             PaymentOrderRepository paymentOrderRepository,
                             WechatNativePayClient wechatNativePayClient,
                             ProductImageStorage productImageStorage) {
        this.productRepository = productRepository;
        this.substitutePostRepository = substitutePostRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.wechatNativePayClient = wechatNativePayClient;
        this.productImageStorage = productImageStorage;
    }

    @ModelAttribute("categories")
    public ProductCategory[] categories() {
        return ProductCategory.values();
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) ProductCategory category,
                       Model model) {
        model.addAttribute("products", productRepository.search(normalizeKeyword(keyword), category));
        model.addAttribute("latestSubstitutePosts", substitutePostRepository.findAll(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedCategory", category);
        return "index";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        model.addAttribute("product", product);
        model.addAttribute("wechatPayConfigured", wechatNativePayClient.isConfigured());
        return "product-detail";
    }

    @GetMapping("/publish")
    public String publish(Model model, UserAccount currentUser) {
        if (!model.containsAttribute("product")) {
            Product product = new Product();
            applyPublisherDefaults(product, currentUser);
            model.addAttribute("product", product);
        }
        return "publish";
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute Product product,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         BindingResult bindingResult,
                         Model model,
                         UserAccount currentUser,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "publish";
        }

        try {
            String imagePath = productImageStorage.store(image);
            if (imagePath != null) {
                product.setImagePath(imagePath);
            }
        } catch (ImageUploadException exception) {
            model.addAttribute("imageUploadError", exception.getMessage());
            return "publish";
        }

        applyPublisherDefaults(product, currentUser);
        product.setStatus(ProductStatus.ON_SALE);
        Product savedProduct = productRepository.save(product);
        redirectAttributes.addFlashAttribute("message", "发布成功，商品已进入浙江传媒学院校园交易广场。");
        return "redirect:/products/" + savedProduct.getId();
    }

    @GetMapping("/substitutes")
    public String substitutes(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("substitutePosts", substitutePostRepository.search(normalizeKeyword(keyword)));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "substitutes";
    }

    @GetMapping("/substitutes/publish")
    public String publishSubstitute(Model model, UserAccount currentUser) {
        if (!model.containsAttribute("substitutePost")) {
            SubstitutePost substitutePost = new SubstitutePost();
            applyPublisherDefaults(substitutePost, currentUser);
            model.addAttribute("substitutePost", substitutePost);
        }
        return "substitute-publish";
    }

    @PostMapping("/substitutes")
    public String createSubstitute(@Valid @ModelAttribute SubstitutePost substitutePost,
                                   BindingResult bindingResult,
                                   UserAccount currentUser,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "substitute-publish";
        }

        applyPublisherDefaults(substitutePost, currentUser);
        SubstitutePost savedPost = substitutePostRepository.save(substitutePost);
        redirectAttributes.addFlashAttribute("message", "发布成功，代课信息已进入校园代课互助。");
        return "redirect:/substitutes/" + savedPost.getId();
    }

    @GetMapping("/substitutes/{id}")
    public String substituteDetail(@PathVariable Long id, Model model) {
        SubstitutePost post = substitutePostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "代课信息不存在"));
        model.addAttribute("substitutePost", post);
        model.addAttribute("messages", chatMessageRepository.findBySubstitutePostIdOrderByCreatedAtAsc(id));
        model.addAttribute("wechatPayConfigured", wechatNativePayClient.isConfigured());
        return "substitute-detail";
    }

    @PostMapping("/substitutes/{id}/messages")
    public String createSubstituteMessage(@PathVariable Long id,
                                          @RequestParam String senderName,
                                          @RequestParam String content,
                                          RedirectAttributes redirectAttributes) {
        substitutePostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "代课信息不存在"));

        if (senderName == null || senderName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("chatError", "昵称和消息内容都不能为空。");
            return "redirect:/substitutes/" + id + "#chat";
        }

        saveChatMessage(id, senderName, content);
        redirectAttributes.addFlashAttribute("chatMessage", "消息已发送。");
        return "redirect:/substitutes/" + id + "#chat";
    }

    @PostMapping("/products/{id}/payments/wechat")
    public String createProductWechatPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            redirectAttributes.addFlashAttribute("paymentError", "该商品当前不可购买，可能已售出或已预订。");
            return "redirect:/products/" + id;
        }
        PaymentOrder order = paymentOrderRepository.save(new PaymentOrder(
                null,
                PaymentTargetType.PRODUCT,
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                "/products/" + product.getId()
        ));
        return createWechatPayment(order, redirectAttributes);
    }

    @PostMapping("/substitutes/{id}/payments/wechat")
    public String createSubstituteWechatPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        SubstitutePost post = substitutePostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "代课信息不存在"));
        PaymentOrder order = paymentOrderRepository.save(new PaymentOrder(
                null,
                PaymentTargetType.SUBSTITUTE,
                post.getId(),
                post.getCourseName(),
                post.getPrice(),
                "/substitutes/" + post.getId()
        ));
        return createWechatPayment(order, redirectAttributes);
    }

    @GetMapping("/payments/{orderNo}")
    public String paymentDetail(@PathVariable String orderNo, Model model) {
        PaymentOrder order = paymentOrderRepository.findById(orderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在"));
        model.addAttribute("paymentOrder", order);
        return "payment-detail";
    }

    @GetMapping(value = "/payments/{orderNo}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> paymentQr(@PathVariable String orderNo) throws Exception {
        PaymentOrder order = paymentOrderRepository.findById(orderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在"));
        if (order.getCodeUrl() == null || order.getCodeUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "支付二维码不存在");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(order.getCodeUrl(), BarcodeFormat.QR_CODE, 260, 260);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/payments/{orderNo}/status")
    @ResponseBody
    public Map<String, Object> paymentStatus(@PathVariable String orderNo) {
        PaymentOrder order = paymentOrderRepository.findById(orderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在"));
        return Map.of(
                "status", order.getStatus().name(),
                "displayName", order.getStatus().getDisplayName(),
                "paid", order.getStatus() == PaymentStatus.PAID
        );
    }

    @PostMapping("/payments/wechat/notify")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, String>> wechatPayNotify(@RequestHeader Map<String, String> headers,
                                                               @RequestBody String body) {
        try {
            WechatPayNotifyResult notifyResult = wechatNativePayClient.parseNotify(headers, body);
            paymentOrderRepository.findById(notifyResult.orderNo())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在"));
            if (notifyResult.paid()) {
                markPaid(notifyResult.orderNo());
            } else {
                markFailed(notifyResult.orderNo());
            }
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (PaymentConfigurationException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("code", "FAIL", "message", exception.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", "微信支付通知处理失败"));
        }
    }

    private String createWechatPayment(PaymentOrder order, RedirectAttributes redirectAttributes) {
        try {
            order.setCodeUrl(wechatNativePayClient.createNativePayment(order));
            paymentOrderRepository.save(order);
            return "redirect:/payments/" + order.getOrderNo();
        } catch (PaymentConfigurationException exception) {
            redirectAttributes.addFlashAttribute("paymentError", exception.getMessage());
        } catch (Exception exception) {
            markFailed(order.getOrderNo());
            redirectAttributes.addFlashAttribute("paymentError", "创建微信支付订单失败，请稍后再试。");
        }
        return "redirect:" + order.getReturnUrl();
    }

    void markPaid(String orderNo) {
        paymentOrderRepository.findById(orderNo).ifPresent(order -> {
            if (order.getStatus() == PaymentStatus.PAID) {
                return;
            }
            order.setStatus(PaymentStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            paymentOrderRepository.save(order);
            fulfillPaymentTarget(order);
        });
    }

    private void fulfillPaymentTarget(PaymentOrder order) {
        if (order.getTargetType() != PaymentTargetType.PRODUCT) {
            return;
        }
        productRepository.findById(order.getTargetId()).ifPresent(product -> {
            product.setStatus(ProductStatus.SOLD);
            productRepository.save(product);
        });
    }

    private void markFailed(String orderNo) {
        paymentOrderRepository.findById(orderNo).ifPresent(order -> {
            order.setStatus(PaymentStatus.FAILED);
            paymentOrderRepository.save(order);
        });
    }

    private ChatMessage saveChatMessage(Long postId, String senderName, String content) {
        ChatMessage message = new ChatMessage();
        message.setSubstitutePostId(postId);
        message.setSenderName(senderName.trim());
        message.setContent(content.trim());
        return chatMessageRepository.save(message);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private void applyPublisherDefaults(Product product, UserAccount currentUser) {
        if (currentUser != null) {
            product.setOwnerUserId(currentUser.getId());
            if (product.getSellerName() == null || product.getSellerName().isBlank()) {
                product.setSellerName(currentUser.getDisplayName());
            }
        }
    }

    private void applyPublisherDefaults(SubstitutePost post, UserAccount currentUser) {
        if (currentUser != null) {
            post.setOwnerUserId(currentUser.getId());
            if (post.getPublisherName() == null || post.getPublisherName().isBlank()) {
                post.setPublisherName(currentUser.getDisplayName());
            }
        }
    }
}
