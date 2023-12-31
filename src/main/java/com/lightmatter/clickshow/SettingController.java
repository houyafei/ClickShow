package com.lightmatter.clickshow;

import com.lightmatter.clickshow.autostart.AutoStartControl;
import com.lightmatter.clickshow.db.ClickDBHelper;
import com.lightmatter.clickshow.model.ClickStatistic;
import com.lightmatter.clickshow.model.Configuration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class SettingController {

    private static final Logger log = LoggerFactory.getLogger(SettingController.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final TreeMap<String, Integer> recordMoueMap = new TreeMap<>();
    private final TreeMap<String, Integer> recordKeyMap = new TreeMap<>();



    public DatePicker startDate;
    public DatePicker endData;
    @FXML
    public LineChart lineChart;
    public CheckBox autoStart;

    private boolean checkAutoStart = false;

    private LocalDate start, end;

    @FXML
    public void initialize() {
        start = LocalDate.now().minusDays(7);
        end = LocalDate.now();
        startDate.setValue(start);
        endData.setValue(end);

        initData();
        populateLineChart();

    }

    private void initData() {
        // 获历史点击数据
        queryHistoryData();
        // 获取配置数据
        queryConfig();

    }

    private void queryConfig() {
        try {
            Configuration autoConfig =  ClickDBHelper.findByConfigType("autoStart");
            checkAutoStart = Boolean.parseBoolean(autoConfig.getConfigValue());
            autoStart.setSelected(checkAutoStart);
            // 设置自启动
            new Thread(() -> new AutoStartControl().setAutoStart(checkAutoStart)).start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }



    public void goBackMainButtonAction(ActionEvent actionEvent) {
        try {
            if (HelloApplication.scene1 == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
                HelloApplication.scene1 = new Scene(fxmlLoader.load(), 800, 600);
            }
            HelloApplication.primaryStage.setScene(HelloApplication.scene1);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void goSearch(ActionEvent actionEvent) {
        start = startDate.getValue();
        end = endData.getValue();

        recordMoueMap.clear();
        recordKeyMap.clear();

        queryHistoryData();

        populateLineChart();


    }

    private void queryHistoryData() {
        List<ClickStatistic> data;
        try {
            data = ClickDBHelper.findByCreateTimeRange(Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atStartOfDay()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (ClickStatistic datum : data) {
            String key = dateFormat.format(datum.getCreateTime());
            recordMoueMap.put(key, recordMoueMap.getOrDefault(key, 0) + datum.getMouseClickCount());
            recordKeyMap.put(key, recordKeyMap.getOrDefault(key, 0) + datum.getKeyClickCount());
        }
    }

    /**
     * 构造线形图
     */
    @SuppressWarnings("unchecked")
    public void populateLineChart() {
        if (recordKeyMap.isEmpty() || recordMoueMap.isEmpty()){
            return;
        }
        // 创建并设置Y坐标轴
        XYChart.Series<String, Number> mouseSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> keySeries = new XYChart.Series<>();

        mouseSeries.setName("鼠标点击次数");
        keySeries.setName("键盘点击次数");

        updateLineChart2(recordMoueMap, mouseSeries);
        updateLineChart2(recordKeyMap, keySeries);
        lineChart.setAnimated(false);
        lineChart.getData().clear();
        lineChart.getData().addAll(mouseSeries, keySeries);

    }

    private void updateLineChart2(TreeMap<String, Integer> record, XYChart.Series<String, Number> mouseSeries) {
        record.forEach((k, v) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(k, v);
            StackPane stackPane = new StackPane();
            Text dataLabel = new Text(data.getYValue().toString());
            stackPane.getChildren().add(dataLabel);
            data.setNode(stackPane);
            Tooltip tooltip = new Tooltip(String.format("%s\n鼠标：%d\n键盘：%d\n",k,recordMoueMap.getOrDefault(k,0),recordKeyMap.getOrDefault(k,0)));
            Tooltip.install(data.getNode(), tooltip);
            mouseSeries.getData().add(data);
        });
    }

    public void closeWind(MouseEvent mouseEvent) {
        System.exit(1);
    }

    public void openSettingTab(Event event) {
        autoStart.setSelected(checkAutoStart);
    }

    public void updateCheckBoxStatus(ActionEvent actionEvent) throws SQLException {
        checkAutoStart = autoStart.isSelected();
        ClickDBHelper.updateConfigValueByConfigType("autoStart",String.valueOf(checkAutoStart));
        // 设置自启动
        new Thread(() -> new AutoStartControl().setAutoStart(checkAutoStart)).start();
    }
}
