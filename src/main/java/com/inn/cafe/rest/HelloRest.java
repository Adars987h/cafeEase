package com.inn.cafe.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloRest {

    @GetMapping
    String sayHello(){
        return "Hello from CafeEase";
    }
}
