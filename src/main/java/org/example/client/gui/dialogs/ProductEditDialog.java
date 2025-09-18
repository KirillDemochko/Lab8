// ProductEditDialog.java
package org.example.client.gui.dialogs;

import org.example.client.gui.resources.Localization;
import org.example.client.network.GuiClient;
import org.example.client.state.SessionState;
import org.example.data.Product;
import org.example.data.Coordinates;
import org.example.data.Organization;
import org.example.data.UnitOfMeasure;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductEditDialog extends JDialog {
    private final Localization localization;
    private final GuiClient client;
    private final SessionState sessionState;
    private final Product existingProduct;

    private JTextField nameField;
    private JTextField xField;
    private JTextField yField;
    private JTextField priceField;
    private JTextField partNumberField;
    private JTextField manufactureCostField;
    private JComboBox<UnitOfMeasure> unitComboBox;
    private JTextField orgNameField;
    private JTextField orgFullNameField;
    private JTextField employeesCountField;

    private JButton saveButton;
    private JButton cancelButton;

    public ProductEditDialog(Frame owner, Product existingProduct) {
        super(owner, true);
        this.localization = Localization.getInstance();
        this.client = GuiClient.getInstance();
        this.sessionState = SessionState.getInstance();
        this.existingProduct = existingProduct;

        initComponents();
        setTitle(existingProduct == null ?
                localization.getString("product.add.title") :
                localization.getString("product.edit.title"));
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель формы
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Поля продукта
        formPanel.add(new JLabel(localization.getString("label.name")));
        nameField = new JTextField(20);
        formPanel.add(nameField);

        formPanel.add(new JLabel(localization.getString("label.coordinates.x")));
        xField = new JTextField(10);
        formPanel.add(xField);

        formPanel.add(new JLabel(localization.getString("label.coordinates.y")));
        yField = new JTextField(10);
        formPanel.add(yField);

        formPanel.add(new JLabel(localization.getString("label.price")));
        priceField = new JTextField(10);
        formPanel.add(priceField);

        formPanel.add(new JLabel(localization.getString("label.part_number")));
        partNumberField = new JTextField(15);
        formPanel.add(partNumberField);

        formPanel.add(new JLabel(localization.getString("label.manufacture_cost")));
        manufactureCostField = new JTextField(10);
        formPanel.add(manufactureCostField);

        formPanel.add(new JLabel(localization.getString("label.unit_of_measure:")));
        unitComboBox = new JComboBox<>(UnitOfMeasure.values());
        formPanel.add(unitComboBox);

        // Разделитель для организации
        formPanel.add(new JLabel("--- Manufacturer (Optional) ---"));
        formPanel.add(new JLabel(""));

        formPanel.add(new JLabel("Organization Name:"));
        orgNameField = new JTextField(20);
        formPanel.add(orgNameField);

        formPanel.add(new JLabel("Organization Full Name:"));
        orgFullNameField = new JTextField(30);
        formPanel.add(orgFullNameField);

        formPanel.add(new JLabel("Employees Count:"));
        employeesCountField = new JTextField(10);
        formPanel.add(employeesCountField);

        // Заполняем поля, если редактируем существующий продукт
        if (existingProduct != null) {
            populateFields(existingProduct);
        }

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        saveButton = new JButton(localization.getString("button.save"));
        cancelButton = new JButton(localization.getString("button.cancel"));

        saveButton.addActionListener(this::saveProduct);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void populateFields(Product product) {
        nameField.setText(product.getName());
        xField.setText(String.valueOf(product.getCoordinates().getX()));
        yField.setText(String.valueOf(product.getCoordinates().getY()));
        priceField.setText(product.getPrice() != null ? String.valueOf(product.getPrice()) : "");
        partNumberField.setText(product.getPartNumber());
        manufactureCostField.setText(product.getManufactureCost() != null ? String.valueOf(product.getManufactureCost()) : "");
        unitComboBox.setSelectedItem(product.getUnitOfMeasure());

        if (product.getManufacturer() != null) {
            Organization org = product.getManufacturer();
            orgNameField.setText(org.getName());
            orgFullNameField.setText(org.getFullName());
            employeesCountField.setText(String.valueOf(org.getEmployeesCount()));
        }
    }

    private void saveProduct(ActionEvent e) {
        try {
            // Валидация и получение данных из полей
            String name = validateField(nameField, "Name");
            long x = Long.parseLong(validateField(xField, "X coordinate"));
            float y = Float.parseFloat(validateField(yField, "Y coordinate"));

            if (x <= -349) {
                throw new IllegalArgumentException("X coordinate must be greater than -349");
            }

            Long price = null;
            if (!priceField.getText().trim().isEmpty()) {
                price = Long.parseLong(priceField.getText().trim());
                if (price <= 0) {
                    throw new IllegalArgumentException("Price must be positive");
                }
            }

            String partNumber = validateField(partNumberField, "Part number");

            Float manufactureCost = null;
            if (!manufactureCostField.getText().trim().isEmpty()) {
                manufactureCost = Float.parseFloat(manufactureCostField.getText().trim());
            }

            UnitOfMeasure unit = (UnitOfMeasure) unitComboBox.getSelectedItem();

            // Обработка необязательных полей организации
            Organization manufacturer = null;
            String orgName = orgNameField.getText().trim();
            String orgFullName = orgFullNameField.getText().trim();
            String employeesCountText = employeesCountField.getText().trim();

            if (!orgName.isEmpty() || !orgFullName.isEmpty() || !employeesCountText.isEmpty()) {
                // Если хотя бы одно поле заполнено, проверяем все обязательные
                if (orgName.isEmpty()) {
                    throw new IllegalArgumentException("Organization name is required when other organization fields are filled");
                }
                if (orgFullName.isEmpty()) {
                    throw new IllegalArgumentException("Organization full name is required when other organization fields are filled");
                }
                if (employeesCountText.isEmpty()) {
                    throw new IllegalArgumentException("Employees count is required when other organization fields are filled");
                }

                long employeesCount = Long.parseLong(employeesCountText);
                if (employeesCount <= 0) {
                    throw new IllegalArgumentException("Employees count must be positive");
                }

                manufacturer = new Organization(orgName, orgFullName, employeesCount,
                        sessionState.getCurrentUser().getId());
            }

            // Создаем объекты
            Product product;
            if (existingProduct == null) {
                // Создаем новый продукт
                product = new Product(name, new Coordinates(x, y), price, partNumber,
                        manufactureCost, unit, manufacturer, sessionState.getCurrentUser().getId());
            } else {
                // Обновляем существующий продукт
                product = new Product(existingProduct.getId(), name, new Coordinates(x, y),
                        existingProduct.getCreationDate(), price, partNumber, manufactureCost,
                        unit, manufacturer, existingProduct.getCreatorId());
            }

            // Отправляем команду на сервер
            String command = existingProduct == null ? "add" : "update";
            String[] args = existingProduct == null ?
                    getAddArgs(product) : getUpdateArgs(product);

            client.sendCommandRequest(command, args, sessionState.getCurrentUser(), response -> {
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, response.getMessage(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, response.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String validateField(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        return value;
    }

    private String[] getAddArgs(Product product) {
        String[] args = new String[10];
        args[0] = product.getName();
        args[1] = String.valueOf(product.getCoordinates().getX());
        args[2] = String.valueOf(product.getCoordinates().getY());
        args[3] = product.getPrice() != null ? String.valueOf(product.getPrice()) : "";
        args[4] = product.getPartNumber();
        args[5] = product.getManufactureCost() != null ? String.valueOf(product.getManufactureCost()) : "";
        args[6] = product.getUnitOfMeasure().name();

        if (product.getManufacturer() != null) {
            args[7] = product.getManufacturer().getName();
            args[8] = product.getManufacturer().getFullName();
            args[9] = String.valueOf(product.getManufacturer().getEmployeesCount());
        } else {
            args[7] = "";
            args[8] = "";
            args[9] = "";
        }

        return args;
    }

    private String[] getUpdateArgs(Product product) {
        String[] args = new String[11];
        args[0] = String.valueOf(product.getId());
        args[1] = product.getName();
        args[2] = String.valueOf(product.getCoordinates().getX());
        args[3] = String.valueOf(product.getCoordinates().getY());
        args[4] = product.getPrice() != null ? String.valueOf(product.getPrice()) : "";
        args[5] = product.getPartNumber();
        args[6] = product.getManufactureCost() != null ? String.valueOf(product.getManufactureCost()) : "";
        args[7] = product.getUnitOfMeasure().name();

        if (product.getManufacturer() != null) {
            args[8] = product.getManufacturer().getName();
            args[9] = product.getManufacturer().getFullName();
            args[10] = String.valueOf(product.getManufacturer().getEmployeesCount());
        } else {
            args[8] = "";
            args[9] = "";
            args[10] = "";
        }

        return args;
    }
}