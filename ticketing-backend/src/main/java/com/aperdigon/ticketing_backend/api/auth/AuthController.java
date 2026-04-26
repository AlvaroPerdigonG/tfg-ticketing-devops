package com.aperdigon.ticketing_backend.api.auth;

import com.aperdigon.ticketing_backend.api.shared.currentuser.CurrentUserProvider;
import com.aperdigon.ticketing_backend.application.auth.login.LoginCommand;
import com.aperdigon.ticketing_backend.application.auth.login.LoginUseCase;
import com.aperdigon.ticketing_backend.application.auth.profile.GetMyProfileUseCase;
import com.aperdigon.ticketing_backend.application.auth.register.RegisterCommand;
import com.aperdigon.ticketing_backend.application.auth.register.RegisterUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication and profile endpoints")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(
            LoginUseCase loginUseCase,
            RegisterUseCase registerUseCase,
            GetMyProfileUseCase getMyProfileUseCase,
            CurrentUserProvider currentUserProvider
    ) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.getMyProfileUseCase = getMyProfileUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(new LoginResponse(result.accessToken()));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUseCase.execute(new RegisterCommand(
                request.email(),
                request.displayName(),
                request.password(),
                request.confirmPassword()
        ));

        return ResponseEntity.ok(new LoginResponse(result.accessToken()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get profile of the authenticated user")
    public ResponseEntity<AuthProfileResponse> me() {
        var result = getMyProfileUseCase.execute(currentUserProvider.getCurrentUser());
        return ResponseEntity.ok(new AuthProfileResponse(
                result.userId().toString(),
                result.email(),
                result.displayName(),
                result.role().name(),
                result.roles()
        ));
    }
}
