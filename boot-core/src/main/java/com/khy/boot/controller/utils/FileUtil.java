package com.khy.boot.controller.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述：文件工具类
 *
 * @author kehaiyong
 * @date 2022-1-10 17:07
 * @modified By
 */
public class FileUtil {
    /**
     * 读取文件，用于文件回显到页面
     * @param url   文件路径 + 文件名
     * @return  string 属性
     */
    public static String readFile(String url) {
        BufferedReader br = null;
        String file = "";
        try {
            br = new BufferedReader(new FileReader(url));    // 读取文件

            String line = null;
            while((line = br.readLine()) != null) {    // 按行读取
                if(StringUtils.isNotBlank(line)) {
                    file += line +",";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }


    /**
     * 删除文件
     * @param url       文件路径 + 文件名
     * @param content   删除的内容用 ; 隔开
     */
    public static void removeFile(String url, String content) {
        String s = readFile(url);   // 读取文件

        String[] split = content.split(";");    // 删除的内容
        Map<String, String> map = new HashMap<>();
        for(String sp: split) {
            String[] split1 = sp.split("=");
            map.put(split1[0], split1[1]);
        }

        String[] string = s.split(";");     // 原文件内容
        String write = "";  // 写入文件的新数组
        for(String str: string) {
            if(str.contains("#")) { // 过滤注释
                write += str +";";
            }else {
                String[] split1 = str.split("=");
                String s1 = map.get(split1[0]);

                String untrue = map.get("untrue");  // 属性值[mysqld] || [client]

                if(StringUtils.isNotBlank(untrue)) {
                    if(untrue.equals(split1[split1.length - 1])) {
                        map.keySet().removeIf(key -> key.startsWith("untrue"));   // 删除已经赋值元素
                    }else {
                        if(StringUtils.isBlank(s1)) {    // map没有这个属性，不删除
                            write += str +";";
                        }else {
                            map.keySet().removeIf(key -> key.startsWith(split1[0]));   // 删除已经赋值元素
                        }
                    }

                }else {
                    if(StringUtils.isBlank(s1)) {    // map没有这个属性，不删除
                        write += str +";";
                    }else {
                        map.keySet().removeIf(key -> key.startsWith(split1[0]));   // 删除已经赋值元素
                    }
                }
            }
        }

        String property = System.getProperty("line.separator"); // 针对于不同性质的操作系统的换行

        BufferedWriter o = null;  // 写入删除后内容
        try {
            o = new BufferedWriter(new FileWriter(url));
            String[] split1 = write.split(";");

            for(String spl: split1) { // 更新文件
                o.write(spl + property);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if(o != null) {
                try {
                    o.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 更新文件
     * @param url   文件路径，用 / 结尾
     * @param file  文件名
     * @param content   修改追加的文件内容，或者修改的文件内容，用 ; 分割
     */
    public static void updateFile(String url, String file, String content) {

        BufferedReader br = null;
        BufferedWriter out = null;
        BufferedWriter o = null;

        String property = System.getProperty("line.separator"); // 针对于不同性质的操作系统的换行

        try {
            br = new BufferedReader(new FileReader(url + file));    // 读取文件

            File f = new File(url +"copy_"+ file);  // 备份文件
            if(!f.exists()) {
                f.createNewFile(); // 创建文件
            }

            out = new BufferedWriter(new FileWriter(url +"copy_"+ file));   // 备份文件写入

            String[] split = content.split(";");    // 处理需要写入的新数据
            Map<String, String> map = new HashMap<>();  // 保存新数据
            for(String s: split) {
                String[] strings = s.split("=");
                map.put(strings[0], strings[1]);
            }

            String line = null;
            String write = "";
            while ((line = br.readLine()) != null) {    // 按行读取
                if(StringUtils.isNotBlank(line)) {
                    out.write(line + property); // 写入备份文件，换行写入

                    if(line.contains("#")) {    // # 开头是注释 [] 是标注，原样保存
                        write += line + ";";

                    }else { // 根据输入的内容，原本存在的属性，修改；原本没有的属性，追加
                        String[] strings = line.split("="); // 前面是属性，后面是数值，原值
                        String s = map.get(strings[0]); // 根据key获取新赋值数值
                        String untrue = map.get("untrue");  // 属性值[mysqld] || [client]

                        if(StringUtils.isNotBlank(untrue)) {
                            if(untrue.equals(strings[strings.length - 1])) {    // 属性值存在，不操作
                                write += line +";";

                                map.keySet().removeIf(key -> key.startsWith("untrue"));   // 删除已经赋值元素
                            }else {
                                if(StringUtils.isNotBlank(s)) {     // 更改的属性
                                    write += strings[0] +"="+ s +";";

                                    map.keySet().removeIf(key -> key.startsWith(strings[0]));   // 删除已经赋值元素
                                }else { // 新增没有此属性，原值保存
                                    write += line +";";
                                }
                            }
                        }else {
                            if(StringUtils.isNotBlank(s)) {     // 更改的属性
                                write += strings[0] +"="+ s +";";

                                map.keySet().removeIf(key -> key.startsWith(strings[0]));   // 删除已经赋值元素
                            }else { // 新增没有此属性，原值保存
                                write += line +";";
                            }
                        }
                    }
                }
            }

            for(Map.Entry<String, String> m : map.entrySet()) {    // 新增的属性
                if(m.getKey().equals("untrue")) {   // 用于只有一个数值，没有key的属性
                    write += m.getValue() +";";
                }else {
                    write += m.getKey() +"="+ m.getValue() +";";
                }
            }

            o = new BufferedWriter(new FileWriter(url + file));  // 原文件追加或修改属性

            String[] split1 = write.split(";");
            for(String s: split1) { // 更新文件
                o.write(s + property);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(o != null) {
                try {
                    o.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        String s = FileUtil.readFile("D:\\企业id.txt");
        String[] split = s.split(",");
        System.out.println("数量:"+split.length);
        System.out.println(s);
    }
}

