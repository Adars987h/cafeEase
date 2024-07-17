package com.inn.cafe.service;

import com.inn.cafe.wrapper.UserWrapper;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {

    String signUp(Map<String,String> requestMap);

    String login(Map<String,String> requestMap);

    List<UserWrapper> getAllUser();

    String update(Map<String,String> requestMap);

    String changePassword(Map<String, String> requestMap);

    String forgotPassword(Map<String, String> requestMap) throws Exception;



}
