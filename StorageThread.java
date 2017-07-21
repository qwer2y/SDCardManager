package com.example.TestTaxi.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Huyao on 2017/7/20.
 * 内存卡容量检查线程
 */

public class StorageThread extends Thread {

    private String inStoragePath; //内置存储
    private String sdCardPath; //外置SD卡路径
    private String deletePathName; //指定删除的文件夹名字
    private int freeMinSize; //指定最小可用容量
    private List<String> matchList;
    private List<String> taFileList;
    private int tag = 0;


    public StorageThread() {
    }

    /**
     * 可指定从SD卡目录zhhc文件夹下某个文件夹开始删除，指定最小剩余SD卡容量
     * 如不指定，默认为video文件夹，最小SD卡容量默认为500M
     *
     * @param deletePathName 文件名
     * @param freeMinSize    剩余SD卡最小容量 以MB为单位
     */
    public StorageThread(String deletePathName, int freeMinSize) {
        this.deletePathName = deletePathName;
        this.freeMinSize = freeMinSize;
    }

    @Override
    public void run() {
        super.run();
        synchronized (StorageThread.class) {
            matchList = new ArrayList<>();
            List<String> storagePaths = SDCardUtils.getExtSDCardPathList();
            int storagePathSize = storagePaths.size();
            if (deletePathName.isEmpty()
                    || !deletePathName.toLowerCase().equals("video")
                    && !deletePathName.toLowerCase().equals("photo")
                    && !deletePathName.toLowerCase().equals("voice")
                    ) {
                deletePathName = "video";
            }
            if (freeMinSize == 0) {
                freeMinSize = 500;
            }
            if (!SDCardUtils.isSDCardEnable()) {
                SDCardUtils.log("内置存储不可用");
            }
            if (storagePathSize < 2) {
                SDCardUtils.log("外置SD卡不可用");
            } else {
                sdCardPath = storagePaths.get(1);
                File[] rootFiles = sortFileByDate("");
                File[] taFilesArray = sortFileByDate(deletePathName);
                taFileList = new ArrayList<>();
                if (taFilesArray != null && rootFiles != null) {
                    if (isMemoryTension()) {
                        for (File f : rootFiles) {
                            taFileList.add(f.getName());
                        }
                        addWithOrder(deletePathName);
                        if (matchList.contains(deletePathName)) {
                            SDCardUtils.log("开始清理...");
                            for (File f : taFilesArray) {
                                if (isMemoryTension()) {
                                    f.delete();
                                    if(f.exists()){
                                        SDCardUtils.log("文件"+f.getName()+"被占用,删除失败");
                                    }
                                }
                            }
                            SDCardUtils.log("文件夹" + deletePathName + "删除完毕");
                            matchList.remove(deletePathName);
                            for (int i = matchList.size(); i > 0; i--) {
                                tag += 1;
                                if (matchList.size() > 0 && isMemoryTension()) {
                                    deletePathName = String.valueOf(matchList.get(tag));
                                    taFilesArray = sortFileByDate(deletePathName);
                                    if (taFilesArray != null){
                                        for (File file : taFilesArray) {
                                            if (isMemoryTension()) {
                                                file.delete();
                                                if(file.exists()){
                                                    SDCardUtils.log("文件"+file.getName()+"被占用,删除失败");
                                                }
                                            }
                                        }
                                    }
                                    if (matchList.size() > 0) {
                                        matchList.remove(deletePathName);
                                    }
                                }
                            }
                            SDCardUtils.log("文件夹" + deletePathName + "删除完毕");
                            if (isMemoryTension()) {
                                SDCardUtils.log("数据清理完毕但SD卡容量仍小于指定容量！");
                            } else {
                                SDCardUtils.log("SD卡清理完毕");
                            }
                        } else {
                            SDCardUtils.log("SD卡中没有找到对应文件！");
                        }
                    } else {
                        SDCardUtils.log("SD卡内存充足");
                    }
                } else {
                    SDCardUtils.log("SD卡上没有找到对应的二级文件夹");
                }
            }
        }
    }


    /**
     * 检查内存容量是否足够
     *
     * @return true 内存不够 false 内存够
     */
    private boolean isMemoryTension() {
        SDCardUtils.log("SD卡剩余存储空间为 : " + SDCardUtils.getFreeSpace(sdCardPath) + " MB" + " 最小可用容量为: " + freeMinSize + " MB");
        return SDCardUtils.getFreeSpace(sdCardPath) < freeMinSize;
    }


    /**
     * 根据文件路径遍历路径下所有文件按照最后修改时间进行排序
     *
     * @param filePathName 文件名
     * @return 排序好的file数组
     */

    private File[] sortFileByDate(String filePathName) {
        String deleteFilePath = sdCardPath + "/zhhc/" + filePathName;
        try {
            Runtime.getRuntime().exec("chmod 777 " + deleteFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File(deleteFilePath);
        if (!file.isDirectory()) {
            return null;
        }
        File[] fs = file.listFiles();
        try {
            Arrays.sort(fs, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }

                public boolean equals(Object obj) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fs;
    }

    /**
     * 按指定删除顺序添加
     *
     * @param name
     */
    private void addWithOrder(String name) {
        switch (name) {
            case "video":
                if (taFileList.contains("video")) {
                    matchList.add("video");
                }
                if (taFileList.contains("photo")) {
                    matchList.add("photo");
                }
                if (taFileList.contains("voice")) {
                    matchList.add("voice");
                }
                break;
            case "photo":
                if (taFileList.contains("photo")) {
                    matchList.add("photo");
                }
                if (taFileList.contains("voice")) {
                    matchList.add("voice");
                }
                if (taFileList.contains("video")) {
                    matchList.add("video");
                }
                break;
            case "voice":
                if (taFileList.contains("voice")) {
                    matchList.add("voice");
                }
                if (taFileList.contains("photo")) {
                    matchList.add("photo");
                }
                if (taFileList.contains("video")) {
                    matchList.add("video");
                }
                break;
        }
    }
}
