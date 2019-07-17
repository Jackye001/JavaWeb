package com.operatingSystem.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MessageUtil {


    public static Map<String, String> xmlmap = new HashMap<String,String>();

    //存储xml元素信息的容器
    private static List<String> elemList = new ArrayList<String>();

    public static List<String> getElemList() {
        return elemList;
    }

    public static void setElemList(List<String> elemList) {
        MessageUtil.elemList = elemList;
    }



    /**
     * 解析服务器发来的请求（XML）
     *
     * @param responseStream
     * @return map
     * @throws Exception
     */
    public  Map<String, String> parseXml(InputStream responseStream) throws Exception {
        // 将解析结果清理一下
          xmlmap.clear();
          elemList.clear();

        // 从request中取得输入流
        InputStream inputStream = responseStream;
        System.out.println("获取输入流");
       // printXml(inputStream);

        String a = null;
        byte[] data1 = new byte[inputStream.available()];
        inputStream.read(data1);
        // 转成字符串
        a = new String(data1);
        System.out.println(a);

        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(new ByteArrayInputStream(a.getBytes("utf-8")));

        // 得到xml根元素
        Element root = document.getRootElement();
        xmlmap.put("root",root.getName());                                                        //判断responsestatus
        // 得到根元素的所有子节点
        getElementList(root);
        String x = getListString(elemList);
        System.out.println("-----------elemList解析结果------------");
        System.out.println(x);
       /* System.out.println("-----------xmlmap解析结果------------");
        System.out.println(xmlmap);*/

        // 释放资源
        inputStream.close();
        inputStream = null;
        return xmlmap;
    }


    /**
     * 递归遍历方法
     *
     * @param element
     */
    public void getElementList(Element element) {
        List elements = element.elements();
        if (elements.size() == 0) {
            //没有子元素
            String xpath = element.getPath();
            //根节点内文本
            String value = element.getTextTrim();
            elemList.add(xpath + " " + value);
        } else {
            //有子元素
            // 使用方法iterator()要求容器返回一个Iterator,elements实质上是一个list
            for (Iterator it = elements.iterator(); it.hasNext(); ) {
                //第一次调用Iterator的next()方法时，它返回序列的第一个元素
                Element elem = (Element) it.next();
                //递归遍历|相同key值的value存储一起
                if (xmlmap.containsKey(elem.getName())){

                    String oldValue =xmlmap.get(elem.getName());
                    xmlmap.put(elem.getName(), oldValue+"|"+elem.getText());

                }
                else { xmlmap.put(elem.getName(), elem.getText());}

                //自己把自己当做根节点递归
                getElementList(elem);
            }
        }
    }

    public String getListString(List<String> elemList) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> it = elemList.iterator(); it.hasNext(); ) {
            String str = it.next();
            sb.append(str + "\n");
        }
        return sb.toString();
    }

    private void printXml(InputStream in) throws Exception {

        String a = null;
        byte[] data1 = new byte[in.available()];
        in.read(data1);
        // 转成字符串
        a = new String(data1);
        System.out.println(a);

    }


}
