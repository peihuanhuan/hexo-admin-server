package net.peihuan.newblog.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class CmdUtil {
    public static void excuterBash(String[] cmd) throws Exception {
        InputStream in = null;
        try {
            Process pro = Runtime.getRuntime().exec(cmd);
            pro.waitFor();
            in = pro.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String result = read.readLine();
            log.info("INFO:" + result);
        } catch (Exception ex) {
            log.error("______________ 执行bash脚本出错");
            throw ex;
        }
    }

    public static void excuterBashs(String[] cmd) {
        Runtime run = Runtime.getRuntime();
        File wd = new File("/bin");
        System.out.println(wd);
        Process proc = null;
        try {
            proc = run.exec("/bin/bash", null, wd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (proc != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for(String s : cmd){
                out.println(s);
            }
            out.println("exit");//这个命令必须执行，否则in流不结束。
            try {
                String line;
                while ((line = in.readLine()) != null) {
//                    System.out.println(line);
                    log.info(line);
                }
                proc.waitFor();
                in.close();
                out.close();
                proc.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
