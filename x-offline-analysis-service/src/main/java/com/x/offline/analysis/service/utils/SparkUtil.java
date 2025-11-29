package com.x.offline.analysis.service.utils;

import com.x.offline.analysis.service.config.HadoopConfig;
import com.x.offline.analysis.service.config.IoTDBConfig;
import com.x.offline.analysis.service.config.SparkConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;
/**
 * SPARK 工具类
 *
 * @author whj
 */
@Component
@Slf4j
public class SparkUtil {
    private final SparkConfig sparkConfig;
    private final IoTDBConfig iotdbConfig;
    private SparkSession sparkSession;
    private final HadoopConfig hadoopConfig;

    public SparkUtil(SparkConfig sparkConfig, IoTDBConfig iotdbConfig, HadoopConfig hadoopConfig) {
        this.sparkConfig = sparkConfig;
        this.iotdbConfig = iotdbConfig;
        this.hadoopConfig = hadoopConfig;
    }

    // 懒加载创建 SparkSession（YARN 模式）
    public synchronized SparkSession getSparkSession() {
        if (sparkSession == null || sparkSession.sparkContext().isStopped()) {
            SparkSession.Builder builder = SparkSession.builder()
                    .appName(sparkConfig.getAppName())
                    .master(getEffectiveMaster(sparkConfig.getMaster()))
                    // 禁用Spark UI以避免Servlet相关问题
                    .config("spark.ui.enabled", "false")
                    // IoTDB 连接配置
                    .config("spark.iotdb.url", iotdbConfig.getUrl())
                    .config("spark.iotdb.user", iotdbConfig.getUser())
                    .config("spark.iotdb.password", iotdbConfig.getPassword())
                    // 核心：将 Hadoop 配置注入 Spark
                    .config("spark.hadoop.fs.defaultFS", hadoopConfig.getHdfs().get("url"))
                    .config("spark.hadoop.yarn.resourcemanager.address", hadoopConfig.getYarn().get("resource-manager"))
                    // Spark 资源配置
                    .config("spark.executor.cores", sparkConfig.getExecutor().get("cores"))
                    .config("spark.executor.memory", sparkConfig.getExecutor().get("memory"))
                    .config("spark.executor.instances", sparkConfig.getNumExecutors());

            // 只有在真正使用YARN且不是local模式时才设置jars和deployMode
            if (isYarnMode(getEffectiveMaster(sparkConfig.getMaster()))) {
                builder.config("spark.jars", sparkConfig.getJars());// 依赖包从 HDFS 加载
                builder.config("spark.submit.deployMode", sparkConfig.getDeployMode());
            }

            this.sparkSession = builder.getOrCreate();
            log.info("SparkSession 创建成功（{} 模式）", getEffectiveMaster(sparkConfig.getMaster()));
        }
        return sparkSession;
    }

    // 关闭 SparkSession
    public void stopSparkSession() {
        if (sparkSession != null && !sparkSession.sparkContext().isStopped()) {
            sparkSession.stop();
            sparkSession = null;
            log.info("SparkSession 已关闭");
        }
    }

    /**
     * 获取有效的Spark Master URL
     * @param configuredMaster 配置的master
     * @return 实际使用的master
     */
    private String getEffectiveMaster(String configuredMaster) {
        // 如果配置为yarn但在本地运行，则切换到local模式
        if (configuredMaster.toLowerCase().startsWith("yarn")) {
            // 检查是否在YARN环境中运行
            if (System.getenv("HADOOP_CONF_DIR") == null && 
                System.getenv("YARN_CONF_DIR") == null &&
                !isRunningOnCluster()) {
                log.warn("检测到配置为YARN模式但未在集群环境中运行，切换到local模式");
                return "local[*]"; // 使用本地模式进行开发测试
            }
        }
        return configuredMaster;
    }

    /**
     * 判断是否在集群环境中运行
     * @return 是否在集群中运行
     */
    private boolean isRunningOnCluster() {
        // 检查常见的集群环境变量
        return System.getenv("HADOOP_CONF_DIR") != null || 
               System.getenv("YARN_CONF_DIR") != null ||
               System.getProperty("spark.master") != null;
    }

    /**
     * 判断是否为YARN模式
     * @param master master URL
     * @return 是否为YARN模式
     */
    private boolean isYarnMode(String master) {
        return master != null && master.toLowerCase().startsWith("yarn");
    }
}