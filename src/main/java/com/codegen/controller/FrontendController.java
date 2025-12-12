package com.codegen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    // Эти методы будут перенаправлять на HTML файлы
    @GetMapping("/")
    public String index() {
        return "redirect:/templates/index.html";
    }

    @GetMapping("/generator")
    public String generator() {
        return "redirect:/templates/generator.html";
    }

    @GetMapping("/templates")
    public String templates() {
        return "redirect:/templates/templates.html";
    }

    @GetMapping("/projects")
    public String projects() {
        return "redirect:/templates/projects.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "redirect:/templates/profile.html";
    }
}