import enums.DrivingLicenseCategory;
import enums.InsuranceType;
import enums.TicketPriority;
import gui.RentACarApp;
import model.*;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        consoleDemo();
        clearAllData();
        System.out.println("\n=== Czyszczę dane i uruchamiam GUI ===");
        RentACarApp.main(args);
    }

    private static void consoleDemo() {
        LocalDate today = LocalDate.now();

        // ---- dane przykładowe ----
        Branch branch = new Branch("Oddział Centrum");

        Car c1 = new Car("Toyota", "Corolla", "WA1001", 2021, branch, 1, BigDecimal.valueOf(150));
        Car c2 = new Car("Volkswagen", "Golf", "WA1002", 2020, branch, 2, BigDecimal.valueOf(170));
        Car c3 = new Car("Skoda", "Octavia", "WA1003", 2022, branch, 3, BigDecimal.valueOf(180));
        Car c4 = new Car("Ford", "Focus", "WA1004", 2019, branch, 4, BigDecimal.valueOf(140));
        Car c5 = new Car("BMW", "Seria 3", "WA1005", 2023, branch, 5, BigDecimal.valueOf(300));
        Car c6 = new Car("Audi", "A4", "WA1006", 2022, branch, 6, BigDecimal.valueOf(280));
        Car c7 = new Car("Kia", "Ceed", "WA1007", 2021, branch, 7, BigDecimal.valueOf(160));
        Car c8 = new Car("Mazda", "3", "WA1008", 2020, branch, 8, BigDecimal.valueOf(165));

        Address addr = new Address("Testowa 1", "Warszawa", "00-001");
        Client client = new Client("Jan", "Kowalski", LocalDate.of(1990, 1, 1), addr,
                List.of(DrivingLicenseCategory.B));

        Address empAddr = new Address("Pracownicza 5", "Warszawa", "00-002");
        Employee employee = new Employee("Anna", "Nowak", LocalDate.of(1985, 3, 10),
                LocalDate.of(2015, 6, 1), empAddr, branch, 9000);

        // =====================================================================
        System.out.println("1. ASOCJACJA KWALIFIKOWANA: Branch -> Car (przez parkingSpot)");
        for (var entry : branch.getCars().entrySet()) {
            System.out.println("  miejsce " + entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println("  getCarAtSpot(3) = " + branch.getCarAtSpot(3));

        // =====================================================================
        System.out.println("2. NAWIGACJA PO ASOCJACJI (4.2.4): samochody oddziału");
        System.out.println("  branch.getCars().values():");
        branch.getCars().values().forEach(car -> System.out.println("    " + car));

        // =====================================================================
        System.out.println("3. DZIEDZICZENIE DYNAMICZNE: ServiceTicket NEW -> IN_PROGRESS -> CLOSED");
        ServiceTicket ticket = new ServiceTicket("Stuknięty zderzak", client, c8, TicketPriority.HIGH);
        System.out.println("  stan: " + ticket.getState() + ", priorytet: " + ticket.getPriority());
        ticket.takeTicket(employee);
        System.out.println("  po takeTicket -> stan: " + ticket.getState()
                + ", obsługuje: " + ticket.getAssignedEmployee().getName());
        ticket.closeTicket("Wymieniono zderzak");
        System.out.println("  po closeTicket -> stan: " + ticket.getState()
                + ", rozwiązanie: " + ticket.getSolutionDescription()
                + ", czas obsługi (dni): " + ticket.getResolutionDays());
        try {
            ticket.getPriority(); // priorytet dostępny tylko w stanie NEW
        } catch (IllegalStateException e) {
            System.out.println("  strażnik requireState dla getPriority() w CLOSED: " + e.getMessage());
        }

        // =====================================================================
        System.out.println("4. DZIEDZICZENIE OVERLAPPING: InsurancePolicy (OC + AC + ASSISTANCE)");
        InsurancePolicy policy = new InsurancePolicy(
                EnumSet.of(InsuranceType.OC, InsuranceType.AC, InsuranceType.ASSISTANCE),
                BigDecimal.valueOf(1_000_000), // OC
                BigDecimal.valueOf(80_000),    // AC
                100,                           // ASSISTANCE
                null,                          // NNW – brak
                null);                         // GAP – brak
        c1.setPolicy(policy);
        System.out.println("  pokrycia: " + policy.getCoverages());
        System.out.println("  składka łączna: " + policy.calculateTotalPremium());
        System.out.println("  auto polisy: " + policy.getCar());
        try {
            policy.getGapPercentage(); // GAP nie należy do polisy
        } catch (IllegalStateException e) {
            System.out.println("  strażnik requireRole dla getGapPercentage(): " + e.getMessage());
        }

        // =====================================================================
        System.out.println("5. KLASA ASOCJACYJNA + STANY: Rental RESERVED -> ACTIVE -> CLOSED");
        Rental rental = new Rental(today, today.plusDays(5), c4, client);
        System.out.println("  utworzono, stan: " + rental.getStatus());
        rental.pickUp();
        System.out.println("  po pickUp(): " + rental.getStatus());
        rental.returnCar();
        System.out.println("  po returnCar(): " + rental.getStatus());

        Rental toCancel = new Rental(today.plusDays(10), today.plusDays(12), c5, client);
        toCancel.cancel();
        System.out.println("  inne wypożyczenie po cancel(): " + toCancel.getStatus());

        // =====================================================================
        System.out.println("6. WALIDACJA: odrzucenie nakładającej się rezerwacji tego samego auta");
        new Rental(today, today.plusDays(3), c6, client);
        try {
            new Rental(today.plusDays(1), today.plusDays(4), c6, client); // nachodzi na powyższe
        } catch (IllegalArgumentException e) {
            System.out.println("  poprawnie odrzucono: " + e.getMessage());
        }

        // =====================================================================
        System.out.println("7. DZIEDZICZENIE WIELOASPEKTOWE: Payment (metoda) + SettlementType (rozliczenie)");
        Rental payRental = new Rental(today.plusDays(20), today.plusDays(25), c7, client);
        BigDecimal cost = c7.getCostPerDay().multiply(BigDecimal.valueOf(5));

        Prepaid prepaid = new Prepaid(BigDecimal.valueOf(0.30), today.plusDays(2));
        CardPayment card = new CardPayment(cost, payRental, prepaid, "4111111111111111", "TX-1001");
        System.out.println("  CardPayment: prowizja=" + card.getSettlementFee()
                + ", razem=" + card.getTotalCost()
                + ", do zapłaty z góry (30%)=" + card.getUpfrontRequired()
                + ", txId=" + card.getTransactionId());

        Postpaid postpaid = new Postpaid(today.plusDays(30), BigDecimal.valueOf(0.01));
        CashPayment cash = new CashPayment(cost, payRental, postpaid, cost.add(BigDecimal.valueOf(50)));
        System.out.println("  CashPayment: prowizja=" + cash.getSettlementFee()
                + ", reszta=" + cash.getChange()
                + ", do zapłaty z góry (postpaid)=" + cash.getUpfrontRequired());

        TransferPayment transfer = new TransferPayment(cost, payRental, postpaid,
                "PL61109010140000071219812874", 2);
        System.out.println("  TransferPayment: prowizja=" + transfer.getSettlementFee()
                + ", iban=" + transfer.getIban() + ", dni rozliczenia=" + transfer.getClearingDays());

        System.out.println("  liczba płatności wypożyczenia (1 - 0..*): " + payRental.getPayments().size());

        // =====================================================================
        System.out.println("8. ABSTRAKCJA + POLIMORFIZM: getDiscountRate() (Client vs Employee)");
        // trzy zakończone (data końca w przeszłości) wypożyczenia klienta -> zniżka rośnie
        new Rental(today.minusMonths(6), today.minusMonths(5), c1, client);
        new Rental(today.minusMonths(4), today.minusMonths(3), c2, client);
        new Rental(today.minusMonths(2), today.minusMonths(1), c3, client);
        System.out.println("  zniżka klienta (3 zakończone): " + client.getDiscountRate());
        System.out.println("  zniżka pracownika (staż): " + employee.getDiscountRate());

        BigDecimal base = c4.getCostPerDay().multiply(BigDecimal.valueOf(5));
        BigDecimal afterDiscount = base.subtract(base.multiply(client.getDiscountRate()));
        System.out.println("  przykładowy koszt 5 dni: " + base
                + " -> po zniżce klienta: " + afterDiscount);

        // =====================================================================
        System.out.println("9. EKSTENSJE klas");
        System.out.println("Branch: " + ObjectPlus.getExtentFromClass(Branch.class).size());
        System.out.println("Car: " + ObjectPlus.getExtentFromClass(Car.class).size());
        System.out.println("Client: " + ObjectPlus.getExtentFromClass(Client.class).size());
        System.out.println("Employee:" + ObjectPlus.getExtentFromClass(Employee.class).size());
        System.out.println("Rental: " + ObjectPlus.getExtentFromClass(Rental.class).size());
        System.out.println("ServiceTicket: " + ObjectPlus.getExtentFromClass(ServiceTicket.class).size());
        System.out.println("Payment(Card): " + ObjectPlus.getExtentFromClass(CardPayment.class).size());
        System.out.println("InsurancePolicy: " + ObjectPlus.getExtentFromClass(InsurancePolicy.class).size());

        System.out.println("\n=== Demonstracja zakończona pomyślnie ===");
    }

    private static void clearAllData() {
        ObjectPlus.clearExtent();
        File extentFile = new File("extent");
        if (extentFile.exists()) {
            extentFile.delete();
        }
    }
}
