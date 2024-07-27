package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.UnauthorizedException;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;


    @Override
    public String signUp(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
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
            if (auth.isAuthenticated()) {
                if (customerUserDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                    return "{token : " +
                            jwtUtil.generateToken(customerUserDetailsService.getUserDetail().getEmail(),
                                    customerUserDetailsService.getUserDetail().getRole()) + " }";
                } else {
                    throw new BadRequestException("Wait for Admin Approval !!!!!!!");
                }
            } else {
                throw new BadCredentialsException("Wrong Email or Password !!!!!");
            }
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
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());

                    sendMailToUser(requestMap.get("status"), optional.get().getEmail());

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
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
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
            if (!userObj.equals(null)) {
                if (userObj.getPassword().equals(requestMap.get("oldPassword"))) {
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return "Password Updated Successfully";
                }
                throw new BadRequestException("Incorrect Old Password");
            }
            throw new RuntimeException("Something went wrong !!!!");

        } catch (Exception ex) {
//            ex.printStackTrace();
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public String forgotPassword(Map<String, String> requestMap) throws Exception {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
                emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
            }
            return "Check your mail for Credentials";

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
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
