package com.aperdigon.ticketing_backend.api.auth;

import com.aperdigon.ticketing_backend.application.auth.login.LoginCommand;
import com.aperdigon.ticketing_backend.application.auth.login.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(new LoginResponse(result.accessToken()));
    }
}
