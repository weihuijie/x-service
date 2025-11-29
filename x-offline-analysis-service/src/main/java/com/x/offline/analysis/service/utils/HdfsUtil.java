package com.x.offline.analysis.service.utils;

import com.x.offline.analysis.service.config.HadoopConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class HdfsUtil {
    // 支持的存储格式白名单
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("csv", "parquet");
    // 支持的 Parquet 压缩格式
    private static final List<String> SUPPORTED_COMPRESSIONS = Arrays.asList("snappy", "gzip", "none");

    // 线程局部变量：存储每个线程的 FileSystem 实例（解决线程安全问题）
    private final ThreadLocal<FileSystem> fileSystemThreadLocal = new ThreadLocal<>();
    // 缓存 FileSystem 配置（避免重复创建）
    private final ConcurrentHashMap<String, Configuration> confCache = new ConcurrentHashMap<>();

    // Getter（如需外部访问配置，可保留）
    // 基础配置
    @Getter
    private final String hdfsUrl;
    @Getter
    private final String reportPath;
    private final String yarnResourceManager;

    // 扩展配置（可通过 yml 配置，默认值兜底）
    private final String defaultFormat;
    private final String defaultCharset;
    private final String parquetCompression;
    private final int defaultPartitionNum;

    // Kerberos 安全配置（生产环境启用）
    private final boolean kerberosEnabled;
    private final String kerberosPrincipal;
    private final String kerberosKeytabPath;
    private final String krb5ConfPath;

    /**
     * 构造函数：注入配置并初始化
     */
    public HdfsUtil(
            HadoopConfig hadoopConfig,
            @Value("${hadoop.hdfs.default-format:parquet}") String defaultFormat,
            @Value("${hadoop.hdfs.charset:UTF-8}") String defaultCharset,
            @Value("${hadoop.hdfs.parquet-compression:snappy}") String parquetCompression,
            @Value("${hadoop.hdfs.default-partition:10}") int defaultPartitionNum,
            @Value("${hadoop.kerberos.enabled:false}") boolean kerberosEnabled,
            @Value("${hadoop.kerberos.principal:}") String kerberosPrincipal,
            @Value("${hadoop.kerberos.keytab-path:}") String kerberosKeytabPath,
            @Value("${hadoop.kerberos.krb5-conf-path:}") String krb5ConfPath
    ) {
        // 从 HadoopConfig 读取基础配置
        this.hdfsUrl = hadoopConfig.getHdfs().get("url");
        this.reportPath = hadoopConfig.getHdfs().get("report-path");
        this.yarnResourceManager = hadoopConfig.getYarn().get("resource-manager");

        // 注入扩展配置
        this.defaultFormat = defaultFormat;
        this.defaultCharset = defaultCharset;
        this.parquetCompression = parquetCompression;
        this.defaultPartitionNum = defaultPartitionNum;
        this.kerberosEnabled = kerberosEnabled;
        this.kerberosPrincipal = kerberosPrincipal;
        this.kerberosKeytabPath = kerberosKeytabPath;
        this.krb5ConfPath = krb5ConfPath;

        // 初始化前校验配置
        validateConfig();
        // 处理 Windows 环境依赖
//        setupHadoopEnvironment();
    }

    /**
     * 初始化：创建报表根目录（PostConstruct 确保构造函数执行后调用）
     */
    @PostConstruct
    public void initReportDir() throws IOException {
        FileSystem fs = getFileSystem();
        Path rootPath = new Path(reportPath);
        if (!fs.exists(rootPath)) {
            boolean createSuccess = fs.mkdirs(rootPath);
            if (createSuccess) {
                log.info("HDFS 报表根目录创建成功：{}", reportPath);
            } else {
                throw new IOException("创建 HDFS 报表根目录失败：" + reportPath);
            }
        } else {
            log.info("HDFS 报表根目录已存在：{}", reportPath);
        }
    }

    /**
     * 核心方法：保存报表到 HDFS（使用默认配置）
     */
    public void saveReport(Dataset<Row> reportData, String reportName) throws IOException {
        saveReport(reportData, reportName, defaultFormat, defaultPartitionNum);
    }

    /**
     * 重载方法：自定义格式和分区数
     */
    public void saveReport(Dataset<Row> reportData, String reportName, String format, int partitionNum) throws IOException {
        // 1. 参数校验
        Assert.notNull(reportData, "报表数据 Dataset 不能为 null");
        Assert.hasText(reportName, "报表名称 reportName 不能为空");
        Assert.hasText(format, "存储格式 format 不能为空");
        Assert.isTrue(SUPPORTED_FORMATS.contains(format.toLowerCase()),
                "不支持的存储格式：" + format + "，仅支持：" + SUPPORTED_FORMATS);
        Assert.isTrue(partitionNum > 0, "分区数必须大于 0");

        // 2. 构建 HDFS 路径（按时间戳分目录，避免文件名冲突）
        String timeSuffix = String.valueOf(System.currentTimeMillis());
        String hdfsFilePath = String.format("%s/%s/%s", reportPath, reportName, timeSuffix);
        Path outputPath = new Path(hdfsFilePath);
        log.info("开始保存报表到 HDFS：{}，格式：{}，分区数：{}", hdfsFilePath, format, partitionNum);

        // 3. 数据集预处理（重分区，避免小文件过多）
        Dataset<Row> processedData = reportData.repartition(partitionNum);

        // 4. 写入 HDFS（依赖 SaveMode.Overwrite 自动覆盖，无需手动删除）
        try {
            var writeBuilder = processedData.write()
                    .format(format.toLowerCase())
                    .mode(SaveMode.Overwrite)
                    .option("header", "true") // CSV 带表头，Parquet 忽略该参数
                    .option("encoding", defaultCharset);

            // Parquet 格式额外配置压缩
            if ("parquet".equals(format.toLowerCase())) {
                Assert.isTrue(SUPPORTED_COMPRESSIONS.contains(parquetCompression.toLowerCase()),
                        "不支持的 Parquet 压缩格式：" + parquetCompression + "，仅支持：" + SUPPORTED_COMPRESSIONS);
                writeBuilder.option("compression", parquetCompression.toLowerCase());
            }

            // 执行写入
            writeBuilder.save(hdfsFilePath);
            log.info("报表保存成功：{}", hdfsFilePath);
        } catch (Exception e) {
            log.error("报表保存失败：{}", hdfsFilePath, e);
            // 清理失败的目录（避免残留垃圾数据）
            FileSystem fs = getFileSystem();
            if (fs.exists(outputPath)) {
                fs.delete(outputPath, true);
                log.info("已清理失败的临时目录：{}", hdfsFilePath);
            }
            throw new IOException("报表保存到 HDFS 失败：" + hdfsFilePath, e);
        }
    }

    /**
     * 获取当前线程的 FileSystem 实例（线程安全）
     */
    private FileSystem getFileSystem() throws IOException {
        FileSystem fs = fileSystemThreadLocal.get();
        if (Objects.isNull(fs)) {
            // 不存在则创建新实例
            Configuration conf = buildHadoopConf();
            fs = FileSystem.get(URI.create(hdfsUrl), conf);
            fileSystemThreadLocal.set(fs);
            log.debug("创建新的 FileSystem 实例：{}", hdfsUrl);
        }
        return fs;
    }

    /**
     * 构建 Hadoop 配置（支持 Kerberos 认证）
     */
    private Configuration buildHadoopConf() throws IOException {
        String cacheKey = kerberosEnabled ? hdfsUrl + "_kerberos" : hdfsUrl;
        // 缓存配置，避免重复创建
        return confCache.computeIfAbsent(cacheKey, key -> {
            Configuration conf = new Configuration();
            System.setProperty("HADOOP_USER_NAME", "hadoop");
            // 1. 基础 HDFS 配置
            conf.set("fs.defaultFS", hdfsUrl);
            // 2. YARN 配置（供 Spark 提交任务使用）
            conf.set("yarn.resourcemanager.address", yarnResourceManager);
            conf.set("yarn.resourcemanager.scheduler.address", yarnResourceManager.replace(":8032", ":8030"));

            // 3. Windows环境下禁用本地文件系统权限检查
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // 禁用本地文件系统权限检查，避免需要winutils.exe
                conf.set("dfs.permissions.enabled", "false");
                conf.set("dfs.client.use.datanode.hostname", "true");
                // 禁用本地库
                conf.set("hadoop.native.lib", "false");
                // 禁用本地IO操作
                conf.set("io.native.lib.available", "false");
                // 使用简单权限模型
                conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
                conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
            }

            // 4. Kerberos 安全配置（生产环境启用）
            if (kerberosEnabled) {
                Assert.hasText(kerberosPrincipal, "Kerberos 启用时，principal 不能为空");
                Assert.hasText(kerberosKeytabPath, "Kerberos 启用时，keytab-path 不能为空");
                Assert.hasText(krb5ConfPath, "Kerberos 启用时，krb5-conf-path 不能为空");

                // 设置 Kerberos 配置文件路径
                System.setProperty("java.security.krb5.conf", krb5ConfPath);
                // 配置 Hadoop 安全认证
                conf.set("hadoop.security.authentication", "kerberos");
                try {
                    UserGroupInformation.setConfiguration(conf);
                    UserGroupInformation.loginUserFromKeytab(kerberosPrincipal, kerberosKeytabPath);
                    log.info("Kerberos 认证成功，principal：{}", kerberosPrincipal);
                } catch (IOException e) {
                    log.error("Kerberos 认证失败", e);
                    throw new RuntimeException("Kerberos 认证失败", e);
                }
            }
            return conf;
        });
    }

    /**
     * 配置参数校验（提前暴露非法配置）
     */
    private void validateConfig() {
        Assert.hasText(hdfsUrl, "HDFS 地址（hadoop.hdfs.url）不能为空");
        Assert.hasText(reportPath, "报表存储目录（hadoop.hdfs.report-path）不能为空");
        Assert.hasText(yarnResourceManager, "YARN ResourceManager 地址（hadoop.yarn.resource-manager）不能为空");
        Assert.isTrue(SUPPORTED_FORMATS.contains(defaultFormat.toLowerCase()),
                "默认存储格式不支持：" + defaultFormat + "，仅支持：" + SUPPORTED_FORMATS);
        Assert.isTrue(SUPPORTED_COMPRESSIONS.contains(parquetCompression.toLowerCase()),
                "Parquet 压缩格式不支持：" + parquetCompression + "，仅支持：" + SUPPORTED_COMPRESSIONS);
        Assert.isTrue(defaultPartitionNum > 0, "默认分区数必须大于 0");
    }

    /**
     * Windows 环境 Hadoop 依赖修复（优化版）
     */
    private void setupHadoopEnvironment() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // 禁用本地库以避免需要winutils.exe（在系统级别）
            System.setProperty("hadoop.native.lib", "false");
            System.setProperty("io.native.lib.available", "false");
            
            String hadoopHome = System.getenv("HADOOP_HOME") != null ?
                    System.getenv("HADOOP_HOME") : System.getProperty("hadoop.home.dir");

            if (hadoopHome == null || hadoopHome.isBlank()) {
                String tmpDir = System.getProperty("java.io.tmpdir");
                String hadoopTmpDir = tmpDir + File.separator + "hadoop";
                File binDir = new File(hadoopTmpDir, "bin");

                // 创建目录
                try {
                    if (!binDir.exists()) {
                        binDir.mkdirs();
                    }
                } catch (Exception e) {
                    log.warn("创建Hadoop临时目录失败: {}", e.getMessage());
                }

                // 设置Hadoop主目录
                System.setProperty("hadoop.home.dir", hadoopTmpDir);
            }
        }
    }

    /**
     * 资源释放：关闭当前线程的 FileSystem（供外部手动调用，如线程池场景）
     */
    public void closeCurrentThreadFileSystem() {
        FileSystem fs = fileSystemThreadLocal.get();
        if (Objects.nonNull(fs)) {
            try {
                fs.close();
                log.debug("关闭当前线程的 FileSystem 实例");
            } catch (IOException e) {
                log.error("关闭 FileSystem 失败", e);
            } finally {
                fileSystemThreadLocal.remove(); // 移除 ThreadLocal 引用，避免内存泄漏
            }
        }
    }

    /**
     * 容器销毁时：清理所有线程的 FileSystem 和配置缓存
     */
    @PreDestroy
    public void destroy() {
        log.info("开始清理 HDFS 工具类资源");
        // 1. 关闭所有缓存的 FileSystem（实际生产中 ThreadLocal 仅当前线程持有，此处兜底）
        closeCurrentThreadFileSystem();
        // 2. 清空配置缓存
        confCache.clear();
        log.info("HDFS 工具类资源清理完成");
    }
}