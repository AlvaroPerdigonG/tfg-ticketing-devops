package com.aperdigon.ticketing_backend.api.shared.currentuser;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;

public interface CurrentUserProvider {
    CurrentUser getCurrentUser();
}
