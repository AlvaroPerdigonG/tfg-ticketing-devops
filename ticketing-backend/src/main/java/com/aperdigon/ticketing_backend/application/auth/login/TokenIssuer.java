package com.aperdigon.ticketing_backend.application.auth.login;

import com.aperdigon.ticketing_backend.domain.user.User;

public interface TokenIssuer {
    String issue(User user);
}
