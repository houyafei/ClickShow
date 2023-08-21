package com.lightmatter.clickshow.autostart;

import createshortcut.CreateShortCut;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

public class ShortCut {
    private static File targetFile = null;
    private static Logger logger = Logger.getLogger(ShortCut.class.getPackage().getName());

    /**
     * 创建快捷键
     *
     * @return 返回创建的快捷键的全路径
     */
    public String obtainShortCutFile() {
        String exeFileName = "ClickShow.exe";
        File file = new File(".");
        logger.info(file.getAbsolutePath());
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
            logger.info("not found the exe file " + exeFileName);
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
            logger.info(String.format("match file %s is %s ", fileName, file.getAbsolutePath()));
            targetFile = file;
        } else if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                getFile(listFile, fileName);
            }
        }

    }

}
