package org.foree.duker.utils;

import android.util.Log;

import org.foree.duker.base.MyApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by foree on 16-7-25.
 * 文件读写工具类
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    //读取文件
    //TODO 优化文件的读写操作流程
    public static String readFile(File file){
        if( file.exists()) {
            Log.d(TAG, "readFile: " + file.toString());
            FileReader in;
            BufferedReader bufferedReader;
            StringBuffer stringBuffer = new StringBuffer();
            String temp;
            try {
                in = new FileReader(file);
                bufferedReader = new BufferedReader(in);
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuffer.append(temp).append("\r\n");
                }

                bufferedReader.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuffer.toString();
        }else{
            Log.e(TAG, file + "not exits");
            return "";
        }
    }

    //写入文件
    public static void writeFile(File file, String string){
        FileWriter out;
        try {
            out = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(out);
            bufferedWriter.write(string);
            bufferedWriter.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 写入指定文件到data目录
    public static void writeToDataDir(String fileName, String content) {
        File file =  new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + fileName);
        writeFile(file, content);
    }

    // 追加文件
    public static void appendFile(File file, String string){
        FileWriter out;
        try {
            out = new FileWriter(file,true);
            BufferedWriter bufferedWriter = new BufferedWriter(out);
            bufferedWriter.write(string);
            bufferedWriter.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // url encode
    public static String encodeUrl(String url){
        return url.replaceAll("/", "_");
    }
}
