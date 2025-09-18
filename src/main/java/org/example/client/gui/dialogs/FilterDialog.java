package org.example.client.gui.dialogs;

import org.example.client.gui.models.ProductTableModel;
import org.example.client.gui.resources.Localization;
import org.example.data.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.function.Predicate;

public class FilterDialog extends JDialog implements Localization.LocaleChangeListener {
    private final ProductTableModel tableModel;
    private final Localization localization;

    private JComboBox<String> fieldComboBox;
    private JComboBox<String> operatorComboBox;
    private JTextField valueField;
    private JButton applyButton;
    private JButton clearButton;

    public FilterDialog(JFrame owner, ProductTableModel tableModel) {
        super(owner, "Filter Products", true);
        this.localization = Localization.getInstance();
        this.tableModel = tableModel;

        localization.addLocaleChangeListener(this);

        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        formPanel.add(new JLabel(localization.getString("filter.field")));
        fieldComboBox = new JComboBox<>(new String[]{
                localization.getString("filter.field.name"),
                localization.getString("filter.field.price"),
                localization.getString("filter.field.manufactureCost"),
                localization.getString("filter.field.partNumber"),
                localization.getString("filter.field.creatorId")
        });
        formPanel.add(fieldComboBox);

        formPanel.add(new JLabel(localization.getString("filter.operator")));
        operatorComboBox = new JComboBox<>(new String[]{
                localization.getString("filter.operator.equals"),
                localization.getString("filter.operator.contains"),
                localization.getString("filter.operator.greater"),
                localization.getString("filter.operator.less")
        });
        formPanel.add(operatorComboBox);

        formPanel.add(new JLabel(localization.getString("filter.value")));
        valueField = new JTextField(15);
        formPanel.add(valueField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        applyButton = new JButton(localization.getString("filter.apply"));
        clearButton = new JButton(localization.getString("filter.clear"));

        applyButton.addActionListener(this::applyFilter);
        clearButton.addActionListener(this::clearFilter);

        buttonPanel.add(applyButton);
        buttonPanel.add(clearButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void applyFilter(ActionEvent e) {
        try {
            String field = (String) fieldComboBox.getSelectedItem();
            String operator = (String) operatorComboBox.getSelectedItem();
            String value = valueField.getText().trim();

            Predicate<Product> filter = createFilter(field, operator, value);
            tableModel.applyFilter(filter);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    localization.getString("filter.error") + ex.getMessage(),
                    localization.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Predicate<Product> createFilter(String field, String operator, String value) {
        if (field.equals(localization.getString("filter.field.name"))) {
            return createStringFilter(Product::getName, operator, value);
        } else if (field.equals(localization.getString("filter.field.price"))) {
            return createNumericFilter(Product::getPrice, operator, value);
        } else if (field.equals(localization.getString("filter.field.manufactureCost"))) {
            return createNumericFilter(Product::getManufactureCost, operator, value);
        } else if (field.equals(localization.getString("filter.field.partNumber"))) {
            return createStringFilter(Product::getPartNumber, operator, value);
        } else if (field.equals(localization.getString("filter.field.creatorId"))) {
            return createNumericFilter(p -> (long) p.getCreatorId(), operator, value);
        } else {
            return product -> true;
        }
    }

    private Predicate<Product> createStringFilter(
            java.util.function.Function<Product, String> extractor, String operator, String value) {

        if (operator.equals(localization.getString("filter.operator.equals"))) {
            return product -> extractor.apply(product).equalsIgnoreCase(value);
        } else if (operator.equals(localization.getString("filter.operator.contains"))) {
            return product -> extractor.apply(product).toLowerCase().contains(value.toLowerCase());
        } else {
            return product -> true;
        }
    }

    private Predicate<Product> createNumericFilter(
            java.util.function.Function<Product, Number> extractor, String operator, String value) {

        double numericValue = Double.parseDouble(value);

        if (operator.equals(localization.getString("filter.operator.equals"))) {
            return product -> {
                Number num = extractor.apply(product);
                return num != null && Math.abs(num.doubleValue() - numericValue) < 0.001;
            };
        } else if (operator.equals(localization.getString("filter.operator.greater"))) {
            return product -> {
                Number num = extractor.apply(product);
                return num != null && num.doubleValue() > numericValue;
            };
        } else if (operator.equals(localization.getString("filter.operator.less"))) {
            return product -> {
                Number num = extractor.apply(product);
                return num != null && num.doubleValue() < numericValue;
            };
        } else {
            return product -> true;
        }
    }

    private void clearFilter(ActionEvent e) {
        tableModel.clearFilter();
        dispose();
    }

    @Override
    public void onLocaleChanged(Locale newLocale) {
        updateLocalizedText();
    }

    private void updateLocalizedText() {
        setTitle(localization.getString("filter.title"));

        // Update field combo box
        String[] fieldKeys = {
                "filter.field.name",
                "filter.field.price",
                "filter.field.manufactureCost",
                "filter.field.partNumber",
                "filter.field.creatorId"
        };

        String selectedField = (String) fieldComboBox.getSelectedItem();
        fieldComboBox.removeAllItems();
        for (String key : fieldKeys) {
            fieldComboBox.addItem(localization.getString(key));
        }
        fieldComboBox.setSelectedItem(selectedField);

        // Update operator combo box
        String[] operatorKeys = {
                "filter.operator.equals",
                "filter.operator.contains",
                "filter.operator.greater",
                "filter.operator.less"
        };

        String selectedOperator = (String) operatorComboBox.getSelectedItem();
        operatorComboBox.removeAllItems();
        for (String key : operatorKeys) {
            operatorComboBox.addItem(localization.getString(key));
        }
        operatorComboBox.setSelectedItem(selectedOperator);

        // Update buttons and labels
        applyButton.setText(localization.getString("filter.apply"));
        clearButton.setText(localization.getString("filter.clear"));

        // Repack the dialog to fit new text sizes
        pack();
    }
}