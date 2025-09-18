// SortDialog.java
package org.example.client.gui.dialogs;

import org.example.client.gui.resources.Localization;
import org.example.data.Product;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

public class SortDialog extends JDialog {
    private boolean confirmed = false;
    private final Localization localization;
    private JComboBox<String> fieldComboBox;
    private JRadioButton ascendingButton;
    private JRadioButton descendingButton;

    public SortDialog(JFrame owner) {
        super(owner, "Sort Products", true);
        this.localization = Localization.getInstance();
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }


    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        // ЗАМЕНА НА ЛОКАЛИЗИРОВАННЫЕ ТЕКСТЫ
        formPanel.add(new JLabel(localization.getString("sort.field")));
        fieldComboBox = new JComboBox<>(new String[]{
                localization.getString("sort.field.id"),
                localization.getString("sort.field.name"),
                localization.getString("sort.field.price"),
                localization.getString("sort.field.creationDate"),
                localization.getString("sort.field.manufactureCost")
        });
        formPanel.add(fieldComboBox);

        formPanel.add(new JLabel(localization.getString("sort.order")));
        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ascendingButton = new JRadioButton(localization.getString("sort.ascending"), true);
        descendingButton = new JRadioButton(localization.getString("sort.descending"));
        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(ascendingButton);
        orderGroup.add(descendingButton);
        orderPanel.add(ascendingButton);
        orderPanel.add(descendingButton);
        formPanel.add(orderPanel);

        JButton okButton = new JButton(localization.getString("sort.apply"));
        JButton cancelButton = new JButton(localization.getString("button.cancel"));

        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Устанавливаем заголовок окна
        setTitle(localization.getString("dialog.sort.title"));
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Comparator<Product> getComparator() {
        String field = (String) fieldComboBox.getSelectedItem();
        boolean ascending = ascendingButton.isSelected();

        Comparator<Product> comparator = null;

        if (field.equals(localization.getString("sort.field.id"))) {
            comparator = Comparator.comparing(Product::getId);
        } else if (field.equals(localization.getString("sort.field.name"))) {
            comparator = Comparator.comparing(Product::getName);
        } else if (field.equals(localization.getString("sort.field.price"))) {
            comparator = Comparator.comparing(Product::getPrice,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
        } else if (field.equals(localization.getString("sort.field.creationDate"))) {
            comparator = Comparator.comparing(Product::getCreationDate);
        } else if (field.equals(localization.getString("sort.field.manufactureCost"))) {
            comparator = Comparator.comparing(Product::getManufactureCost,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
        } else {
            throw new IllegalArgumentException("Unknown field: " + field);
        }

        return ascending ? comparator : comparator.reversed();
    }
}
