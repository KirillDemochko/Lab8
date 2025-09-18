package org.example.client.gui.components;

import org.example.client.gui.models.ProductTableModel;
import org.example.data.Product;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProductTable extends JTable {
    private final ProductTableModel tableModel;
    private TableRowSorter<ProductTableModel> sorter;

    public ProductTable(ProductTableModel tableModel) {
        super(tableModel);
        this.tableModel = tableModel;
        this.sorter = new TableRowSorter<>(tableModel);

        setRowSorter(sorter);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoCreateRowSorter(true);
        setFillsViewportHeight(true);

        // Добавляем обработчик двойного клика для редактирования
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedProduct();
                }
            }
        });
    }
    public void addSelectionListener() {
        getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Product selected = getSelectedProduct();
                if (selected != null) {
                    // Найти панель визуализации и вызвать метод выделения
                    Container parent = getParent();
                    while (parent != null && !(parent instanceof VisualizationPanel)) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof VisualizationPanel vp) {
                        vp.highlightProduct(selected);
                    }
                }
            }
        });
    }
    public Product getSelectedProduct() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = convertRowIndexToModel(selectedRow);
            return tableModel.getProductAt(modelRow);
        }
        return null;
    }

    private void editSelectedProduct() {
        Product selectedProduct = getSelectedProduct();
        if (selectedProduct != null) {
            // Здесь будет вызов диалога редактирования
            // Для этого нужно передать ссылку на родительское окно
            JOptionPane.showMessageDialog(this,
                    "Edit product: " + selectedProduct.getName(),
                    "Edit",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void applyFilter(RowFilter<? super ProductTableModel, ? super Integer> filter) {
        sorter.setRowFilter(filter);
    }

    public void clearFilter() {
        sorter.setRowFilter(null);
    }
}