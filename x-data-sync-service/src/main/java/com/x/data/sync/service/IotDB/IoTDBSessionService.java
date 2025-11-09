//package com.x.data.sync.service.IotDB;
//
//import jakarta.annotation.PreDestroy;
//import org.apache.iotdb.isession.SessionDataSet;
//import org.apache.iotdb.session.Session;
//import org.apache.tsfile.enums.TSDataType;
//import org.apache.tsfile.file.metadata.enums.TSEncoding;
//import org.apache.tsfile.write.record.Tablet;
//import org.apache.tsfile.write.schema.MeasurementSchema;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//
//public class IoTDBSessionService {
//
//         private Session session;
//
//         // 初始化Session连接
//
//         public void initSession() throws Exception {
//
//             session = new Session("localhost", 6667, "root", "root");
//
//             session.open();
//
//             // 设置批量写入参数
//             session.setFetchSize(5000);
//
//}
//
//	 // 批量写入数据（Session API 核心优势：低开销、高吞吐量）
//
//public void batchInsertWithSession (List timestamps, List temperatures, List humidities) throws Exception {
//
//	 // 1. 定义时间序列路径（格式：root. 存储组。设备。测量值）
//
//	 String deviceId = "root.sensor.demo";
//
//	 // 2. 定义测量值 schema（数据类型、编码方式）
//
//	 List measurements = new ArrayList<>();
//
//	 List schemas = new ArrayList<>();
//
//	 // 添加温度测量值（FLOAT 类型，RLE 编码适合连续数值）
//
//	 measurements.add ("temperature");
//
//	 schemas.add (new MeasurementSchema("temperature", TSDataType.FLOAT, TSEncoding.RLE));
//
//	 // 添加湿度测量值（INT32 类型，PLAIN 编码适合离散数值）
//
//	 measurements.add ("humidity");
//
//	 schemas.add (new MeasurementSchema ("humidity", TSDataType.INT32, TSEncoding.PLAIN));
//
//	 // 3. 创建 Tablet（IoTDB 批量数据载体，比 JDBC Batch 更高效）
//
//	 Tablet tablet = new Tablet (deviceId, schemas, timestamps.size ());
//
//	 // 4. 填充数据（按列填充，减少 IO 开销）
//
//	 int row = 0;
//
//	 for (Object timestamp : timestamps) {
//
//	 // 设置时间戳
//
//	 tablet.addTimestamp (row, (Long) timestamp);
//
//	 // 设置温度值（第 0 列）
//
//	 tablet.addValue ((String) measurements.get (0), row, temperatures.get (row));
//
//	 // 设置湿度值（第 1 列）
//
//	 tablet.addValue ((String) measurements.get (1), row, humidities.get (row));
//
//	 row++;
//
//	 }
//
//	 // 5. 执行批量写入（Session 会自动处理连接复用和数据分片）
//
//	 session.insertTablet (tablet);
//
//	 System.out.printf ("Session API 批量写入成功，共 % d 条数据", timestamps.size ());
//
//	 }
//
//	 // 时间范围查询（Session API 支持更灵活的结果格式）
//
//	 public List<Map<String, Object>> queryWithSession (long startTime, long endTime) throws Exception {
//
//	 List<Map<String, Object>> resultList = new ArrayList<>();
//
//	 // 1. 定义查询参数
//
//	 String deviceId = "root.sensor.demo";
//
//	 List measurements = Arrays.asList ("temperature", "humidity");
//
//	 // 2. 执行查询（返回 ResultSet，支持流式处理）
//
//	 try (SessionDataSet resultSet = session.executeQuery (deviceId, measurements, startTime, endTime)) {
//
//	 // 3. 解析查询结果
//
//	 while (resultSet.hasNext ()) {
//
//	      Map<String, Object> dataMap = new HashMap<>();
//
//	      // 获取时间戳
//
//	      dataMap.put ("timestamp", resultSet.nextTimestamp ());
//
//	      // 获取测量值（按顺序匹配 measurements 列表）
//
//	      dataMap.put ("temperature", resultSet.nextFloat ());
//
//	      dataMap.put ("humidity", resultSet.nextInt ());
//
//	      resultList.add (dataMap);
//
//	   }
//
//	}
//
//	 return resultList;
//
//}
//
//// 关闭 Session 连接（建议在服务销毁时调用）
//
//@PreDestroy
//
//public void closeSession () throws Exception {
//
//	 if (session != null && session.isOpen ()) {
//
//	   session.close ();
//
//	   System.out.println ("Session 连接已关闭");
//
//	 }
//
//   }
//
//}
//
//
//
