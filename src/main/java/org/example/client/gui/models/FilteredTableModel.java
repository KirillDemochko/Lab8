package org.example.client.gui.models;

import org.example.data.Product;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilteredTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "ID", "Name", "Coordinates", "Creation Date", "Price",
            "Part Number", "Manufacture Cost", "Unit", "Manufacturer", "Creator"
    };

    private List<Product> originalProducts;
    private List<Product> filteredProducts;
    private Predicate<Product> currentFilter;

    public FilteredTableModel(List<Product> products) {
        this.originalProducts = new ArrayList<>(products);
        this.filteredProducts = new ArrayList<>(products);
        this.currentFilter = product -> true;
    }
    public void updateTableHeaders() {
        fireTableStructureChanged();
    }
    public void setProducts(List<Product> products) {
        this.originalProducts = new ArrayList<>(products);
        applyFilter(currentFilter);
    }

    public void applyFilter(Predicate<Product> filter) {
        this.currentFilter = filter;
        this.filteredProducts = originalProducts.stream()
                .filter(filter)
                .collect(Collectors.toList());
        fireTableDataChanged();
    }

    public void clearFilter() {
        applyFilter(product -> true);
    }

    public Product getProductAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < filteredProducts.size()) {
            return filteredProducts.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return filteredProducts.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = filteredProducts.get(rowIndex);

        switch (columnIndex) {
            case 0: return product.getId();
            case 1: return product.getName();
            case 2: return product.getCoordinates().toString();
            case 3: return product.getCreationDate().toString();
            case 4: return product.getPrice();
            case 5: return product.getPartNumber();
            case 6: return product.getManufactureCost();
            case 7: return product.getUnitOfMeasure();
            case 8: return product.getManufacturer() != null ?
                    product.getManufacturer().getName() : "None";
            case 9: return product.getCreatorId();
            default: return null;
        }
    }
}