package io.github.habatoo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

/**
 * Тестовый контроллер.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String homePage() {
        return "<h1>Test " + LocalDateTime.now() + " </h1>";
    }
}
