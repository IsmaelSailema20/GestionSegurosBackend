package com.app.seguros.AplicacionSeguros.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev/inicio")
@RequiredArgsConstructor
public class DemoController {
    @PostMapping(value = "/demo")
    public String inicio(){
        return "estas en inicio";
    }

}
