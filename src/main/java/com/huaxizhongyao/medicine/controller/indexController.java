package com.huaxizhongyao.medicine.controller;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class indexController {

    @GetMapping("/")
    public String redirectToIndex() {
        return "index";
    }

    @GetMapping("/index")
    public String index(HttpSession session) {
        // 验证session是否存在
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "index";
    }
}
