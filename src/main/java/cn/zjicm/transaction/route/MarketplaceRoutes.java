package cn.zjicm.transaction.route;

import cn.zjicm.transaction.model.Product;
import cn.zjicm.transaction.model.ProductCategory;
import cn.zjicm.transaction.model.SubstitutePost;
import cn.zjicm.transaction.repository.ProductRepository;
import cn.zjicm.transaction.repository.SubstitutePostRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MarketplaceRoutes {

    private final ProductRepository productRepository;
    private final SubstitutePostRepository substitutePostRepository;

    public MarketplaceRoutes(ProductRepository productRepository, SubstitutePostRepository substitutePostRepository) {
        this.productRepository = productRepository;
        this.substitutePostRepository = substitutePostRepository;
    }

    @ModelAttribute("categories")
    public ProductCategory[] categories() {
        return ProductCategory.values();
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) ProductCategory category,
                       Model model) {
        model.addAttribute("products", productRepository.search(keyword, category));
        model.addAttribute("latestSubstitutePosts", substitutePostRepository.findLatest(3));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedCategory", category);
        return "index";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/publish")
    public String publish(Model model) {
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        return "publish";
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute Product product,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "publish";
        }

        Product savedProduct = productRepository.save(product);
        redirectAttributes.addFlashAttribute("message", "发布成功，商品已进入浙江传媒学院校园交易广场。");
        return "redirect:/products/" + savedProduct.getId();
    }

    @GetMapping("/substitutes")
    public String substitutes(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("substitutePosts", substitutePostRepository.search(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "substitutes";
    }

    @GetMapping("/substitutes/publish")
    public String publishSubstitute(Model model) {
        if (!model.containsAttribute("substitutePost")) {
            model.addAttribute("substitutePost", new SubstitutePost());
        }
        return "substitute-publish";
    }

    @PostMapping("/substitutes")
    public String createSubstitute(@Valid @ModelAttribute SubstitutePost substitutePost,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "substitute-publish";
        }

        SubstitutePost savedPost = substitutePostRepository.save(substitutePost);
        redirectAttributes.addFlashAttribute("message", "发布成功，代课信息已进入校园代课互助。");
        return "redirect:/substitutes/" + savedPost.getId();
    }

    @GetMapping("/substitutes/{id}")
    public String substituteDetail(@PathVariable Long id, Model model) {
        SubstitutePost post = substitutePostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "代课信息不存在"));
        model.addAttribute("substitutePost", post);
        model.addAttribute("messages", substitutePostRepository.findMessagesByPostId(id));
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

        substitutePostRepository.addMessage(id, senderName, content);
        redirectAttributes.addFlashAttribute("chatMessage", "消息已发送。");
        return "redirect:/substitutes/" + id + "#chat";
    }
}
