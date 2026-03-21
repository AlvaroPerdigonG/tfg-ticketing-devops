# Canonical functional specification
# This file is part of the canonical functional specification of the project.
# It is not executed with Cucumber.
# Its purpose is to provide stable functional scenarios and lightweight traceability
# between requirements, automated tests, and code.

Feature: Authentication and account access
  As a platform user
  I want to authenticate and register correctly
  So that I can access the ticketing platform according to my account state

  Background:
    Given the platform exposes authentication endpoints
    And users authenticate with email and password
    And successful authentication returns an access token

  Scenario: AUTH-01 Correct login
    Given an active user account exists with valid credentials
    When the user submits the correct email and password
    Then the authentication request is accepted
    And an access token is returned
    And the token identifies the authenticated user

  Scenario: AUTH-02 Invalid login
    Given an active user account exists
    When the user submits a valid email with an incorrect password
    Then the authentication request is rejected
    And no access token is returned

  Scenario: AUTH-03 Inactive user cannot enter
    Given an inactive user account exists with valid credentials
    When the user attempts to log in with the correct password
    Then the authentication request is rejected
    And no authenticated session is created

  Scenario: AUTH-04 Correct registration
    Given no existing account uses the submitted email address
    When a visitor submits a valid registration form
    Then a new user account is created
    And the new account is assigned the default user role
    And an access token is returned

  Scenario: AUTH-05 Registration with duplicated email fails
    Given an existing account already uses the submitted email address
    When a visitor submits a valid registration form with that same email
    Then the registration request is rejected
    And no duplicate account is created
