package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.UnauthorizedException;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import com.inn.cafe.exceptions.AccountDisabledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MIN_PASSWORD_LENGTH = 8;


    @Override
    public String signUp(Map<String, String> requestMap) {
        log.info("Inside signup for {}", requestMap.get("email")); // never log the password
        try {
            if (validateSignUpMap(requestMap)) {
                requirePasswordStrength(requestMap.get("password"));
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return "Successfully Registered";
                } else {
                    throw new BadRequestException("Email already exists!!!!!");
                }
            } else {
                throw new BadRequestException(CafeConstants.INVALID_PAYLOAD);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }

    }


    @Override
    public String login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
            if (!auth.isAuthenticated()) {
                throw new BadCredentialsException("Wrong Email or Password !!!!!");
            }
            // Look the user up for this request rather than reading shared state.
            User user = userDao.findByEmailId(auth.getName());
            if (!"true".equalsIgnoreCase(user.getStatus())) {
                throw new AccountDisabledException(
                        "This account has been disabled. Contact the café to have it re-enabled.");
            }
            return jwtUtil.generateToken(user.getEmail(), user.getRole());
        } catch (Exception ex) {
            if(ex instanceof BadCredentialsException){
                throw new BadCredentialsException("Wrong Email or Password !!!!!");
            }
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<UserWrapper> getAllUser() {
        try {
            if (jwtFilter.isAdmin()) {
                return userDao.getAllUser();
            } else {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public String update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));

                    // @Async on a private method has no effect: Spring's AOP proxy can only
                    // intercept calls that come in through the bean, and a self-invocation
                    // like this bypasses the proxy entirely, so these ran synchronously on
                    // the request thread. That meant a mail failure (no SMTP configured, as
                    // in local dev) turned a status change that had already succeeded in the
                    // database into a 500 response. Catch and log instead, matching how
                    // OrderRestImpl already handles the same failure mode for order mail.
                    try {
                        sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                        sendMailToUser(requestMap.get("status"), optional.get().getEmail());
                    } catch (Exception mailEx) {
                        log.error("Status updated but notification mail failed: {}", mailEx.getMessage());
                    }

                    return "User Status Updated to " + requestMap.get("status") + " Successfully";
                } else {
                    throw new BadRequestException("User id doesn't exist !!!!!");
                }
            } else {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email") && requestMap.containsKey("password")) {
            return true;
        } else {
            return false;
        }
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(passwordEncoder.encode(requestMap.get("password")));
//        user.setStatus("false");
        user.setStatus("true");
        user.setRole("user");
        return user;

    }


    @Async
    private void sendMailToAllAdmin(String status, String user, List<String> allAdminEmail) {
        allAdminEmail.remove(jwtFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved", "USER:- " + user + "\n is approved by\nADMIN:- " + jwtFilter.getCurrentUser(), allAdminEmail);
        } else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", "USER:- " + user + "\n is disabled by\nADMIN:- " + jwtFilter.getCurrentUser(), allAdminEmail);

        }

    }

    @Async
    private void sendMailToUser(String status, String userEmail) {
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendSimpleMessage(userEmail, "Account Approved", "Your account " + userEmail + " has been Approved by Admin", null);
        } else {
            emailUtils.sendSimpleMessage(userEmail, "Account Disabled", "Your account " + userEmail + " has been Disabled by Admin", null);


        }

    }


    @Override
    public String changePassword(Map<String, String> requestMap) {
        try {
            User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
            if (userObj == null) {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
            if (!passwordEncoder.matches(requestMap.get("oldPassword"), userObj.getPassword())) {
                throw new BadRequestException("Incorrect Old Password");
            }
            requirePasswordStrength(requestMap.get("newPassword"));
            userObj.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
            userDao.save(userObj);
            return "Password Updated Successfully";

        } catch (Exception ex) {
//            ex.printStackTrace();
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public String forgotPassword(Map<String, String> requestMap) throws Exception {
        try {
            // Used to email the account's current password - only possible because passwords
            // were stored in plain text. Now issues a single-use link valid for 30 minutes.
            // The response is identical whether or not the email exists, so it can't be used
            // to discover registered addresses.
            User user = userDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
                byte[] raw = new byte[32];
                RANDOM.nextBytes(raw);
                String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
                user.setResetTokenHash(sha256(token));
                user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
                userDao.save(user);
                try {
                    emailUtils.sendPasswordReset(user.getEmail(), frontendUrl + "/reset-password?token=" + token);
                } catch (Exception mailEx) {
                    log.error("Password reset mail to {} failed: {}", user.getEmail(), mailEx.getMessage());
                }
            }
            return "If that email is registered, a reset link is on its way.";

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public String resetPassword(Map<String, String> requestMap) {
        String token = requestMap.get("token");
        if (Strings.isNullOrEmpty(token)) {
            throw new BadRequestException("This reset link is invalid or has expired.");
        }
        User user = userDao.findByResetTokenHash(sha256(token));
        if (user == null || user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This reset link is invalid or has expired.");
        }
        requirePasswordStrength(requestMap.get("password"));
        user.setPassword(passwordEncoder.encode(requestMap.get("password")));
        user.setResetTokenHash(null);
        user.setResetTokenExpiry(null);
        userDao.save(user);
        return "Password updated. You can sign in now.";
    }

    private static void requirePasswordStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            User user = userDao.findByEmail(email);
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
                return user;
            }
            throw new BadRequestException("No user found with the given Email");

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }


}
