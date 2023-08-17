package com.lightmatter.clickshow.autostart;

import java.io.File;

public class AutoStartControl {

    private ShortCut myShortCut;


    public AutoStartControl() {
        this.myShortCut = new ShortCut();
    }

    public boolean setAutoStart(boolean yesAutoStart) {
        String linkFileName = myShortCut.obtainShortCutFile();
        boolean result = false;
        if (linkFileName != null) {
            result = setAutoStart(yesAutoStart, linkFileName);
            System.out.println("setting auto Constants.IS_AUTO_START " + linkFileName + "\n" + "--" + result);
        }

        return result;
    }

    // 写入快捷方式 是否自启动，快捷方式的名称，注意后缀是lnk
    private boolean setAutoStart(boolean yesAutoStart, String lnk) {
        File f = new File(lnk);
        String lnkPath = f.getAbsolutePath();
        String startFolder = "";
        String osName = System.getProperty("os.name");
        System.out.println("---------------->" + osName);
        if (osName.equals("Windows 7")
                || osName.equals("Windows 8")
                || osName.equals("Windows 10")
                || osName.equals("Windows 11")
                || osName.equals("Windows Server 2012 R2")
                || osName.equals("Windows Server 2014 R2")
                || osName.equals("Windows Server 2016")) {
            startFolder = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        }
        if (osName.endsWith("Windows XP")) {
            startFolder = System.getProperty("user.home") + "\\「开始」菜单\\程序\\启动";
        }
        return setRunBySys(yesAutoStart, lnkPath, startFolder, lnk);
    }

    // 设置是否随系统启动
    private boolean setRunBySys(boolean b, String lnkPath, String startFolder, String lnk) {
        return b ? addRunBySys(lnkPath, startFolder, lnk) : deleteRunBySys(startFolder, lnk);
    }

    private boolean addRunBySys(String lnkPath, String startFolder, String lnk) {
        Runtime run = Runtime.getRuntime();
        File f = new File(lnk);
        File file = new File(startFolder + "\\" + f.getName());
        // 删除
        try {
            if (f.isHidden()) {
                // 取消隐藏
                Runtime.getRuntime().exec("attrib -H \"" + lnkPath + "\"");
            }
            if (!file.exists()) {
                run.exec("cmd /c copy " + formatPath(lnkPath) + " " + formatPath(startFolder));
            } else {
                System.out.println(" auto start by system already exists, no need reset");
            }
            // 延迟0.5秒防止复制需要时间
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean deleteRunBySys(String startFolder, String lnk) {
        Runtime run = Runtime.getRuntime();
        File f = new File(lnk);
        File file = new File(startFolder + "\\" + f.getName());
        // 删除
        if (file.exists()) {
            try {
                if (file.isHidden()) {
                    // 取消隐藏
                    Runtime.getRuntime().exec("attrib -H \"" + file.getAbsolutePath() + "\"");
                    Thread.sleep(500);
                }
                run.exec("cmd /c del " + formatPath(file.getAbsolutePath()));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    // 解决路径中空格问题
    private String formatPath(String path) {
        return path == null ? "" : path.replaceAll(" ", "\" \"");
    }

}
