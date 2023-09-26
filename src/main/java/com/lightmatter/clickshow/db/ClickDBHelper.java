package com.lightmatter.clickshow.db;


import com.lightmatter.clickshow.HelloApplication;
import com.lightmatter.clickshow.model.ClickStatistic;
import com.lightmatter.clickshow.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClickDBHelper {
    private static Logger log = LoggerFactory.getLogger(HelloApplication.class.getName());

    private static final String SQLITE_JDBC_URL = "jdbc:sqlite:clickDb.db";

    private static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_JDBC_URL);
            log.info("Connection established.");
        } catch (SQLException | ClassNotFoundException e) {
            log.error("connect error ",e);
            throw new RuntimeException(e);
        }

        return connection;
    }

    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS click_statistic (\n"
                + " hour_key varchar(15) PRIMARY KEY,\n"
                + " mouse_click_count int default 0,\n"
                + " key_click_count int default 0,\n"
                + " create_time timestamp default currenttimestamp \n"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createConfigTable() {
        String sql ="CREATE TABLE IF NOT EXISTS configurations (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    config_type VARCHAR(255) NOT NULL,\n" +
                "    config_value VARCHAR(255) NOT NULL\n" +
                ");\n";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void insert(ClickStatistic clickStatistic) throws Exception {
        String sql = "INSERT INTO click_statistic(hour_key, mouse_click_count, key_click_count, create_time) VALUES(?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clickStatistic.getHourKey());
            pstmt.setInt(2, clickStatistic.getMouseClickCount());
            pstmt.setInt(3, clickStatistic.getKeyClickCount());
            pstmt.setTimestamp(4, clickStatistic.getCreateTime());
            pstmt.executeUpdate();
        }
    }

    public static void update(ClickStatistic clickStatistic) throws Exception {
        String sql = "UPDATE click_statistic SET mouse_click_count = ?, key_click_count = ?, create_time = ? WHERE hour_key = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clickStatistic.getMouseClickCount());
            pstmt.setInt(2, clickStatistic.getKeyClickCount());
            pstmt.setTimestamp(3, clickStatistic.getCreateTime());
            pstmt.setString(4, clickStatistic.getHourKey());
            pstmt.executeUpdate();
        }
    }

    public static ClickStatistic findByHourKey(String hourKey) throws Exception {
        ClickStatistic clickStatistic = null;
        String sql = "SELECT * FROM click_statistic WHERE hour_key = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hourKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                clickStatistic = new ClickStatistic();
                clickStatistic.setHourKey(rs.getString("hour_key"));
                clickStatistic.setMouseClickCount(rs.getInt("mouse_click_count"));
                clickStatistic.setKeyClickCount(rs.getInt("key_click_count"));
                clickStatistic.setCreateTime(rs.getTimestamp("create_time"));
            }
        }
        return clickStatistic;
    }

    public static List<ClickStatistic> findByCreateTimeRange(Timestamp start, Timestamp end) throws Exception {
        List<ClickStatistic> statisticsList = new ArrayList<>();
        String sql = "SELECT * FROM click_statistic WHERE create_time BETWEEN ? AND ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, start);
            pstmt.setTimestamp(2, end);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ClickStatistic clickStatistic = new ClickStatistic();
                clickStatistic.setHourKey(rs.getString("hour_key"));
                clickStatistic.setMouseClickCount(rs.getInt("mouse_click_count"));
                clickStatistic.setKeyClickCount(rs.getInt("key_click_count"));
                clickStatistic.setCreateTime(rs.getTimestamp("create_time"));
                statisticsList.add(clickStatistic);
            }
        }
        return statisticsList;
    }


    // 根据config_type查询配置
    public static Configuration findByConfigType(String configType) throws SQLException {
        String sql = "SELECT * FROM configurations WHERE config_type = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, configType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Configuration(rs.getInt("id"), rs.getString("config_type"), rs.getString("config_value"));
                }
            }
        }
        return null;
    }

    // 根据config_type更新配置值
    public static boolean updateConfigValueByConfigType(String configType, String newValue) throws SQLException {
        String sql = "UPDATE configurations SET config_value = ? WHERE config_type = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newValue);
            stmt.setString(2, configType);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean upsertAutoStartConfig( String configValue) throws SQLException {
        String sql = "INSERT INTO configurations (config_type, config_value) VALUES ('autoStart', ?) " +
                "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, configValue);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean addAutoStartIfNotExists(String configValue) throws SQLException {
        // 首先检查是否存在 config_type = 'autoStart' 的配置
        Configuration existingConfig = findByConfigType( "autoStart");
        if (existingConfig != null) {
            // 如果已存在，不进行任何操作，并返回 false
            return false;
        }

        // 如果不存在，插入新的记录
        String sql = "INSERT INTO configurations (config_type, config_value) VALUES ('autoStart', ?)";
        try (Connection conn = connect();PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, configValue);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }


}
