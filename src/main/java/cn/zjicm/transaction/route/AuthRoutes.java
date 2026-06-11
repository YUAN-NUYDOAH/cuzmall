package cn.zjicm.transaction.route;

import cn.zjicm.transaction.model.RegisterForm;
import cn.zjicm.transaction.model.UserAccount;
import cn.zjicm.transaction.repository.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthRoutes {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthRoutes(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(@Valid @ModelAttribute RegisterForm registerForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userAccountRepository.existsByUsername(registerForm.getUsername().trim())) {
            bindingResult.rejectValue("username", "duplicate", "该用户名已被注册");
            return "register";
        }

        UserAccount account = new UserAccount(
                registerForm.getUsername().trim(),
                passwordEncoder.encode(registerForm.getPassword()),
                registerForm.getDisplayName().trim()
        );
        userAccountRepository.save(account);
        redirectAttributes.addFlashAttribute("message", "注册成功，请登录后发布商品或代课信息。");
        return "redirect:/login";
    }
}
