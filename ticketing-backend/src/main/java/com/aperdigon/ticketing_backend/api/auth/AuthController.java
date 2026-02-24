package com.aperdigon.ticketing_backend.api.auth;

import com.aperdigon.ticketing_backend.application.auth.login.LoginCommand;
import com.aperdigon.ticketing_backend.application.auth.login.LoginUseCase;
import com.aperdigon.ticketing_backend.application.auth.register.RegisterCommand;
import com.aperdigon.ticketing_backend.application.auth.register.RegisterUseCase;
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
    private final RegisterUseCase registerUseCase;

    public AuthController(LoginUseCase loginUseCase, RegisterUseCase registerUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(new LoginResponse(result.accessToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUseCase.execute(new RegisterCommand(
                request.email(),
                request.displayName(),
                request.password(),
                request.confirmPassword()
        ));

        return ResponseEntity.ok(new LoginResponse(result.accessToken()));
    }
}
