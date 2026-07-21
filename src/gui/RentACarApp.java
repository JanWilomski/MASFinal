package gui;

import enums.DrivingLicenseCategory;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RentACarApp {

    private static final String SELECTION = "selection";
    private static final String SUMMARY = "summary";

    private final CardLayout cards = new CardLayout();
    private final JPanel container = new JPanel(cards);
    private SelectionPanel selectionPanel;
    private SummaryPanel summaryPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RentACarApp().start());
    }

    public static void persist() {
        try {
            ObjectPlus.saveExtent();
        } catch (IOException e) {
            System.err.println("Błąd zapisu ekstensji: " + e.getMessage());
        }
    }

    private void start() {
        loadOrSeed();
        Client client = sampleClient();

        JFrame frame = new JFrame("Wypożycz samochód");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(820, 540);
        frame.setLocationRelativeTo(null);

        selectionPanel = new SelectionPanel(this::showSummary);
        summaryPanel = new SummaryPanel(client, this::showSelection);

        container.add(selectionPanel, SELECTION);
        container.add(summaryPanel, SUMMARY);

        frame.setContentPane(container);
        frame.setVisible(true);
        cards.show(container, SELECTION);
    }

    private void showSummary(Car car, LocalDate from, LocalDate to) {
        summaryPanel.configure(car, from, to);
        cards.show(container, SUMMARY);
    }

    private void showSelection() {
        selectionPanel.refresh();
        cards.show(container, SELECTION);
    }

    private void loadOrSeed() {
        File file = new File("extent");
        if (file.exists()) {
            try {
                ObjectPlus.loadExtent();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Nie udało się wczytać danych, startuję z danymi przykładowymi.",
                        "Wczytywanie", JOptionPane.WARNING_MESSAGE);
            }
        }
        if (ObjectPlus.getExtentFromClass(Branch.class).isEmpty()) {
            seed();
            persist();
        }
    }

    private void seed() {
        Branch centrum = new Branch("Oddział Centrum");
        Branch lotnisko = new Branch("Oddział Lotnisko");

        new Car("Toyota", "Corolla", "WA1001", 2021, centrum, 1, BigDecimal.valueOf(150));
        new Car("Volkswagen", "Golf", "WA1002", 2020, centrum, 2, BigDecimal.valueOf(170));
        new Car("Skoda", "Octavia", "WA1003", 2022, centrum, 3, BigDecimal.valueOf(180));
        new Car("Ford", "Focus", "KR2001", 2019, lotnisko, 1, BigDecimal.valueOf(140));
        new Car("BMW", "Seria 3", "KR2002", 2023, lotnisko, 2, BigDecimal.valueOf(300));

        Address address = new Address("Testowa 1", "Warszawa", "00-001");
        new Client("Jan", "Kowalski", LocalDate.of(1990, 1, 1), address, List.of(DrivingLicenseCategory.B));
    }

    private Client sampleClient() {
        List<Client> clients = ObjectPlus.getExtentFromClass(Client.class);
        if (!clients.isEmpty()) {
            return clients.get(0);
        }
        Address address = new Address("Testowa 1", "Warszawa", "00-001");
        return new Client("Jan", "Kowalski", LocalDate.of(1990, 1, 1), address, List.of(DrivingLicenseCategory.B));
    }
}
