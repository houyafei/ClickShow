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
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            stage.setScene(scene);
            stage.setTitle("我的今日点击战绩 v 1.0");
            stage.getIcons().add(new Image(HelloApplication.class.getResource("/images/c_128.png").toExternalForm()));
        } catch (IOException e) {
            System.out.println("----------start--" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() throws Exception {
        super.init();
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        // 初始化数据表
        ClickDBHelper.createTable();
        // 设置自启动
        new Thread(() -> {
            new AutoStartControl().setAutoStart(true);
        }).start();


    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("---------good bye--------");
        System.exit(1);
    }
}