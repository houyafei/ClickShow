package com.lightmatter.clickshow;

import com.lightmatter.clickshow.db.ClickDBHelper;
import com.lightmatter.clickshow.model.ClickStatistic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseAdapter;
import org.jnativehook.mouse.NativeMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class.getName());

    private enum ClickType {
        MOUSE, KEY
    }

    private int mouseClickCount;

    private int keyClickCount;

    private final TreeMap<String, Integer> recordMoueMap = new TreeMap<>();
    private final TreeMap<String, Integer> recordKeyMap = new TreeMap<>();

    private final ObservableList<XYChart.Data<String, Number>> recordMoueList = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<String, Number>> recordKeyList = FXCollections.observableArrayList();

    @FXML
    private Label clickKeyboardText;

    @FXML
    private Label clickMouseText;


    @FXML
    private BarChart<String, Number> barChart;


    @FXML
    public void initialize() {
        initRecordData();
        initTextView();
        populateBarChart();
        addListener();

    }

    private void initTextView() {
        Platform.runLater(() -> {
            clickMouseText.setText(String.format("* %d *", mouseClickCount));
            clickKeyboardText.setText(String.format("* %d *", keyClickCount));
        });
    }

    /**
     * 打开时，自动获取当天的点击次数信息，然后初始化一些数据
     */
    private void initRecordData() {
        // 获今天的点击数据
        List<ClickStatistic> todayData;
        try {
            todayData = ClickDBHelper.findByCreateTimeRange(Timestamp.valueOf(LocalDate.now().atStartOfDay()),
                    Timestamp.valueOf(LocalDate.now().atStartOfDay().plusDays(1)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 更新map
        if (todayData.isEmpty()) {
            return;
        }
        todayData.sort(ClickStatistic::compareTo);
        for (ClickStatistic todayDatum : todayData) {
            mouseClickCount += todayDatum.getMouseClickCount();
            keyClickCount += todayDatum.getKeyClickCount();
            recordMoueMap.put(todayDatum.getHourKey(), todayDatum.getMouseClickCount());
            recordKeyMap.put(todayDatum.getHourKey(), todayDatum.getKeyClickCount());
        }
    }

    private void addListener() {
        GlobalScreen.addNativeMouseListener(new NativeMouseAdapter() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
                super.nativeMouseClicked(nativeMouseEvent);
                Platform.runLater(() -> {
                    recordClick(ClickType.MOUSE);
                    updateView(ClickType.MOUSE);

                });

            }

            @Override
            public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
                super.nativeMousePressed(nativeMouseEvent);
            }

            @Override
            public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
                super.nativeMouseReleased(nativeMouseEvent);
            }
        });

        GlobalScreen.addNativeKeyListener(new NativeKeyAdapter() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
                super.nativeKeyTyped(nativeKeyEvent);
                log.info(String.valueOf(nativeKeyEvent.getKeyChar()));
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
                super.nativeKeyReleased(nativeKeyEvent);
                Platform.runLater(() -> {
                    recordClick(ClickType.KEY);
                    updateView(ClickType.KEY);
                });
            }
        });

    }

    private void updateView(ClickType clickType) {
        LocalDateTime today = LocalDateTime.now();
        String hour = today.format(DateTimeFormatter.ofPattern("MM-dd HH"));
        String date = hour.substring(0, 6);
        if (clickType == ClickType.MOUSE) {
            recordMoueMap.put(hour, recordMoueMap.getOrDefault(hour, 0) + 1);
            updateBarChart(recordMoueMap, recordMoueList);
            mouseClickCount = 0;
            recordMoueMap.forEach((k, v) -> {
                if (k.substring(0, 6).equals(date)) {
                    mouseClickCount = mouseClickCount + v;
                }
            });
            clickMouseText.setText(String.format("* %d *", mouseClickCount));

        } else {
            recordKeyMap.put(hour, recordKeyMap.getOrDefault(hour, 0) + 1);
            updateBarChart(recordKeyMap, recordKeyList);
            keyClickCount = 0;
            recordKeyMap.forEach((k, v) -> {
                if (k.substring(0, 6).equals(date)) {
                    keyClickCount = keyClickCount + v;
                }
            });
            clickKeyboardText.setText(String.format("* %d *", keyClickCount));
        }
    }

    /**
     * 将点击次数信息记录到数据库中
     * @param clickType 点击类型  MOUSE, KEY
     */
    private void recordClick(ClickType clickType) {
        LocalDateTime today = LocalDateTime.now();
        String hour = today.format(DateTimeFormatter.ofPattern("MM-dd HH"));

        try {
            ClickStatistic data = ClickDBHelper.findByHourKey(hour);
            if (data != null) {
                log.info(String.valueOf(data));
                if (clickType == ClickType.KEY) {
                    data.setKeyClickCount(data.getKeyClickCount() + 1);
                } else {
                    data.setMouseClickCount(data.getMouseClickCount() + 1);
                }
                ClickDBHelper.update(data);
            } else {
                data = new ClickStatistic();
                data.setHourKey(hour);
                if (clickType == ClickType.KEY) {
                    data.setKeyClickCount(1);
                } else {
                    data.setMouseClickCount(1);
                }
                data.setCreateTime(new Timestamp(new Date().getTime()));
                ClickDBHelper.insert(data);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 构造柱状图，用于初始化图标
     */
    @SuppressWarnings("unchecked")
    public void populateBarChart() {
        // 创建并设置Y坐标轴
        XYChart.Series<String, Number> mouseSeries = new XYChart.Series<>(recordMoueList);
        XYChart.Series<String, Number> keySeries = new XYChart.Series<>(recordKeyList);

        mouseSeries.setName("鼠标点击次数");
        keySeries.setName("键盘点击次数");

        updateBarChart(recordMoueMap, recordMoueList);
        updateBarChart(recordKeyMap, recordKeyList);

        barChart.getData().addAll(mouseSeries, keySeries);
    }

    private void updateBarChart(TreeMap<String, Integer> record, ObservableList<XYChart.Data<String, Number>> dataList) {

        record.forEach((k, v) -> {
            boolean isModified = false;
            for (XYChart.Data<String, Number> stringNumberData : dataList) {
                if (stringNumberData.getXValue().equals(k)) {
                    stringNumberData.setYValue(v);
                    log.info("update:" + k + ":" + v);
                    isModified = true;
                    break;
                }
            }
            if (!isModified) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(k, v);
                dataList.add(data);
            }
        });
        for (XYChart.Data<String, Number> stringNumberData : dataList) {
            Tooltip tooltip = new Tooltip("点击次数：" + stringNumberData.getYValue());
            Tooltip.install(stringNumberData.getNode(), tooltip);
        }
    }
}