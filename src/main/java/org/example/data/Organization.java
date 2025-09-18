package org.example.data;

import java.io.Serializable;

public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String name;
    private String fullName;
    private long employeesCount;
    private int creatorId;

    // Конструктор для создания новых организаций (без id)
    public Organization(String name, String fullName, long employeesCount, int creatorId) {
        setName(name);
        setFullName(fullName);
        setEmployeesCount(employeesCount);
        setCreatorId(creatorId);
    }

    // Конструктор для загрузки из БД (с id)
    public Organization(long id, String name, String fullName, long employeesCount, int creatorId) {
        this(name, fullName, employeesCount, creatorId);
        setId(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be non-negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Organization name cannot be empty");
        }
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        this.fullName = fullName;
    }

    public long getEmployeesCount() {
        return employeesCount;
    }

    public void setEmployeesCount(long employeesCount) {
        if (employeesCount <= 0) {
            throw new IllegalArgumentException("Employees count must be positive");
        }
        this.employeesCount = employeesCount;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Organization[id=%d, name='%s', fullName='%s', employees=%d, creatorId=%d]",
                id, name, fullName, employeesCount, creatorId
        );
    }
}