package cn.zjicm.transaction.security;

import cn.zjicm.transaction.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MarketUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public MarketUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .map(MarketUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }
}
