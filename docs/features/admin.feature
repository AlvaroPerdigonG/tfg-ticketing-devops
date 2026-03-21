# Canonical functional specification
# This file is part of the canonical functional specification of the project.
# It is not executed with Cucumber.
# Its purpose is to provide stable functional scenarios and lightweight traceability
# between requirements, automated tests, and code.

Feature: Administrative management
  As a platform administrator
  I want to manage administrative resources
  So that I can supervise users and categories securely

  Background:
    Given administrative endpoints exist for users and categories
    And only administrators may access those endpoints

  Scenario: ADMIN-01 Admin lists users
    Given the requester has administrator role
    When the requester asks for the user list
    Then the request is accepted
    And the response contains platform users with their relevant administrative data

  Scenario: ADMIN-02 Admin lists categories
    Given the requester has administrator role
    When the requester asks for the category list
    Then the request is accepted
    And the response contains available categories with their administrative state

  Scenario: ADMIN-03 Admin deactivates user
    Given the requester has administrator role
    And an active non-admin user exists
    When the administrator deactivates that user
    Then the user is updated as inactive
    And that user can no longer authenticate successfully

  Scenario: ADMIN-04 Non-admin cannot access admin endpoints
    Given the requester is authenticated without administrator role
    When the requester attempts to access an administrative endpoint
    Then the request is rejected as forbidden
    And no administrative action is performed
