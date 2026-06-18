package gui;

import model.Branch;
import model.Car;
import model.ObjectPlus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SelectionPanel extends JPanel {

    public interface ProceedListener {
        void onProceed(Car car, LocalDate from, LocalDate to);
    }

    private final DefaultListModel<Branch> branchModel = new DefaultListModel<>();
    private final DefaultListModel<Car> carModel = new DefaultListModel<>();
    private final JList<Branch> branchList = new JList<>(branchModel);
    private final JList<Car> carList = new JList<>(carModel);
    private final JTextField dateFromField = new JTextField(10);
    private final JTextField dateToField = new JTextField(10);

    public SelectionPanel(ProceedListener listener) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        branchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        carList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        branchList.setCellRenderer(new BranchRenderer());
        carList.setCellRenderer(new CarRenderer());

        branchList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showCarsOfSelectedBranch();
            }
        });

        JPanel branchPanel = new JPanel(new BorderLayout(4, 4));
        branchPanel.add(new JLabel("Oddziały"), BorderLayout.NORTH);
        branchPanel.add(new JScrollPane(branchList), BorderLayout.CENTER);

        JPanel carPanel = new JPanel(new BorderLayout(4, 4));
        carPanel.add(new JLabel("Samochody w oddziale"), BorderLayout.NORTH);
        carPanel.add(new JScrollPane(carList), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, branchPanel, carPanel);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.add(new JLabel("Data od (RRRR-MM-DD):"));
        bottom.add(dateFromField);
        bottom.add(new JLabel("Data do (RRRR-MM-DD):"));
        bottom.add(dateToField);
        JButton nextButton = new JButton("Wybierz / Dalej");
        bottom.add(nextButton);
        add(bottom, BorderLayout.SOUTH);

        nextButton.addActionListener(e -> handleProceed(listener));

        refresh();
    }

    public void refresh() {
        Branch previous = branchList.getSelectedValue();
        branchModel.clear();
        for (Branch b : ObjectPlus.getExtentFromClass(Branch.class)) {
            branchModel.addElement(b);
        }
        if (previous != null && branchModel.contains(previous)) {
            branchList.setSelectedValue(previous, true);
        } else {
            carModel.clear();
        }
    }

    private void showCarsOfSelectedBranch() {
        carModel.clear();
        Branch selected = branchList.getSelectedValue();
        if (selected == null) {
            return;
        }
        List<Car> cars = new ArrayList<>(selected.getCars().values());
        cars.sort(Comparator.comparing(Car::getParkingSpot));
        for (Car c : cars) {
            carModel.addElement(c);
        }
    }

    private void handleProceed(ProceedListener listener) {
        Car car = carList.getSelectedValue();
        if (car == null) {
            JOptionPane.showMessageDialog(this, "Wybierz samochód.", "Brak wyboru", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(dateFromField.getText().trim());
            to = LocalDate.parse(dateToField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Podaj daty w formacie RRRR-MM-DD.", "Błędna data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!to.isAfter(from)) {
            JOptionPane.showMessageDialog(this, "Data zakończenia musi być po dacie rozpoczęcia.", "Błędny termin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        listener.onProceed(car, from, to);
    }

    private static class BranchRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focused) {
            super.getListCellRendererComponent(list, value, index, selected, focused);
            if (value instanceof Branch b) {
                setText(b.getName());
            }
            return this;
        }
    }

    private static class CarRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focused) {
            super.getListCellRendererComponent(list, value, index, selected, focused);
            if (value instanceof Car c) {
                setText(c.getBrand() + " | " + c.getModel()+ "  —  " + c.getCostPerDay() + " zł/dzień");
            }
            return this;
        }
    }
}
