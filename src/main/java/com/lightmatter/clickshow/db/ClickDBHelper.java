package com.lightmatter.clickshow.db;


import com.lightmatter.clickshow.HelloApplication;
import com.lightmatter.clickshow.model.ClickStatistic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClickDBHelper {
    private static Logger log = LogManager.getLogger(HelloApplication.class.getName());

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


}
