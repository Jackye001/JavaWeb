package com.operatingSystem.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTPUtil {

    /**
     * 简单操作FTP工具类 ,此工具类支持中文文件名，不支持中文目录
     * 如果需要支持中文目录，需要 new String(path.getBytes("UTF-8"),"ISO-8859-1") 对目录进行转码
     */

        /**
         * 获取FTPClient对象
         * @param ftpHost 服务器IP
         * @param ftpPort 服务器端口号
         * @param ftpUserName 用户名
         * @param ftpPassword 密码
         * @return FTPClient
         */
        public static FTPClient getFTPClient(String ftpHost, int ftpPort, String ftpUserName, String ftpPassword) {

            FTPClient ftp = null;
            try {
                ftp = new FTPClient();
                // 连接FPT服务器,设置IP及端口
                ftp.connect(ftpHost, ftpPort);
                // 设置用户名和密码
                ftp.login(ftpUserName, ftpPassword);
                // 设置连接超时时间,5000毫秒
                ftp.setConnectTimeout(50000);
                // 设置中文编码集，防止中文乱码
                ftp.setControlEncoding("UTF-8");
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    System.out.println("未连接到FTP，用户名或密码错误");

                    ftp.disconnect();
                } else {
                    System.out.println("FTP连接成功");
                }

            } catch (SocketException e) {
                e.printStackTrace();
                System.out.println("FTP的IP地址可能错误，请正确配置");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("FTP的端口错误,请正确配置");
            }
            return ftp;
        }

        /**
         * 关闭FTP方法
         * @param ftp
         * @return
         */
        public static boolean closeFTP(FTPClient ftp){

            try {
                ftp.logout();
            } catch (Exception e) {
                System.out.println("FTP关闭失败");

            }finally{
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException ioe) {
                        System.out.println("FTP关闭失败");
                    }
                }
            }
            return false;
        }

        /**
         * 下载FTP下指定文件
         * @param ftp FTPClient对象
         * @param filePath FTP文件路径
         * @param downPath 下载保存的目录
         * @return
         */
        public static boolean downLoadFTP(FTPClient ftp, String filePath, String downPath) {
            // 默认失败
            boolean flag = false;
            try {
                // 跳转到文件目录
                ftp.changeWorkingDirectory(new String(filePath.getBytes("UTF-8"),"ISO-8859-1"));
                // 获取目录下文件集合
                ftp.enterLocalPassiveMode();
                FTPFile[] files = ftp.listFiles();
                System.out.println(files.length);
                for(FTPFile file:files){
                    System.out.println(file.getName());
                }

                for (FTPFile file : files)
                {
                    // 取得指定文件并下载
                    if (file.getName().endsWith("mp4")) {
                        File downFile = new File(downPath + File.separator
                                + file.getName());
                        System.out.println("Choosen: " + file.getName()+" "+ downFile.getName());
                        OutputStream out = new FileOutputStream(downFile);
                        // 绑定输出流下载文件,需要设置编码集，不然可能出现文件为空的情况
                        flag = ftp.retrieveFile(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"), out);
                        // 下载成功删除文件,看项目需求
                        // ftp.deleteFile(new String(fileName.getBytes("UTF-8"),"ISO-8859-1"));
                        out.flush();
                        out.close();
                        if(flag){
                            System.out.println("下载成功");
                        }else{
                            System.out.println("下载失败");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return flag;
        }

    /**
     * 检查文件
     * @param ftp
     * @param filePath
     */
        public static void checkFile(FTPClient ftp, String filePath) {
            try {
                // 跳转到文件目录
                ftp.changeWorkingDirectory(new String(filePath.getBytes("UTF-8"),"ISO-8859-1"));
                // 获取目录下文件集合
                ftp.enterLocalPassiveMode();
                FTPFile[] files = ftp.listFiles();
                System.out.println(files.length);
                for(FTPFile file:files){
                    System.out.println(file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * FTP文件上传工具类
         * @param ftp
         * @param filePath
         * @param ftpPath
         * @return
         */
        public static boolean uploadFile(FTPClient ftp,String filePath,String ftpPath){
            boolean flag = false;
            InputStream in = null;
            try {
                // 设置PassiveMode传输
                ftp.enterLocalPassiveMode();
                //设置二进制传输，使用BINARY_FILE_TYPE，ASC容易造成文件损坏
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                //判断FTP目标文件夹时候存在不存在则创建
                if(!ftp.changeWorkingDirectory(ftpPath)){
                    ftp.makeDirectory(ftpPath);
                }
                //跳转目标目录
                if(ftp.changeWorkingDirectory(ftpPath)){
                    System.out.println("Create FTP file sUccessfully");
                }
                else {
                    System.out.println("Create FTP file Fail");
                }

                //上传文件
                File file = new File(filePath);
                in = new FileInputStream(file);
                String tempName = ftpPath+"/"+file.getName();
                System.out.println(tempName);
                flag = ftp.storeFile(new String (tempName.getBytes("UTF-8"),"ISO-8859-1"),in);
                if(flag){
                    System.out.println("上传成功");
                }else{
                    System.out.println("上传失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("上传失败");
            }finally{
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return flag;
        }

        /**
         * FPT上文件的复制
         * @param ftp  FTPClient对象
         * @param olePath 原文件地址
         * @param newPath 新保存地址
         * @param fileName 文件名
         * @return
         */
        public boolean copyFile(FTPClient ftp, String olePath, String newPath,String fileName) {
            boolean flag = false;

            try {
                // 跳转到文件目录
                ftp.changeWorkingDirectory(olePath);
                //设置连接模式，不设置会获取为空
                ftp.enterLocalPassiveMode();
                // 获取目录下文件集合
                FTPFile[] files = ftp.listFiles();
                ByteArrayInputStream  in = null;
                ByteArrayOutputStream out = null;
                for (FTPFile file : files) {
                    // 取得指定文件并下载
                    if (file.getName().equals(fileName)) {

                        //读取文件，使用下载文件的方法把文件写入内存,绑定到out流上
                        out = new ByteArrayOutputStream();
                        ftp.retrieveFile(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"), out);
                        in = new ByteArrayInputStream(out.toByteArray());
                        //创建新目录
                        ftp.makeDirectory(newPath);
                        //文件复制，先读，再写
                        //二进制
                        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                        flag = ftp.storeFile(newPath+File.separator+(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1")),in);
                        out.flush();
                        out.close();
                        in.close();
                        if(flag){
                            System.out.println("转存成功");
                        }else{
                            System.out.println("复制失败");
                        }


                    }
                }
            } catch (Exception e) {
                System.out.println("复制失败");
            }
            return flag;
        }

        /**
         * 实现文件的移动，这里做的是一个文件夹下的所有内容移动到新的文件，
         * 如果要做指定文件移动，加个判断判断文件名
         * 如果不需要移动，只是需要文件重命名，可以使用ftp.rename(oleName,newName)
         * @param ftp
         * @param oldPath
         * @param newPath
         * @return
         */
        public boolean moveFile(FTPClient ftp,String oldPath,String newPath){
            boolean flag = false;

            try {
                ftp.changeWorkingDirectory(oldPath);
                ftp.enterLocalPassiveMode();
                //获取文件数组
                FTPFile[] files = ftp.listFiles();
                //新文件夹不存在则创建
                if(!ftp.changeWorkingDirectory(newPath)){
                    ftp.makeDirectory(newPath);
                }
                //回到原有工作目录
                ftp.changeWorkingDirectory(oldPath);
                for (FTPFile file : files) {

                    //转存目录
                    flag = ftp.rename(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"), newPath+File.separator+new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"));
                    if(flag){
                        System.out.println(file.getName()+"移动成功");
                    }else{
                        System.out.println(file.getName()+"移动失败");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("移动文件失败");
            }
            return flag;
        }

        /**
         * 删除FTP上指定文件夹下文件及其子文件方法，添加了对中文目录的支持
         * @param ftp FTPClient对象
         * @param FtpFolder 需要删除的文件夹
         * @return
         */
        public static boolean deleteByFolder(FTPClient ftp,String FtpFolder){
            boolean flag = false;
            try {
                ftp.changeWorkingDirectory(new String(FtpFolder.getBytes("UTF-8"),"ISO-8859-1"));
                ftp.enterLocalPassiveMode();
                FTPFile[] files = ftp.listFiles();
                for (FTPFile file : files) {
                    //判断为文件则删除
                    if(file.isFile()){
                        ftp.deleteFile(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"));
                    }
                    //判断是文件夹
//                    if(file.isDirectory()){
//                        String childPath = FtpFolder + File.separator+file.getName();
//                        //递归删除子文件夹
//                        deleteByFolder(ftp,childPath);
//                    }
                }
                //循环完成后删除文件夹
                flag = ftp.removeDirectory(new String(FtpFolder.getBytes("UTF-8"),"ISO-8859-1"));
                if(flag){
                    System.out.println(FtpFolder+"文件夹删除成功");
                }else{
                    System.out.println(FtpFolder+"文件夹删除成功");
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("删除失败");
            }
            return flag;

        }

        /**
         * 遍历解析文件夹下所有文件
         * @param folderPath 需要解析的的文件夹
         * @param ftp FTPClient对象
         * @return
         */
        public boolean readFileByFolder(FTPClient ftp,String folderPath){
            boolean flage = false;
            try {
                ftp.changeWorkingDirectory(new String(folderPath.getBytes("UTF-8"),"ISO-8859-1"));
                //设置FTP连接模式
                ftp.enterLocalPassiveMode();
                //获取指定目录下文件文件对象集合
                FTPFile files[] = ftp.listFiles();
                InputStream in = null;
                BufferedReader reader = null;
                for (FTPFile file : files) {
                    //判断为txt文件则解析
                    if(file.isFile()){
                        String fileName = file.getName();
                        if(fileName.endsWith(".txt")){
                            in = ftp.retrieveFileStream(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"));
                            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                            String temp;
                            StringBuffer buffer = new StringBuffer();
                            while((temp = reader.readLine())!=null){
                                buffer.append(temp);
                            }
                            if(reader!=null){
                                reader.close();
                            }
                            if(in!=null){
                                in.close();
                            }
                            //ftp.retrieveFileStream使用了流，需要释放一下，不然会返回空指针
                            ftp.completePendingCommand();
                            //这里就把一个txt文件完整解析成了个字符串，就可以调用实际需要操作的方法
                            System.out.println(buffer.toString());
                        }
                    }
                    //判断为文件夹，递归
                    if(file.isDirectory()){
                        String path = folderPath+File.separator+file.getName();
                        readFileByFolder(ftp, path);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("文件解析失败");
            }

            return flage;

        }

    /** 路径分隔符 */
    private static final String SEPARATOR_STR = "/";

    /** 点 */
    private static final String DOT_STR = ".";

    private static String userRootDir = "";

    /**
     * 下载文件,文件名encode编码
     *
     * 注:根据不同的(Server/Client)情况,这里灵活设置
     */
    private static String downfileNameEncodingParam1 = "UTF-8";

    /**
     * 下载文件,文件名decode编码
     *
     * 注:根据不同的(Server/Client)情况,这里灵活设置
     */
    private static String downfileNameDecodingParam2 = "ISO-8859-1";

    public static int downloadFile(FTPClient ftpClient ,String remoteDirOrRemoteFile, String localDir) throws IOException{
        remoteDirOrRemoteFile = handleRemoteDir(ftpClient,remoteDirOrRemoteFile);
        int successSum = 0;
        int failSum = 0;
        // 根据remoteDirOrRemoteFile是文件还是目录,来切换changeWorkingDirectory
        if (!remoteDirOrRemoteFile.contains(DOT_STR)) {
            // 切换至要下载的文件所在的目录,否者下载下来的文件大小为0
            boolean flag = ftpClient.changeWorkingDirectory(remoteDirOrRemoteFile);
            // 不排除那些 没有后缀名的文件 存在的可能;
            // 如果切换至该目录失败,那么其可能是没有后缀名的文件,那么尝试着下载该文件
            if (!flag) {
                return downloadNonsuffixFile(ftpClient, remoteDirOrRemoteFile, localDir);
            }
        } else {
            String tempWorkingDirectory;
            int index = remoteDirOrRemoteFile.lastIndexOf(SEPARATOR_STR);
            if (index > 0) {
                tempWorkingDirectory = remoteDirOrRemoteFile.substring(0, index);
            } else {
                tempWorkingDirectory = SEPARATOR_STR;
            }
            // 切换至要下载的文件所在的目录,否者下载下来的文件大小为0
            ftpClient.changeWorkingDirectory(tempWorkingDirectory);
        }
        File localFileDir = new File(localDir);
        // 获取remoteDirOrRemoteFile目录下所有 文件以及文件夹   或  获取指定的文件
        FTPFile[] ftpFiles = ftpClient.listFiles(remoteDirOrRemoteFile);
        for (FTPFile file : ftpFiles) {
            // 如果是文件夹,那么不下载 (因为:直接下载文件夹的话,是无效文件)
            if (file.isDirectory()) {
                continue;
            }
            //如果文件夹不存在则创建    
            if (!localFileDir.exists()) {
                boolean result = localFileDir.mkdirs();
                System.out.println("create file folder: " + result);
            }
            String name = new String(file.getName().getBytes(downfileNameEncodingParam1),
                    downfileNameDecodingParam2);
            String tempLocalFile = localDir.endsWith(SEPARATOR_STR) ?
                    localDir + name :
                    localDir + SEPARATOR_STR + name;
            File localFile = new File(tempLocalFile);
            try {
                OutputStream os = new FileOutputStream(localFile);
                boolean result = ftpClient.retrieveFile(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"), os);
//                os.flush();
//                os.close();
                if (result) {
                    successSum++;
                } else {
                    failSum++;
                }
            } catch (Exception e) {

            }
        }
        return successSum;
    }

    public static int recursiveDownloadFile(FTPClient ftpClient,String remoteDirOrRemoteFile, String localDir) throws IOException {
        remoteDirOrRemoteFile = handleRemoteDir(ftpClient, remoteDirOrRemoteFile);
        int successSum = 0;
        // remoteDirOrRemoteFile是一个明确的文件  还是  一个目录
        if (remoteDirOrRemoteFile.contains(DOT_STR)) {
            successSum = downloadFile(ftpClient, remoteDirOrRemoteFile, localDir);
        } else {
            /// 初步组装数据,调用递归方法;查询给定FTP目录以及其所有子孙目录,进而得到FTP目录与本地目录的对应关系Map
            // 有序存放FTP remote文件夹路径
            // 其实逻辑是:先往alreadyQueriedDirList里面存,再进行的查询。此处可以这么处理。
            List<String> alreadyQueryDirList = new ArrayList<>(30);
            alreadyQueryDirList.add(remoteDirOrRemoteFile);
            // 有序存放FTP remote文件夹路径
            List<String> requiredQueryDirList = new ArrayList<>(30);
            requiredQueryDirList.add(remoteDirOrRemoteFile);
            // 记录FTP目录与 本地目录对应关系
            Map<String, String> storeDataMap = new HashMap<>(30);
            storeDataMap.put(remoteDirOrRemoteFile, localDir);
            queryFTPAllChildrenDirectory(ftpClient, storeDataMap, alreadyQueryDirList, requiredQueryDirList);
            String tempPath;
            // 循环调用downloadFile()方法,进行嵌套下载
            for(String str : alreadyQueryDirList) {
                // 将FTP用户的pwd的绝对路径转换为，用户输入的路径(因为 downloadFile方法会将用户输入的路径转化为pwd路径)
                // 提示:用户刚登陆进FTP时，输入pwd,得到的不一定是"/"，有可能时FTP对应的Linux上的文件夹路径，
                //     这与FTP的设置有关，可详见《程序员成长笔记(四)》搭建FTP服务器相关章节
                tempPath = str.length() > userRootDir.length() ?
                        str.substring(userRootDir.length()) :
                        SEPARATOR_STR;
                int thiscount = downloadFile(ftpClient,tempPath, storeDataMap.get(str));
                successSum += thiscount;
            }
        }
        System.out.println(" FtpUtil -> recursiveDownloadFile(excluded created directories) "
                + " success download file total -> " + successSum);
        return successSum;
    }

    private static String handleRemoteDir(FTPClient client,String remoteDirOrFile) throws IOException {
        if(remoteDirOrFile == null
                || "".equals(remoteDirOrFile.trim())
                || SEPARATOR_STR.equals(remoteDirOrFile)) {
            remoteDirOrFile = userRootDir + SEPARATOR_STR;
        } else if(remoteDirOrFile.startsWith(SEPARATOR_STR)) {
            remoteDirOrFile = userRootDir + remoteDirOrFile;
        } else {
            remoteDirOrFile = userRootDir + SEPARATOR_STR + remoteDirOrFile;
        }
        return remoteDirOrFile;
    }


    private static int downloadNonsuffixFile(FTPClient ftpClient, String remoteDirOrFile, String localDir) throws IOException {
        int successSum = 0;
        int failSum = 0;
        File localFileDir = new File(localDir);
        String tempWorkingDirectory;
        String tempTargetFileName;
        int index = remoteDirOrFile.lastIndexOf(SEPARATOR_STR);
        tempTargetFileName = remoteDirOrFile.substring(index + 1);
        if(tempTargetFileName.length() > 0) {
            if (index > 0) {
                tempWorkingDirectory = remoteDirOrFile.substring(0, index);
            }else {
                tempWorkingDirectory = SEPARATOR_STR;
            }
            ftpClient.changeWorkingDirectory(tempWorkingDirectory);
            // 获取tempWorkingDirectory目录下所有 文件以及文件夹   或  获取指定的文件
            FTPFile[] ftpFiles = ftpClient.listFiles(tempWorkingDirectory);
            for(FTPFile file : ftpFiles){
                String name = new String(file.getName().getBytes(downfileNameEncodingParam1),
                        downfileNameDecodingParam2);
                // 如果不是目标文件,那么不下载
                if(!tempTargetFileName.equals(name)) {
                    continue;
                }
                //如果文件夹不存在则创建    
                if (!localFileDir.exists()) {
                    boolean result = localFileDir.mkdirs();
                }
                String tempLocalFile = localDir.endsWith(SEPARATOR_STR) ?
                        localDir + name :
                        localDir + SEPARATOR_STR + name;
                File localFile = new File(tempLocalFile);
                try (OutputStream os = new FileOutputStream(localFile)) {
                    boolean result = ftpClient.retrieveFile(file.getName(), os);
                    if (result) {
                        successSum++;
                    } else {
                        failSum++;
                    }
                }
            }
        }
        return successSum;
    }

    private static void queryFTPAllChildrenDirectory(FTPClient ftpClient,
                                                Map<String, String> storeDataMap,
                                              List<String> alreadyQueriedDirList,
                                              List<String> requiredQueryDirList) throws IOException {
        List<String> newRequiredQueryDirList = new ArrayList<>(16);
        if(requiredQueryDirList.size() == 0) {
            return;
        }
        for (String str : requiredQueryDirList) {
            String rootLocalDir = storeDataMap.get(str);
            // 获取rootRemoteDir目录下所有 文件以及文件夹(或  获取指定的文件)
            FTPFile[] ftpFiles = ftpClient.listFiles(str);
            for(FTPFile file : ftpFiles){
                if (file.isDirectory()) {
                    String tempName = file.getName();
                    String ftpChildrenDir = str.endsWith(SEPARATOR_STR) ?
                            str + tempName :
                            str + SEPARATOR_STR + tempName;
                    String localChildrenDir = rootLocalDir.endsWith(SEPARATOR_STR) ?
                            rootLocalDir + tempName :
                            rootLocalDir + SEPARATOR_STR + tempName;
                    alreadyQueriedDirList.add(ftpChildrenDir);
                    newRequiredQueryDirList.add(ftpChildrenDir);
                    storeDataMap.put(ftpChildrenDir, localChildrenDir);
                }
            }
        }
        queryFTPAllChildrenDirectory(ftpClient,storeDataMap, alreadyQueriedDirList, newRequiredQueryDirList);
    }
}




