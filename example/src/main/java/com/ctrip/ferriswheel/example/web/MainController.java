package com.ctrip.ferriswheel.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/test")
    public String test() {
        return "Hello world";
    }
}
