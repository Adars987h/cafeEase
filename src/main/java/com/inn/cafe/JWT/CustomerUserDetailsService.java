package com.inn.cafe.JWT;

import com.inn.cafe.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class CustomerUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    @Autowired
    UserDao userDao;

    // No instance state. This bean is a singleton shared by every concurrent request; it
    // used to keep "the last user loaded" in a field, so under load one customer's cart,
    // orders, bills - and even the role baked into a freshly issued token - could come from
    // another customer's request. The current user now lives on the request's own
    // SecurityContext (set by JwtFilter).
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.inn.cafe.POJO.User user = userDao.findByEmailId(username);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("User not found...");
        }
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    /**
     * Called by Spring Security after a successful login when the stored hash uses an
     * outdated encoding - i.e. a legacy plaintext password. Re-saves it as BCrypt, so
     * existing accounts migrate transparently on their next sign-in.
     */
    @Override
    public UserDetails updatePassword(UserDetails details, String newEncodedPassword) {
        com.inn.cafe.POJO.User user = userDao.findByEmailId(details.getUsername());
        if (user != null) {
            user.setPassword(newEncodedPassword);
            userDao.save(user);
            log.info("Upgraded password hash for {}", user.getEmail());
        }
        return new User(details.getUsername(), newEncodedPassword, details.getAuthorities());
    }

    /** The signed-in user for the current request, or null for an anonymous request. */
    public com.inn.cafe.POJO.User getUserDetail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getDetails() instanceof com.inn.cafe.POJO.User user ? user : null;
    }
}
