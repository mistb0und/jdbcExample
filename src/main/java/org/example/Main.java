package org.example;


import java.sql.*;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/skillbox";
    private static final String USER = "root";
    private static final String PASSWORD = "14553228sql";

    public static void main(String[] args) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            transactionExample(connection);
        }
    }

    private static void transactionExample(Connection connection) throws SQLException {
        connection.setAutoCommit(false);

        try {
            insertCourse(connection, "Course A", 1000);
            insertCourse(connection, "Course B", 2000);

            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO courses(id, name, price) VALUES (1, 'Duplicate', 1)");
            }

            connection.commit();
            System.out.println("Транзакція закомічена");
        } catch (SQLException e) {
            connection.rollback();
            System.out.println("Помилка, відкат транзакції: " + e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void insertCourse(Connection connection, String name, int price) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO courses(name, price) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, price);
            ps.executeUpdate();
        }
    }

    private static void addCourseBatch(Connection connection) {
        String[] names = {"Java", "Python", "Go", "Rust", "Kotlin"};
        int[] prices = {10000, 12000, 15000, 20000, 18000};

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO courses(name,price) VALUES(?, ?)"
        )) {
            for (int i = 0; i < names.length; i++) {
                preparedStatement.setString(1, names[i]);
                preparedStatement.setInt(2, prices[i]);
                preparedStatement.addBatch();
            }

            int[] results = preparedStatement.executeBatch();
            System.out.println("Добавлено курсів " + results.length);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteCourse(Connection connection, String name) throws SQLException {
        PreparedStatement findCourseIdPs
                = connection.prepareStatement("SELECT id from courses where name= ?");

        findCourseIdPs.setString(1, name);
        ResultSet resultSet = findCourseIdPs.executeQuery();
        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            printCourses(connection, id);

            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM courses WHERE id = ?"
            );
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            System.out.println("Видалено рядків: " + result);
            printCourses(connection, id);
        } else {
            System.out.println("Курс '" + name + "' не знайдено");
        }
    }

    private static void updatePrice(Connection connection) throws SQLException {
        printCourses(connection, 5);

        PreparedStatement ps = connection.prepareStatement(
                "UPDATE courses SET price = ? WHERE id = ?"
        );
        ps.setInt(1, 11999);
        ps.setInt(2, 5);
        int result = ps.executeUpdate();
        System.out.println("Оновлено рядків: " + result);
        printCourses(connection, 5);
    }

    private static void printCourses(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name, price from courses where id = ?"
        );
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            System.out.printf("%-2d | %-35s | %d%n",
                    resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("price"));
        } else {
            System.out.println("Курс з id=" + id + " не знайдено");
        }
    }

    private static void addCourse(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO courses(name, price) values (?, ?)"
        );
        preparedStatement.setString(1, "Java Basic");
        preparedStatement.setInt(2, 9999);
        int result = preparedStatement.executeUpdate();
        System.out.println("Додано рядків: " + result);
    }

    private static void selectAndPrintCourses(Statement statement) throws SQLException {
        ResultSet selectCourses = statement.executeQuery("SELECT id, name, price from courses");
        while (selectCourses.next()) {
            int id = selectCourses.getInt("id");
            String name = selectCourses.getString("name");
            int price = selectCourses.getInt("price");
            System.out.printf("%-2d | %-35s | %d%n", id, name, price);
        }
    }
}
