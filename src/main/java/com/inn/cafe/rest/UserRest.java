package com.inn.cafe.rest;

import com.inn.cafe.dto.Response;
import com.inn.cafe.wrapper.UserWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/user")
public interface UserRest {
    @PostMapping(path = "/signup")
    ResponseEntity<Response> signUp(@RequestBody(required = true) Map<String, String> requestMap);

    @PostMapping(path = "/login")
    ResponseEntity<Response> login(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<Response> getAllUser();

    @PostMapping(path = "/update")
    ResponseEntity<Response> update(@RequestBody(required = true) Map<String, String> requestMap);

    @PostMapping(path = "/changePassword")
    ResponseEntity<Response> changePassword(@RequestBody Map<String, String> requestMap);

    @PostMapping(path = "/forgotPassword")
    ResponseEntity<Response> forgotPassword(@RequestBody Map<String, String> requestMap) throws Exception;

}
