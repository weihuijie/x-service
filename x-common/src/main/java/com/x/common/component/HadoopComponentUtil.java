package com.x.common.component;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hadoop组件工具类
 * 提供Hadoop HDFS操作常用方法的封装
 */
@Component
public class HadoopComponentUtil {
    
    private Configuration configuration;
    private FileSystem fileSystem;
    
    public HadoopComponentUtil() {
        this.configuration = new Configuration();
    }
    
    /**
     * 连接到HDFS
     * @param hdfsUri HDFS URI
     * @throws IOException IO异常
     * @throws URISyntaxException URI异常
     */
    public void connectToHDFS(String hdfsUri) throws IOException, URISyntaxException {
        if (fileSystem != null) {
            fileSystem.close();
        }
        
        fileSystem = FileSystem.get(new URI(hdfsUri), configuration);
    }
    
    /**
     * 上传文件到HDFS
     * @param localFilePath 本地文件路径
     * @param hdfsFilePath HDFS文件路径
     * @throws IOException IO异常
     */
    public void uploadFile(String localFilePath, String hdfsFilePath) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        Path localPath = new Path(localFilePath);
        Path hdfsPath = new Path(hdfsFilePath);
        fileSystem.copyFromLocalFile(localPath, hdfsPath);
    }
    
    /**
     * 从HDFS下载文件
     * @param hdfsFilePath HDFS文件路径
     * @param localFilePath 本地文件路径
     * @throws IOException IO异常
     */
    public void downloadFile(String hdfsFilePath, String localFilePath) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        Path hdfsPath = new Path(hdfsFilePath);
        Path localPath = new Path(localFilePath);
        fileSystem.copyToLocalFile(hdfsPath, localPath);
    }
    
    /**
     * 在HDFS上创建目录
     * @param hdfsDirPath HDFS目录路径
     * @throws IOException IO异常
     */
    public void createDirectory(String hdfsDirPath) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        Path path = new Path(hdfsDirPath);
        if (!fileSystem.exists(path)) {
            fileSystem.mkdirs(path);
        }
    }
    
    /**
     * 检查HDFS上文件或目录是否存在
     * @param hdfsPath HDFS路径
     * @return 是否存在
     * @throws IOException IO异常
     */
    public boolean exists(String hdfsPath) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        return fileSystem.exists(new Path(hdfsPath));
    }
    
    /**
     * 删除HDFS上的文件或目录
     * @param hdfsPath HDFS路径
     * @param recursive 是否递归删除
     * @throws IOException IO异常
     */
    public void delete(String hdfsPath, boolean recursive) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        fileSystem.delete(new Path(hdfsPath), recursive);
    }
    
    /**
     * 列出HDFS目录下的文件和子目录
     * @param hdfsDirPath HDFS目录路径
     * @return 文件状态数组
     * @throws IOException IO异常
     */
    public FileStatus[] listFiles(String hdfsDirPath) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        return fileSystem.listStatus(new Path(hdfsDirPath));
    }
    
    /**
     * 获取文件状态
     * @param hdfsPath HDFS路径
     * @return 文件状态
     * @throws IOException IO异常
     */
    public FileStatus getFileStatus(String hdfsPath) throws IOException {
        if (fileSystem == null) {
            throw new IllegalStateException("HDFS未连接");
        }
        
        return fileSystem.getFileStatus(new Path(hdfsPath));
    }
    
    /**
     * 关闭HDFS连接
     * @throws IOException IO异常
     */
    public void close() throws IOException {
        if (fileSystem != null) {
            fileSystem.close();
            fileSystem = null;
        }
    }
}