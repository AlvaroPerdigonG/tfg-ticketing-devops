# Canonical functional specification
# This file is part of the canonical functional specification of the project.
# It is not executed with Cucumber.
# Its purpose is to provide stable functional scenarios and lightweight traceability
# between requirements, automated tests, and code.

Feature: User ticket management
  As an authenticated end user
  I want to create and consult my own tickets
  So that I can report incidents and follow their progress

  Background:
    Given the platform exposes ticket management endpoints
    And authenticated users can create and consult their own tickets

  Scenario: TICKET-USER-01 User creates ticket correctly
    Given an authenticated user is allowed to create tickets
    And at least one valid active category exists
    When the user submits a valid ticket creation request
    Then the ticket is created successfully
    And the ticket is stored with open status
    And the ticket is associated with the requesting user

  Scenario: TICKET-USER-02 Creating a ticket without token returns 401
    Given the platform requires authentication to create tickets
    When an unauthenticated client submits a ticket creation request
    Then the request is rejected with unauthorized response
    And no ticket is created

  Scenario: TICKET-USER-03 User sees only their own tickets
    Given multiple tickets exist in the system for different users
    When an authenticated user requests their ticket list
    Then only tickets created by that same user are returned
    And tickets from other users are not visible in the response

  Scenario: TICKET-USER-04 User sees detail of their own ticket
    Given an authenticated user has created a ticket
    When that same user requests the ticket detail
    Then the request is accepted
    And the response contains the full detail of that ticket

  Scenario: TICKET-USER-05 User cannot change ticket status
    Given an authenticated user owns an existing ticket
    When the user attempts to change the ticket status directly
    Then the request is rejected as forbidden
    And the ticket status remains unchanged
