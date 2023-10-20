package com.lightmatter.clickshow;

import com.lightmatter.clickshow.db.ClickDBHelper;
import com.lightmatter.clickshow.model.ClickStatistic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseAdapter;
import org.jnativehook.mouse.NativeMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class.getName());
    public static final String MM_DD_HH = "MM-dd HH";
    public Button more;


    private enum ClickType {
        MOUSE, KEY
    }

    private LocalDate yesterday;
    private int mouseClickCount, yesterdayMouseClickCount;

    private int keyClickCount, yesterdayKeyClickCount;

    private final TreeMap<String, Integer> recordMoueMap = new TreeMap<>();
    private final TreeMap<String, Integer> recordKeyMap = new TreeMap<>();

    private final ObservableList<XYChart.Data<String, Number>> recordMoueList = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<String, Number>> recordKeyList = FXCollections.observableArrayList();


    @FXML
    private Label clickKeyboardText, otherClickKeyboardText;

    @FXML
    private Label clickMouseText, otherClickMouseText;


    @FXML
    private BarChart<String, Number> barChart;
    private XYChart.Series<String, Number> mouseSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> keySeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        initRecordData();
        initTextView();
        initYesterdayDataView();

        populateBarChart();
        addListener();
    }

    private void initTextView() {
        Platform.runLater(() -> {
            clickMouseText.setText(String.format("* %d *", mouseClickCount));
            clickKeyboardText.setText(String.format("* %d *", keyClickCount));
            otherClickMouseText.setText(String.format(" %d ", yesterdayMouseClickCount));
            otherClickKeyboardText.setText(String.format(" %d ", yesterdayKeyClickCount));
        });
    }

    /**
     * 打开时，自动获取当天的点击次数信息，然后初始化一些数据
     */
    private void initRecordData() {
        // 获今天的点击数据
        List<ClickStatistic> todayData;
        try {
            todayData = ClickDBHelper.findByCreateTimeRange(Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)),
                    Timestamp.valueOf(LocalDate.now().atStartOfDay().plusDays(1)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        recordMoueMap.clear();
        recordKeyMap.clear();
        for (ClickStatistic todayDatum : todayData) {
            mouseClickCount += todayDatum.getMouseClickCount();
            keyClickCount += todayDatum.getKeyClickCount();
            recordMoueMap.put(todayDatum.getHourKey(), todayDatum.getMouseClickCount());
            recordKeyMap.put(todayDatum.getHourKey(), todayDatum.getKeyClickCount());
        }

    }

    private void initYesterdayDataView() {
        List<ClickStatistic> yesterdayData;
        yesterday = LocalDate.now().minusDays(1);
        yesterdayMouseClickCount = yesterdayKeyClickCount = 0;
        try {
            yesterdayData = ClickDBHelper.findByCreateTimeRange(
                    Timestamp.valueOf(LocalDate.now().atStartOfDay().minusDays(1)),
                    Timestamp.valueOf(LocalDate.now().atStartOfDay())
            );
        } catch (Exception e) {
            log.error("query data error" + e);
            e.printStackTrace();
            yesterday = LocalDate.now();
            return;
//            throw new RuntimeException(e);
        }

        for (ClickStatistic clickStatistic : yesterdayData) {
            yesterdayMouseClickCount += clickStatistic.getMouseClickCount();
            yesterdayKeyClickCount += clickStatistic.getKeyClickCount();
        }
        Platform.runLater(() -> {
            otherClickMouseText.setText(String.format(" %d ", yesterdayMouseClickCount));
            otherClickKeyboardText.setText(String.format(" %d ", yesterdayKeyClickCount));
        });
    }

    private void addListener() {
        GlobalScreen.addNativeMouseListener(new NativeMouseAdapter() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
                super.nativeMouseClicked(nativeMouseEvent);
                recordClick(ClickType.MOUSE);
                Platform.runLater(() -> {
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
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
                super.nativeKeyReleased(nativeKeyEvent);
                recordClick(ClickType.KEY);
                Platform.runLater(() -> {
                    updateView(ClickType.KEY);
                });
            }
        });
    }

    private void updateView(ClickType clickType) {
        LocalDateTime today = LocalDateTime.now();
        String hour = today.format(DateTimeFormatter.ofPattern(MM_DD_HH));
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

        if (!yesterday.equals(LocalDate.now().minusDays(1))) {
            initYesterdayDataView();
            initRecordData();
        }
    }


    /**
     * 将点击次数信息记录到数据库中
     *
     * @param clickType 点击类型  MOUSE, KEY
     */
    private void recordClick(ClickType clickType) {
        LocalDateTime today = LocalDateTime.now();
        String hour = today.format(DateTimeFormatter.ofPattern(MM_DD_HH));

        try {
            ClickStatistic data = ClickDBHelper.findByHourKey(hour);
            if (data != null) {
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


    public void goSettingView() {
        try {
            if (HelloApplication.scene2 == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("setting.fxml"));
                HelloApplication.scene2 = new Scene(fxmlLoader.load(), 800, 600);
            }
            HelloApplication.primaryStage.setScene(HelloApplication.scene2);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 构造柱状图，用于初始化图标
     */
    @SuppressWarnings("unchecked")
    public void populateBarChart() {

        mouseSeries.setName("鼠标点击次数");
        keySeries.setName("键盘点击次数");
        mouseSeries.setData(recordMoueList);
        keySeries.setData(recordKeyList);
        updateBarChart(recordKeyMap, recordKeyList);
        updateBarChart(recordMoueMap, recordMoueList);
        barChart.getData().addAll(mouseSeries, keySeries);

    }

    private void updateBarChart(TreeMap<String, Integer> record, ObservableList<XYChart.Data<String, Number>> recordList) {

        record.forEach((k, v) -> {
            boolean isNew = true;
            for (XYChart.Data<String, Number> data : recordList) {
                if (data.getXValue().equals(k)) {
                    if (data.getYValue().intValue() != v) {
                        data.setYValue(v);
                        ((Text) ((StackPane) data.getNode()).getChildren().get(0)).setText(v.toString());
                    }
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(k, v);
                StackPane stackPane = new StackPane();
                Text dataLabel = new Text(v.toString());
                stackPane.getChildren().add(dataLabel);
                data.setNode(stackPane);
                dataLabel.setTranslateY(-10);
                recordList.add(data);
            }
        });

        for (int i = recordList.size() - 1; i >= 0; i--) {
            XYChart.Data<String, Number> data = recordList.get(i);
            if (!record.containsKey(data.getXValue())) {
                recordList.remove(i);
            }
        }
        barChart.layout();
    }
}