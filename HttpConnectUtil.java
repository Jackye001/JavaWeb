package com.operatingSystem.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class HttpConnectUtil {

    /**
     * 与服务器建立连接
     * @param reqUrl
     * @param reqMethod
     * @return map
     * @throws Exception
     */
    public static Map<String, String> ManageConnection(String reqUrl, String reqMethod , String messageXML) throws Exception {
        System.out.println(messageXML);
        URL url;
        String xml =messageXML;
        byte[] data = xml.getBytes("UTF-8");//得到了xml的实体数据
        url = new URL(reqUrl);
        MessageUtil messageUtil =new MessageUtil();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setConnectTimeout(20* 1000);
        con.setReadTimeout(20* 1000);
        con.setRequestProperty("Cache-Control", "no-cache");
        con.setRequestProperty("Content-Type", "application/xml");
        con.setRequestProperty("Content-Length", String.valueOf(data.length));
        con.setRequestProperty("Charset", "UTF-8");
        if (null != reqMethod && !reqMethod.equals("")) {
            con.setRequestMethod(reqMethod);
        } else {
            con.setRequestMethod("GET");
        }

        //发送消息
        if (!messageXML.isEmpty())
        {
            OutputStream outStream = con.getOutputStream();

            outStream.write(data);

            outStream.flush();

            outStream.close();

            System.out.println("消息已经发送");
        }

        System.out.println("con.getResponseCode()"+con.getResponseCode());
       //接受消息
        if (con.getResponseCode() == 200) {

            InputStream responseStream = con.getInputStream();

            System.out.println("消息获取到了");

            return messageUtil.parseXml(responseStream);

        }
        else
        {
            InputStream responseStream = con.getErrorStream();

            System.out.println("wrong消息获取到了");

            return messageUtil.parseXml(responseStream);
        }


    }

}



