package cn.zjicm.transaction.config;

import cn.zjicm.transaction.model.UserAccount;
import cn.zjicm.transaction.security.MarketUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AuthModelAdvice {

    @ModelAttribute("currentUser")
    public UserAccount currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof MarketUserDetails marketUserDetails) {
            return marketUserDetails.getUserAccount();
        }
        return null;
    }
}
