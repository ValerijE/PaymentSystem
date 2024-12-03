package com.evv.http.controller;

import com.evv.dto.ClientCreateEditDto;
import com.evv.dto.ClientReadDto;
import com.evv.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/clients")
    public String findAllClients(Model model) {
        List<ClientReadDto> clients = userService.findAllClients();
        model.addAttribute("clients", clients);
        return "user/clients";
    }

    @PostMapping("/clients")
    public String createClient(@ModelAttribute @Validated ClientCreateEditDto client,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("client", client);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/users/registration";
        }
        userService.create(client);
        return "redirect:/users/login";
    }

    @GetMapping("/registration")
    public String registration(@ModelAttribute("client") ClientCreateEditDto client) {
        return "user/registration";
    }
}
