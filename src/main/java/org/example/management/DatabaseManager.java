package org.example.management;

import org.example.data.Product;
import org.example.data.Coordinates;
import org.example.data.Organization;
import org.example.data.UnitOfMeasure;
import org.example.data.User;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String CHECK_DRIVER = "SELECT 1";
    private static final String CHECK_USERS_TABLE = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users' AND table_schema = 'public')";
    private static final String CHECK_PRODUCTS_TABLE = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'products' AND table_schema = 'public')";
    private static final String CHECK_ORGANIZATIONS_TABLE = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'organizations' AND table_schema = 'public')";


    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS s465751.users (
            id SERIAL PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            password_hash VARCHAR(64) NOT NULL
        )
    """;

    private static final String CREATE_ORGANIZATIONS_TABLE = """
        CREATE TABLE IF NOT EXISTS s465751.organizations (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            full_name VARCHAR(150) NOT NULL UNIQUE,
            employees_count BIGINT NOT NULL CHECK (employees_count > 0),
            creator_id INTEGER REFERENCES users(id)
        )
    """;

    private static final String CREATE_PRODUCTS_TABLE = """
        CREATE TABLE IF NOT EXISTS s465751.products (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            coordinates_x BIGINT NOT NULL CHECK (coordinates_x > -349),
            coordinates_y REAL NOT NULL,
            creation_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            price BIGINT CHECK (price > 0),
            part_number VARCHAR(50) NOT NULL UNIQUE,
            manufacture_cost REAL,
            unit_of_measure VARCHAR(20) NOT NULL,
            manufacturer_id BIGINT REFERENCES organizations(id),
            creator_id INTEGER REFERENCES users(id)
        )
    """;

    private static final String INSERT_USER = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id";
    private static final String SELECT_USER_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_ORGANIZATION = """
        INSERT INTO organizations (name, full_name, employees_count, creator_id)
        VALUES (?, ?, ?, ?) RETURNING id
    """;
    private static final String INSERT_PRODUCT = """
        INSERT INTO products (name, coordinates_x, coordinates_y, price, part_number, manufacture_cost, unit_of_measure, manufacturer_id, creator_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
    """;
    private static final String SELECT_ALL_PRODUCTS = """
        SELECT p.*, o.id as org_id, o.name as org_name, o.full_name as org_full_name, 
               o.employees_count as org_employees, o.creator_id as org_creator_id
        FROM products p LEFT JOIN organizations o ON p.manufacturer_id = o.id
    """;
    private static final String UPDATE_PRODUCT = """
        UPDATE products SET name = ?, coordinates_x = ?, coordinates_y = ?, price = ?, 
        part_number = ?, manufacture_cost = ?, unit_of_measure = ?, manufacturer_id = ?
        WHERE id = ? AND creator_id = ?
    """;
    private static final String DELETE_PRODUCT = "DELETE FROM products WHERE id = ? AND creator_id = ?";
    private static final String DELETE_USER_PRODUCTS = "DELETE FROM products WHERE creator_id = ?";
    private static final String DELETE_ORGANIZATION = "DELETE FROM organizations WHERE id = ? AND creator_id = ?";
    private static final String SAVE_PRODUCT = """
        INSERT INTO products (id, name, coordinates_x, coordinates_y, creation_date, price, part_number, 
        manufacture_cost, unit_of_measure, manufacturer_id, creator_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            coordinates_x = EXCLUDED.coordinates_x,
            coordinates_y = EXCLUDED.coordinates_y,
            creation_date = EXCLUDED.creation_date,
            price = EXCLUDED.price,
            part_number = EXCLUDED.part_number,
            manufacture_cost = EXCLUDED.manufacture_cost,
            unit_of_measure = EXCLUDED.unit_of_measure,
            manufacturer_id = EXCLUDED.manufacturer_id
    """;
    private static final String SAVE_ORGANIZATION = """
        INSERT INTO organizations (id, name, full_name, employees_count, creator_id)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            full_name = EXCLUDED.full_name,
            employees_count = EXCLUDED.employees_count
    """;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Проверка что драйвер работает
            stmt.executeQuery(CHECK_DRIVER);
            System.out.println("JDBC драйвер работает корректно");

        } catch (SQLException e) {
            System.err.println("Ошибка инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        String query;
        switch (tableName) {
            case "users": query = CHECK_USERS_TABLE; break;
            case "products": query = CHECK_PRODUCTS_TABLE; break;
            case "organizations": query = CHECK_ORGANIZATIONS_TABLE; break;
            default: return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getBoolean(1);
        }
    }

    public User registerUser(String username, String passwordHash) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                return new User(id, username, passwordHash);
            }
            throw new SQLException("Не удалось зарегистрировать пользователя");
        }
    }
    public String getUsernameById(int userId) throws SQLException {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("username") : "Unknown";
        }
    }
    public User getUserByUsername(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String storedHash = rs.getString("password_hash");
                return new User(id, username, storedHash);
            }
            return null;
        }
    }
    public User authenticateUser(String username, String passwordHash) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_BY_USERNAME)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String storedHash = rs.getString("password_hash");

                if (storedHash.equals(passwordHash)) {
                    return new User(id, username, storedHash);
                }
            }
            return null;
        }
    }

    public List<Product> loadProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_PRODUCTS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                new Coordinates(rs.getLong("coordinates_x"), rs.getFloat("coordinates_y")),
                rs.getTimestamp("creation_date").toInstant().atZone(ZoneId.systemDefault()),
                rs.getObject("price", Long.class),
                rs.getString("part_number"),
                rs.getObject("manufacture_cost", Float.class),
                UnitOfMeasure.valueOf(rs.getString("unit_of_measure")),
                rs.getObject("manufacturer_id") != null ? mapOrganization(rs) : null,
                rs.getInt("creator_id")
        );
    }

    private Organization mapOrganization(ResultSet rs) throws SQLException {
        return new Organization(
                rs.getLong("org_id"),
                rs.getString("org_name"),
                rs.getString("org_full_name"),
                rs.getLong("org_employees"),
                rs.getInt("org_creator_id")
        );
    }

    public Long addProduct(Product product, int creatorId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Long manufacturerId = null;
            if (product.getManufacturer() != null) {
                manufacturerId = insertOrganization(conn, product.getManufacturer(), creatorId);
            }

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_PRODUCT, Statement.RETURN_GENERATED_KEYS)) {
                bindProductParameters(stmt, product, manufacturerId, creatorId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Не удалось добавить продукт");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        conn.commit();
                        return id;
                    } else {
                        throw new SQLException("Не удалось получить ID продукта");
                    }
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Ошибка при откате транзакции: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }
    }

    private Long insertOrganization(Connection conn, Organization org, int creatorId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_ORGANIZATION, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, org.getName());
            stmt.setString(2, org.getFullName());
            stmt.setLong(3, org.getEmployeesCount());
            stmt.setInt(4, creatorId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Не удалось добавить организацию");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Не удалось получить ID организации");
                }
            }
        }
    }

    private void bindProductParameters(PreparedStatement stmt, Product product,
                                       Long manufacturerId, int creatorId) throws SQLException {
        stmt.setString(1, product.getName());
        stmt.setLong(2, product.getCoordinates().getX());
        stmt.setFloat(3, product.getCoordinates().getY());
        stmt.setObject(4, product.getPrice());
        stmt.setString(5, product.getPartNumber());
        stmt.setObject(6, product.getManufactureCost());
        stmt.setString(7, product.getUnitOfMeasure().name());
        stmt.setObject(8, manufacturerId);
        stmt.setInt(9, creatorId);
    }

    public boolean updateProduct(Long id, Product newProduct, int creatorId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Получаем старый продукт для получения manufacturer_id
            Product oldProduct = getProductById(conn, id, creatorId);
            if (oldProduct == null) return false;

            Long oldManufacturerId = oldProduct.getManufacturer() != null ? oldProduct.getManufacturer().getId() : null;
            Long newManufacturerId = null;

            if (newProduct.getManufacturer() != null) {
                if (oldManufacturerId != null) {
                    // Обновляем существующую организацию
                    updateOrganization(conn, oldManufacturerId, newProduct.getManufacturer(), creatorId);
                    newManufacturerId = oldManufacturerId;
                } else {
                    // Создаем новую организацию
                    newManufacturerId = insertOrganization(conn, newProduct.getManufacturer(), creatorId);
                }
            } else if (oldManufacturerId != null) {
                // Удаляем старую организацию, если она была
                deleteOrganization(conn, oldManufacturerId, creatorId);
            }

            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_PRODUCT)) {
                stmt.setString(1, newProduct.getName());
                stmt.setLong(2, newProduct.getCoordinates().getX());
                stmt.setFloat(3, newProduct.getCoordinates().getY());
                stmt.setObject(4, newProduct.getPrice());
                stmt.setString(5, newProduct.getPartNumber());
                stmt.setObject(6, newProduct.getManufactureCost());
                stmt.setString(7, newProduct.getUnitOfMeasure().name());
                stmt.setObject(8, newManufacturerId);
                stmt.setLong(9, id);
                stmt.setInt(10, creatorId);

                int affectedRows = stmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Ошибка при откате транзакции: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }
    }

    private Product getProductById(Connection conn, Long id, int creatorId) throws SQLException {
        String query = SELECT_ALL_PRODUCTS + " WHERE p.id = ? AND p.creator_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, id);
            stmt.setInt(2, creatorId);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapProduct(rs) : null;
        }
    }

    private void updateOrganization(Connection conn, Long id, Organization org, int creatorId) throws SQLException {
        String query = "UPDATE organizations SET name = ?, full_name = ?, employees_count = ? WHERE id = ? AND creator_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, org.getName());
            stmt.setString(2, org.getFullName());
            stmt.setLong(3, org.getEmployeesCount());
            stmt.setLong(4, id);
            stmt.setInt(5, creatorId);
            stmt.executeUpdate();
        }
    }

    private void deleteOrganization(Connection conn, Long id, int creatorId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_ORGANIZATION)) {
            stmt.setLong(1, id);
            stmt.setInt(2, creatorId);
            stmt.executeUpdate();
        }
    }

    public boolean removeProduct(Long id) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Сначала получаем продукт, чтобы получить информацию о производителе
            Product product = getProductById(conn, id);
            if (product == null) {
                conn.rollback();
                return false;
            }

            // Удаляем продукт
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                stmt.setLong(1, id);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Если у продукта был производитель, проверяем, не используется ли он другими продуктами
            if (product.getManufacturer() != null) {
                if (!isOrganizationUsedByOtherProducts(conn, product.getManufacturer().getId(), id)) {
                    deleteOrganization(conn, product.getManufacturer().getId());
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Ошибка при откате транзакции: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }
    }

    private boolean isOrganizationUsedByOtherProducts(Connection conn, Long organizationId, Long excludingProductId) throws SQLException {
        String query = "SELECT COUNT(*) FROM products WHERE manufacturer_id = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, organizationId);
            stmt.setLong(2, excludingProductId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void deleteOrganization(Connection conn, Long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM organizations WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    private Product getProductById(Connection conn, Long id) throws SQLException {
        String query = SELECT_ALL_PRODUCTS + " WHERE p.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapProduct(rs) : null;
        }
    }

    public boolean clearUserProducts(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_USER_PRODUCTS)) {

            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public void saveCollection(List<Product> products, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Сохраняем организации сначала
            for (Product product : products) {
                if (product.getManufacturer() != null && product.getCreatorId() == userId) {
                    saveOrganization(conn, product.getManufacturer(), userId);
                }
            }

            // Сохраняем продукты
            for (Product product : products) {
                if (product.getCreatorId() == userId) {
                    saveProduct(conn, product, userId);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Ошибка при откате транзакции: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }
    }

    private void saveOrganization(Connection conn, Organization org, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SAVE_ORGANIZATION)) {
            stmt.setLong(1, org.getId());
            stmt.setString(2, org.getName());
            stmt.setString(3, org.getFullName());
            stmt.setLong(4, org.getEmployeesCount());
            stmt.setInt(5, userId);
            stmt.executeUpdate();
        }
    }

    private void saveProduct(Connection conn, Product product, int userId) throws SQLException {
        Long manufacturerId = product.getManufacturer() != null ? product.getManufacturer().getId() : null;

        try (PreparedStatement stmt = conn.prepareStatement(SAVE_PRODUCT)) {
            stmt.setLong(1, product.getId());
            stmt.setString(2, product.getName());
            stmt.setLong(3, product.getCoordinates().getX());
            stmt.setFloat(4, product.getCoordinates().getY());
            stmt.setTimestamp(5, Timestamp.from(product.getCreationDate().toInstant()));
            stmt.setObject(6, product.getPrice());
            stmt.setString(7, product.getPartNumber());
            stmt.setObject(8, product.getManufactureCost());
            stmt.setString(9, product.getUnitOfMeasure().name());
            stmt.setObject(10, manufacturerId);
            stmt.setInt(11, userId);
            stmt.executeUpdate();
        }
    }
}