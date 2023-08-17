package com.lightmatter.clickshow.autostart;

import createshortcut.CreateShortCut;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public class ShortCut {
    private static File targetFile = null;
    private static Logger logger = Logger.getLogger(ShortCut.class.getPackage().getName());

    public String obtainShortCutFile() {
        String exeFileName = "ClickShow.exe";
        File file = new File(".");
        logger.info(file.getAbsolutePath());
        getFile(file, exeFileName);
        boolean isSuccess = false;
        if (targetFile != null) {
            isSuccess = CreateShortCut.createLnk(targetFile.getParentFile().getAbsolutePath(),
                    targetFile.getName().substring(0, targetFile.getName().indexOf(".")),
                    targetFile.getAbsolutePath());
        } else {
            logger.info("not found the exe file " + exeFileName);
        }
        return isSuccess ? targetFile.getParentFile().getAbsolutePath()
                + "\\"
                + targetFile.getName().substring(0, targetFile.getName().indexOf("."))
                + ".lnk" : null;
    }

    private static void getFile(File file, String fileName) {
        if (file.getName().matches(fileName)) {
            logger.info(String.format("match file %s is %s ",fileName,file.getAbsolutePath()));
            targetFile = file;
        } else if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                getFile(listFile, fileName);
            }
        }

    }

}
