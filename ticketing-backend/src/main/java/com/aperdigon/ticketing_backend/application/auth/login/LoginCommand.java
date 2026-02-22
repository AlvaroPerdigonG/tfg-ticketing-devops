package com.aperdigon.ticketing_backend.application.auth.login;

public record LoginCommand(String email, String password) {
}
