package com.huaxizhongyao.medicine.controller;

import com.huaxizhongyao.medicine.service.loginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@Controller
public class loginController {
    @Autowired
    private loginService loginservice;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        if(loginservice.loginValidate(username, password)) {
            session.setAttribute("user", username);
            session.setMaxInactiveInterval((int) TimeUnit.HOURS.toSeconds(24));
            return "redirect:/index";
        }
        return "redirect:/login?error";
    }


    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
