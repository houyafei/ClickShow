package com.lightmatter.clickshow.autostart;

import createshortcut.CreateShortCut;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

public class ShortCut {
    private static File targetFile = null;
    private static Logger log = LogManager.getLogger(ShortCut.class.getName());

    /**
     * 创建快捷键
     *
     * @return 返回创建的快捷键的全路径
     */
    public String obtainShortCutFile() {
        String exeFileName = "ClickShow.exe";
        File file = new File(".");
        log.info(file.getAbsolutePath());
        getFile(file, exeFileName);
        boolean isSuccess = false;
        String lnkPath = null;
        if (targetFile != null) {
            lnkPath = targetFile.getParentFile().getAbsolutePath()
                    + "\\"
                    + targetFile.getName().substring(0, targetFile.getName().indexOf("."))
                    + ".lnk";
            if (!Paths.get(lnkPath).toFile().exists()) {
                isSuccess = CreateShortCut.createLnk(targetFile.getParentFile().getAbsolutePath(),
                        targetFile.getName().substring(0, targetFile.getName().indexOf(".")),
                        targetFile.getAbsolutePath());
                lnkPath = isSuccess ? lnkPath : null;
            }
        } else {
            log.warn("not found the exe file " + exeFileName);
        }
        return lnkPath;
    }

    /**
     * 查找可执行文件的全路径
     *
     * @param file     指定搜索范围
     * @param fileName 目标文件名称
     */
    private static void getFile(File file, String fileName) {
        if (file.getName().matches(fileName)) {
            log.info(String.format("match file %s is %s ", fileName, file.getAbsolutePath()));
            targetFile = file;
        } else if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                getFile(listFile, fileName);
            }
        }

    }

}
