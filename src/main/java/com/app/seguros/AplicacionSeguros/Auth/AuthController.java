package com.app.seguros.AplicacionSeguros.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
     @Autowired
    private AuthService authService;

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try{
            return ResponseEntity.ok(authService.login(request));
        }catch (RuntimeException e){
            return ResponseEntity.internalServerError().body("Error: "+e.getMessage());
        }
    }

}
