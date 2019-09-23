package com.ctrip.ferriswheel.example.web;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {
    @GetMapping("/")
    public String index(HttpServletResponse response) {
        response.addHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
        return "/index.html";
    }
}
