package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.delete.DeleteTicketCommand;
import com.aperdigon.ticketing_backend.application.tickets.delete.DeleteTicketUseCase;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class DeleteTicketUseCaseTest {

 @Test
 void admin_can_delete_existing_ticket() {
  var repo = new InMemoryTicketRepository();
  var ticket = TicketTestDataBuilder.aTicket().build();
  repo.save(ticket);
  var useCase = new DeleteTicketUseCase(repo);

  useCase.execute(new DeleteTicketCommand(ticket.id(), new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN)));

  assertTrue(repo.findById(ticket.id()).isEmpty());
 }

 @Test
 void non_admin_cannot_delete() {
  var repo = new InMemoryTicketRepository();
  var useCase = new DeleteTicketUseCase(repo);

  assertThrows(ForbiddenException.class, () -> useCase.execute(new DeleteTicketCommand(TicketId.of(UUID.randomUUID()), new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT))));
 }

 @Test
 void deleting_non_existing_ticket_throws_not_found() {
  var repo = new InMemoryTicketRepository();
  var useCase = new DeleteTicketUseCase(repo);

  assertThrows(NotFoundException.class, () -> useCase.execute(new DeleteTicketCommand(TicketId.of(UUID.randomUUID()), new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN))));
 }
}
