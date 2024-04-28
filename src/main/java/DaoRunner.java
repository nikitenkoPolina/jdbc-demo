import dao.TicketDao;
import dto.TicketFilter;
import entity.Ticket;

import java.math.BigDecimal;

public class DaoRunner {

    public static void main(String[] args) {
        var ticket = TicketDao.getInstance().findById(4L);

        System.out.println(ticket);
    }

    private static void filterTest() {
        var filter = new TicketFilter(3, 2, null, "A1");
        var ticketDao = TicketDao.getInstance();
        System.out.println(ticketDao.findAll(filter));
    }

    private static void ticketTest() {
        var ticketDao = TicketDao.getInstance();
        var maybeTicket = ticketDao.findById(2L);
        System.out.println(maybeTicket);

        maybeTicket.ifPresent(ticket -> {
                    ticket.setSeatNo("B3");
                    ticketDao.update(ticket);
                }
        );
    }

    private static boolean deleteTest() {
        var ticketDao = TicketDao.getInstance();
        return ticketDao.delete(56L);
    }

    private static void saveTest() {
        var ticketDao = TicketDao.getInstance();

        var ticket = new Ticket();

        ticket.setPassengerNo("123456");
        ticket.setPassengerName("Polina Nikitenko");
        //ticket.setFlight(3L);
        ticket.setSeatNo("A17");
        ticket.setCost(BigDecimal.TEN);

        Ticket savedTicket = ticketDao.save(ticket);

        System.out.println(savedTicket);
    }
}
