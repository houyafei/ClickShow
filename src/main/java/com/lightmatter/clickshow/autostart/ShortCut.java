package com.lightmatter.clickshow.autostart;

import createshortcut.CreateShortCut;

import java.io.File;
import java.util.Objects;

public class ShortCut {
    private static File targetFile = null;

    public String obtainShortCutFile() {
        String exeFileName = "ClickShow*.exe";
        File file = new File("../../");
        System.out.println(file.getAbsolutePath());
        getFile(file, exeFileName);
        boolean isSuccess = false;
        if (targetFile != null) {
            isSuccess = CreateShortCut.createLnk(targetFile.getParentFile().getAbsolutePath(),
                    targetFile.getName().substring(0, targetFile.getName().indexOf(".")),
                    targetFile.getAbsolutePath());
        } else {
            System.out.println("not found the exe file " + exeFileName);
        }
        return isSuccess ? targetFile.getParentFile().getAbsolutePath()
                + "\\"
                + targetFile.getName().substring(0, targetFile.getName().indexOf("."))
                + ".lnk" : null;
    }

    private static void getFile(File file, String fileName) {
        if (file.getName().matches(fileName)) {
            System.out.println(file.getAbsolutePath());
            targetFile = file;
        } else if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                getFile(listFile, fileName);
            }
        }

    }

}
