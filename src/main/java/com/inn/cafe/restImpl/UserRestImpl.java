package com.inn.cafe.restImpl;

import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.UserRest;
import com.inn.cafe.service.UserService;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class UserRestImpl implements UserRest {

    @Autowired
    UserService userService;

    @Override
    public ResponseEntity<Response> signUp(Map<String, String> requestMap) {
        try {
            String result = userService.signUp(requestMap);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
//          ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> login(Map<String, String> requestMap) {
        try {
            String result = userService.login(requestMap);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getAllUser() {
        try {
            List<UserWrapper> result = userService.getAllUser();
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);

        } catch (Exception ex) {
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> update(Map<String, String> requestMap) {
        try {
            String result = userService.update(requestMap);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }


    @Override
    public ResponseEntity<Response> changePassword(Map<String, String> requestMap) {
        try {
            String result = userService.changePassword(requestMap);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> forgotPassword(Map<String, String> requestMap) throws Exception {
        try {
            String result = userService.forgotPassword(requestMap);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }
}
