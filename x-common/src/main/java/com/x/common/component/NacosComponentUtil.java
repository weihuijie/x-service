package com.x.common.component;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Nacos组件工具类
 * 提供Nacos配置中心和服务注册发现操作常用方法的封装
 */
@Component
public class NacosComponentUtil {

    @Autowired(required = false)
    private ConfigService configService;

    @Autowired(required = false)
    private NamingService namingService;

    /**
     * 获取配置信息
     * @param dataId 配置ID
     * @param group 分组
     * @return 配置内容
     * @throws NacosException Nacos异常
     */
    public String getConfig(String dataId, String group) throws NacosException {
        if (configService == null) {
            throw new IllegalStateException("ConfigService未初始化");
        }

        return configService.getConfig(dataId, group, 3000);
    }

    /**
     * 发布配置信息
     * @param dataId 配置ID
     * @param group 分组
     * @param content 配置内容
     * @return 是否发布成功
     * @throws NacosException Nacos异常
     */
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        if (configService == null) {
            throw new IllegalStateException("ConfigService未初始化");
        }

        return configService.publishConfig(dataId, group, content);
    }

    /**
     * 删除配置信息
     * @param dataId 配置ID
     * @param group 分组
     * @return 是否删除成功
     * @throws NacosException Nacos异常
     */
    public boolean removeConfig(String dataId, String group) throws NacosException {
        if (configService == null) {
            throw new IllegalStateException("ConfigService未初始化");
        }

        return configService.removeConfig(dataId, group);
    }

    /**
     * 注册服务实例
     * @param serviceName 服务名称
     * @param ip IP地址
     * @param port 端口
     * @throws NacosException Nacos异常
     */
    public void registerInstance(String serviceName, String ip, int port) throws NacosException {
        if (namingService == null) {
            throw new IllegalStateException("NamingService未初始化");
        }

        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        namingService.registerInstance(serviceName, instance);
    }

    /**
     * 注册服务实例（带权重和元数据）
     * @param serviceName 服务名称
     * @param ip IP地址
     * @param port 端口
     * @param weight 权重
     * @param metadata 元数据
     * @throws NacosException Nacos异常
     */
    public void registerInstance(String serviceName, String ip, int port, double weight, java.util.Map<String, String> metadata) throws NacosException {
        if (namingService == null) {
            throw new IllegalStateException("NamingService未初始化");
        }

        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(weight);
        instance.setMetadata(metadata);
        namingService.registerInstance(serviceName, instance);
    }

    /**
     * 获取服务实例列表
     * @param serviceName 服务名称
     * @return 实例列表
     * @throws NacosException Nacos异常
     */
    public List<Instance> getAllInstances(String serviceName) throws NacosException {
        if (namingService == null) {
            throw new IllegalStateException("NamingService未初始化");
        }

        return namingService.getAllInstances(serviceName);
    }

    /**
     * 获取健康的服务实例列表
     * @param serviceName 服务名称
     * @return 健康实例列表
     * @throws NacosException Nacos异常
     */
    public List<Instance> getHealthyInstances(String serviceName) throws NacosException {
        if (namingService == null) {
            throw new IllegalStateException("NamingService未初始化");
        }

        return namingService.selectInstances(serviceName, true);
    }

    /**
     * 注销服务实例
     * @param serviceName 服务名称
     * @param ip IP地址
     * @param port 端口
     * @throws NacosException Nacos异常
     */
    public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {
        if (namingService == null) {
            throw new IllegalStateException("NamingService未初始化");
        }

        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        namingService.deregisterInstance(serviceName, instance);
    }
}
