package com.lightmatter.clickshow;

import com.lightmatter.clickshow.autostart.AutoStartControl;
import com.lightmatter.clickshow.db.ClickDBHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelloApplication.class.getName());

    @Override
    public void start(Stage stage) {
        log.info("----------start-------" + new Date());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            stage.setScene(scene);
            stage.setTitle("我的今日点击战绩 v 1.1");
            stage.getIcons().add(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("/images/c_128.png"))));
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }


        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(event -> {
            log.info("Minimize the window " + event.getEventType());
            event.consume();
            stage.setIconified(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() throws Exception {
        super.init();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            log.error(ex.getMessage(),ex);
            System.exit(1);
        }
        // 初始化数据表
        ClickDBHelper.createTable();
        // 设置自启动
        new Thread(() -> new AutoStartControl().setAutoStart(true)).start();

    }

// 开启这个，点叉号，可以关闭，否则不关闭。
//    @Override
//    public void stop() throws Exception {
//        super.stop();
//        log.info("---------good bye--------");
//        System.exit(1);
//    }
}