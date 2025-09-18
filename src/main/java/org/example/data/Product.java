package org.example.data;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Product implements Comparable<Product>, Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Coordinates coordinates;
    private ZonedDateTime creationDate;
    private Long price;
    private String partNumber;
    private Float manufactureCost;
    private UnitOfMeasure unitOfMeasure;
    private Organization manufacturer;
    private int creatorId;

    // Конструктор для создания нового продукта (без ID)
    public Product(String name, Coordinates coordinates, Long price,
                   String partNumber, Float manufactureCost,
                   UnitOfMeasure unitOfMeasure, Organization manufacturer, int creatorId) {
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = ZonedDateTime.now();
        this.price = price;
        this.partNumber = partNumber;
        this.manufactureCost = manufactureCost;
        this.unitOfMeasure = unitOfMeasure;
        this.manufacturer = manufacturer;
        this.creatorId = creatorId;
        this.id = 0L; // Временный ID
    }

    // Полный конструктор (для загрузки из БД)
    public Product(Long id, String name, Coordinates coordinates, ZonedDateTime creationDate,
                   Long price, String partNumber, Float manufactureCost,
                   UnitOfMeasure unitOfMeasure, Organization manufacturer, int creatorId) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.price = price;
        this.partNumber = partNumber;
        this.manufactureCost = manufactureCost;
        this.unitOfMeasure = unitOfMeasure;
        this.manufacturer = manufacturer;
        this.creatorId = creatorId;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        this.coordinates = coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Creation date cannot be null");
        }
        this.creationDate = creationDate;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        if (price != null && price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = price;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        if (partNumber == null || partNumber.isEmpty()) {
            throw new IllegalArgumentException("Part number cannot be empty");
        }
        this.partNumber = partNumber;
    }

    public Float getManufactureCost() {
        return manufactureCost;
    }

    public void setManufactureCost(Float manufactureCost) {
        this.manufactureCost = manufactureCost;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasure == null) {
            throw new IllegalArgumentException("Unit of measure cannot be null");
        }
        this.unitOfMeasure = unitOfMeasure;
    }

    public Organization getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Organization manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        if (creatorId <= 0) {
            throw new IllegalArgumentException("Creator ID must be positive");
        }
        this.creatorId = creatorId;
    }

    @Override
    public int compareTo(Product other) {
        if (this.manufactureCost == null && other.manufactureCost == null) {
            return 0;
        }
        if (this.manufactureCost == null) {
            return -1;
        }
        if (other.manufactureCost == null) {
            return 1;
        }
        return Float.compare(this.manufactureCost, other.manufactureCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format(
                "Product[id=%d, name='%s', creatorId=%d, coordinates=%s, creationDate=%s, price=%d]",
                id, name, creatorId, coordinates, creationDate, price
        );
    }
}