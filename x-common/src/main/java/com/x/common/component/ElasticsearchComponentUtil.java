package com.x.common.component;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Elasticsearch组件工具类
 * 提供Elasticsearch搜索引擎操作常用方法的封装
 */
@Component
public class ElasticsearchComponentUtil {
    
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    
    /**
     * 索引文档
     * @param index 索引名称
     * @param id 文档ID
     * @param source 文档内容
     * @return 是否索引成功
     * @throws IOException IO异常
     */
    public boolean indexDocument(String index, String id, Map<String, Object> source) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index)
                .id(id)
                .source(source);
        
        try {
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 搜索文档
     * @param index 索引名称
     * @param field 字段名
     * @param value 字段值
     * @return 搜索结果
     * @throws IOException IO异常
     */
    public SearchResponse searchDocument(String index, String field, String value) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(field, value));
        searchRequest.source(searchSourceBuilder);
        
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }
    
    /**
     * 搜索所有文档
     * @param index 索引名称
     * @return 搜索结果
     * @throws IOException IO异常
     */
    public SearchResponse searchAllDocuments(String index) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }
    
    /**
     * 检查Elasticsearch客户端是否连接正常
     * @return 是否连接正常
     */
    public boolean isClientAvailable() {
        try {
            return restHighLevelClient != null && restHighLevelClient.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            return false;
        }
    }
}