// controller/LoginController.java
package com.weibo.controller;

import com.weibo.model.User;
import com.weibo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // 显示登录页
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // 如果已经登录，直接跳转到首页
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/";
        }
        return "login";
    }

    // 处理登录请求
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpSession session,
                          Model model) {
        User user = userService.authenticate(username, password);
        if (user != null) {
            session.setAttribute("loginUser", user);
            // 重定向到首页
            return "redirect:/";
        } else {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }

    // 登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}