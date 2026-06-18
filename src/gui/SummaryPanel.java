package gui;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SummaryPanel extends JPanel {

    private final Client client;
    private final Runnable onBack;

    private Car car;
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalCost = BigDecimal.ZERO;

    private final JLabel carLabel = new JLabel();
    private final JLabel periodLabel = new JLabel();
    private final JLabel daysLabel = new JLabel();
    private final JLabel costLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();

    private final JRadioButton cardButton = new JRadioButton("Karta", true);
    private final JRadioButton cashButton = new JRadioButton("Gotówka");
    private final JRadioButton transferButton = new JRadioButton("Przelew");

    private final JRadioButton prepaidButton = new JRadioButton("Prepaid (przedpłata)", true);
    private final JRadioButton postpaidButton = new JRadioButton("Postpaid (po terminie)");

    public SummaryPanel(Client client, Runnable onBack) {
        super(new BorderLayout(8, 8));
        this.client = client;
        this.onBack = onBack;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JLabel("Podsumowanie wypożyczenia"), BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(0, 2, 6, 6));
        details.add(new JLabel("Samochód:"));
        details.add(carLabel);
        details.add(new JLabel("Termin:"));
        details.add(periodLabel);
        details.add(new JLabel("Liczba dni:"));
        details.add(daysLabel);
        details.add(new JLabel("Koszt całkowity:"));
        details.add(costLabel);
        details.add(new JLabel("Status:"));
        details.add(statusLabel);

        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cardButton);
        paymentGroup.add(cashButton);
        paymentGroup.add(transferButton);
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Metoda płatności"));
        paymentPanel.add(cardButton);
        paymentPanel.add(cashButton);
        paymentPanel.add(transferButton);

        ButtonGroup settlementGroup = new ButtonGroup();
        settlementGroup.add(prepaidButton);
        settlementGroup.add(postpaidButton);
        JPanel settlementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settlementPanel.setBorder(BorderFactory.createTitledBorder("Typ rozliczenia"));
        settlementPanel.add(prepaidButton);
        settlementPanel.add(postpaidButton);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(details);
        center.add(paymentPanel);
        center.add(settlementPanel);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton backButton = new JButton("Wstecz");
        JButton confirmButton = new JButton("Potwierdź i zapłać");
        bottom.add(backButton);
        bottom.add(confirmButton);
        add(bottom, BorderLayout.SOUTH);

        backButton.addActionListener(e -> onBack.run());
        confirmButton.addActionListener(e -> confirm());
    }

    public void configure(Car car, LocalDate from, LocalDate to) {
        this.car = car;
        this.from = from;
        this.to = to;
        long days = ChronoUnit.DAYS.between(from, to);
        this.totalCost = car.getCostPerDay().multiply(BigDecimal.valueOf(days));

        carLabel.setText(car.getBrand() + " | " + car.getModel());
        periodLabel.setText(from + "  →  " + to);
        daysLabel.setText(String.valueOf(days));
        costLabel.setText(totalCost + " zł");
        statusLabel.setText(enums.RentalStatus.RESERVED.toString());
    }

    private void confirm() {
        Rental rental;
        try {
            rental = new Rental(from, to, car, client);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Termin niedostępny", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            SettlementType settlement = buildSettlement();
            buildPayment(rental, settlement);
            ObjectPlus.saveExtent();
        } catch (Exception ex) {
            rental.delete();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Błąd płatności", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Wypożyczenie utworzone.\nStatus: " + rental.getStatus()
                        + "\nKoszt: " + totalCost + " zł",
                "Sukces", JOptionPane.INFORMATION_MESSAGE);
        onBack.run();
    }

    private SettlementType buildSettlement() {
        if (postpaidButton.isSelected()) {
            return new Postpaid(LocalDate.now().plusDays(14), BigDecimal.valueOf(0.01));
        }
        return new Prepaid(BigDecimal.valueOf(0.30), from);
    }

    private void buildPayment(Rental rental, SettlementType settlement) {
        if (cashButton.isSelected()) {
            new CashPayment(totalCost, rental, settlement, totalCost);
        } else if (transferButton.isSelected()) {
            new TransferPayment(totalCost, rental, settlement, "PL00000000000000000000000000", 2);
        } else {
            new CardPayment(totalCost, rental, settlement, "0000111122223333", "TX" + System.nanoTime());
        }
    }
}
