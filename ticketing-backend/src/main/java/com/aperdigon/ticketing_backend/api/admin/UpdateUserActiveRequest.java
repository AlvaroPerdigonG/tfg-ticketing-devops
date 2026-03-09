package com.aperdigon.ticketing_backend.api.admin;

import jakarta.validation.constraints.NotNull;

public record UpdateUserActiveRequest(
        @NotNull Boolean isActive
) {}
