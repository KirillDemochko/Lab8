package org.example.client.gui.models;

import org.example.client.gui.resources.Localization;
import org.example.data.Product;
import org.example.data.UnitOfMeasure;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductTableModel extends AbstractTableModel {
    private final Localization localization;
    private List<Product> products;
    private List<Product> filteredProducts;
    private Predicate<Product> currentFilter;

    public ProductTableModel(List<Product> products) {
        this.localization = Localization.getInstance();
        this.products = new ArrayList<>(products);
        this.filteredProducts = new ArrayList<>(products);
        this.currentFilter = product -> true;
    }

    public void setProducts(List<Product> products) {
        this.products = new ArrayList<>(products);
        applyFilter(currentFilter);
    }

    public Product getProductAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < filteredProducts.size()) {
            return filteredProducts.get(rowIndex);
        }
        return null;
    }

    public void applyFilter(Predicate<Product> filter) {
        this.currentFilter = filter;
        this.filteredProducts = products.stream()
                .filter(filter)
                .collect(Collectors.toList());
        fireTableDataChanged();
    }

    public void clearFilter() {
        applyFilter(product -> true);
    }

    @Override
    public int getRowCount() {
        return filteredProducts.size();
    }

    @Override
    public int getColumnCount() {
        return 11;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0: return localization.getString("table.header.id");
            case 1: return localization.getString("table.header.name");
            case 2: return localization.getString("table.header.coordinates") + " X";
            case 3: return localization.getString("table.header.coordinates") + " Y";
            case 4: return localization.getString("table.header.creationDate");
            case 5: return localization.getString("table.header.price");
            case 6: return localization.getString("table.header.partNumber");
            case 7: return localization.getString("table.header.manufactureCost");
            case 8: return localization.getString("table.header.unit");
            case 9: return localization.getString("table.header.manufacturer");
            case 10: return localization.getString("table.header.creator");
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Long.class;
            case 1: return String.class;
            case 2: return Long.class;
            case 3: return Float.class;
            case 4: return String.class;
            case 5: return Long.class;
            case 6: return String.class;
            case 7: return Float.class;
            case 8: return UnitOfMeasure.class;
            case 9: return String.class;
            case 10: return Integer.class;
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = filteredProducts.get(rowIndex);

        switch (columnIndex) {
            case 0: return product.getId();
            case 1: return product.getName();
            case 2: return product.getCoordinates().getX();
            case 3: return product.getCoordinates().getY();
            case 4: return product.getCreationDate().toString();
            case 5: return product.getPrice();
            case 6: return product.getPartNumber();
            case 7: return product.getManufactureCost();
            case 8: return product.getUnitOfMeasure();
            case 9: return product.getManufacturer() != null ?
                    product.getManufacturer().getName() : "None";
            case 10: return product.getCreatorId();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 || columnIndex == 5 || columnIndex == 6 || columnIndex == 7;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Product product = filteredProducts.get(rowIndex);

        switch (columnIndex) {
            case 1: product.setName((String) aValue); break;
            case 5:
                if (aValue instanceof Long) {
                    product.setPrice((Long) aValue);
                } else if (aValue instanceof String) {
                    try {
                        product.setPrice(Long.parseLong((String) aValue));
                    } catch (NumberFormatException e) {
                    }
                }
                break;
            case 6: product.setPartNumber((String) aValue); break;
            case 7:
                if (aValue instanceof Float) {
                    product.setManufactureCost((Float) aValue);
                } else if (aValue instanceof String) {
                    try {
                        product.setManufactureCost(Float.parseFloat((String) aValue));
                    } catch (NumberFormatException e) {
                    }
                }
                break;
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }
}