# Canonical functional specification
# This file is part of the canonical functional specification of the project.
# It is not executed with Cucumber.
# Its purpose is to provide stable functional scenarios and lightweight traceability
# between requirements, automated tests, and code.

Feature: Agent and administrator operational ticket management
  As an agent or administrator
  I want to manage operational ticket queues
  So that I can assign, review, and progress incidents correctly

  Background:
    Given operational roles can access management ticket queues
    And only agent or administrator roles may perform operational ticket actions

  Scenario: TICKET-AGENT-01 Agent/admin changes status correctly
    Given an existing ticket is in a state with at least one valid transition
    And the requester has role agent or administrator
    When the requester submits a valid status change
    Then the request is accepted
    And the ticket status is updated accordingly

  Scenario: TICKET-AGENT-02 Invalid transition returns error
    Given an existing ticket is in a state that does not allow the requested next status
    And the requester has role agent or administrator
    When the requester submits that invalid status transition
    Then the request is rejected with an error
    And the ticket state remains unchanged

  Scenario: TICKET-AGENT-03 Agent sees manageable tickets
    Given the system contains tickets in operational queues
    And the requester has role agent or administrator
    When the requester lists manageable tickets
    Then the response contains tickets available for operational management
    And the requester can filter or scope that operational view

  Scenario: TICKET-AGENT-04 Assign to me works correctly
    Given an existing ticket is assignable
    And the requester has role agent or administrator
    When the requester asks to assign the ticket to themselves
    Then the request is accepted
    And the ticket becomes assigned to that requester
