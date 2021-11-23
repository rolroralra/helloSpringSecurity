package com.example.security.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/login")
    public String login(@RequestParam(defaultValue = "false") Boolean error, Model model) {
        if (error) {
            model.addAttribute("errorMessage", "Password is incorrect.");
        }

        return "login";
    }

}
