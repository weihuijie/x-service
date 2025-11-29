package com.x.offline.analysis.service.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;

public class JavaParquetReader {
    public static void main(String[] args) throws Exception {
        // 1. 配置 Hadoop
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000");

        // 2. 读取 Parquet 文件
        String hdfsPath = "/iotdb/reports/daily/2025-11-12/1764348073011/part-00000-cf6cc8d9-6198-4b3d-8874-2d3d58efa796-c000.snappy.parquet";
        HadoopInputFile inputFile = HadoopInputFile.fromPath(new Path(hdfsPath), conf);
        ParquetFileReader reader = ParquetFileReader.open(inputFile);

        // 3. 查看数据结构
        MessageType schema = reader.getFileMetaData().getSchema();
        System.out.println("数据结构：" + schema);

        // 4. 读取数据（需解析每行内容，略复杂，推荐用 Spark 更简洁）
        reader.close();
    }
}