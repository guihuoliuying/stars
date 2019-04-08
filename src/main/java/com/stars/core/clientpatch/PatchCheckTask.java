package com.stars.core.clientpatch;

import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by zhaowenshuo on 2017/4/25.
 */
public class PatchCheckTask implements Runnable {
    @Override
    public void run() {
        try {
            File file = new File("./clientpatch/configpatcher.txt");
            if (file.exists() && file.isFile()) {
                Path path = Paths.get(file.getPath());
                long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
                if (lastModifiedTime > PatchManager.lastModifiedTime) {
                    //
                    StringBuilder sb = new StringBuilder(4096);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        String patch = sb.toString();
                        if (patch.getBytes("UTF-8").length < Short.MAX_VALUE) {
                            PatchManager.patch = patch;
                            PatchManager.needPatch = true;
                            PatchManager.lastModifiedTime = lastModifiedTime;
                            LogUtil.info("PatchManager|检测客户端数据补丁更新|lastModifiedTime:{}|text:{}",
                                    lastModifiedTime, patch);
                            notifyPlayer();
                        } else {
                            LogUtil.error("数据补丁过大");
                        }
                    } catch (Throwable cause) {
                        LogUtil.error("", cause);
                    }
                }
            }
        } catch (Throwable cause) {
            LogUtil.error("", cause);
        }
    }

    private void notifyPlayer() {
        ServiceHelper.roleService().noticeAll(new ClientPatchEvent());
    }
}
