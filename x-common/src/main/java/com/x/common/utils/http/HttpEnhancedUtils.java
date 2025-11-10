package com.x.common.utils.http;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * 增强版 HttpClient 工具类（修复代理类型错误，支持 ProxySelector）
 */
public class HttpEnhancedUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpEnhancedUtils.class);

    // 默认线程池（CPU核心数 * 2，动态适配）
    private static final ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
    );

    private HttpEnhancedUtils() {}

    // ==================== 核心修复：自定义固定代理选择器 ====================
    /**
     * 固定代理选择器（实现 ProxySelector 接口）
     * 用于指定固定的 HTTP/HTTPS 代理，适配 HttpClient 要求
     */
    private static class FixedProxySelector extends ProxySelector {
        private final Proxy proxy;

        // 构造器：传入代理类型（默认HTTP）、主机、端口
        public FixedProxySelector(String host, int port) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }

        // 构造器：支持自定义代理类型
        public FixedProxySelector(Proxy.Type type, String host, int port) {
            this.proxy = new Proxy(type, new InetSocketAddress(host, port));
        }

        // 核心方法：为指定URI选择代理（返回固定代理）
        @Override
        public List<Proxy> select(URI uri) {
            return Collections.singletonList(proxy);
        }

        // 核心方法：通知代理连接失败（空实现，可根据需求扩展）
        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            log.error("代理连接失败，URI：{}，代理地址：{}", uri, sa, ioe);
        }
    }

    // ==================== 建造者类（修复代理配置逻辑）====================
    public static class RequestBuilder<T> {
        private final String url;
        private final String method;
        private Map<String, String> headers = new HashMap<>();
        private String jsonBody;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration responseTimeout = Duration.ofSeconds(10);
        private int retryTimes = 3;
        private long retryDelay = 1000;
        private HttpClient.Redirect redirectPolicy = HttpClient.Redirect.NORMAL;
        private ProxySelector proxySelector; // 改为 ProxySelector 类型
        private SSLContext sslContext;
        // 重试条件（默认：5xx + 非中断异常）
        private Predicate<Throwable> retryPredicate = ex -> {
            if (ex instanceof HttpRetryException) {
                int statusCode = ((HttpRetryException) ex).getStatusCode();
                return statusCode >= 500 && statusCode < 600;
            }
            return !(ex instanceof InterruptedException) && !(ex.getCause() instanceof InterruptedException);
        };
        // 响应处理器（默认字符串）
        @SuppressWarnings("unchecked")
        private HttpResponse.BodyHandler<T> responseHandler = (HttpResponse.BodyHandler<T>) HttpResponse.BodyHandlers.ofString();

        private RequestBuilder(String url, String method) {
            this.url = Objects.requireNonNull(url, "URL不能为空");
            this.method = Objects.requireNonNull(method, "请求方法不能为空");
        }

        // 所有原有配置方法保持不变（省略重复代码）
        public RequestBuilder<T> headers(Map<String, String> headers) {
            if (headers != null && !headers.isEmpty()) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public RequestBuilder<T> jsonBody(String jsonBody) {
            this.jsonBody = jsonBody;
            this.headers.putIfAbsent("Content-Type", "application/json");
            return this;
        }

        public RequestBuilder<T> connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "连接超时不能为空");
            return this;
        }

        public RequestBuilder<T> responseTimeout(Duration responseTimeout) {
            this.responseTimeout = Objects.requireNonNull(responseTimeout, "响应超时不能为空");
            return this;
        }

        public RequestBuilder<T> retryTimes(int retryTimes) {
            if (retryTimes < 0) {
                throw new IllegalArgumentException("重试次数不能为负数");
            }
            this.retryTimes = retryTimes;
            return this;
        }

        public RequestBuilder<T> retryDelay(long retryDelay) {
            if (retryDelay < 0) {
                throw new IllegalArgumentException("重试间隔不能为负数");
            }
            this.retryDelay = retryDelay;
            return this;
        }

        public RequestBuilder<T> retryPredicate(Predicate<Throwable> retryPredicate) {
            this.retryPredicate = Objects.requireNonNull(retryPredicate, "重试条件不能为空");
            return this;
        }

        public RequestBuilder<T> redirectPolicy(HttpClient.Redirect redirectPolicy) {
            this.redirectPolicy = Objects.requireNonNull(redirectPolicy, "重定向策略不能为空");
            return this;
        }

        // ==================== 修复代理配置方法 ====================
        /**
         * 配置HTTP代理（默认代理类型：HTTP）
         * @param host 代理主机
         * @param port 代理端口
         * @return RequestBuilder
         */
        public RequestBuilder<T> proxy(String host, int port) {
            Objects.requireNonNull(host, "代理主机不能为空");
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("代理端口无效（1-65535）");
            }
            this.proxySelector = new FixedProxySelector(host, port);
            return this;
        }

        /**
         * 配置自定义类型代理（支持HTTP/SOCKS）
         * @param type 代理类型（Proxy.Type.HTTP / Proxy.Type.SOCKS）
         * @param host 代理主机
         * @param port 代理端口
         * @return RequestBuilder
         */
        public RequestBuilder<T> proxy(Proxy.Type type, String host, int port) {
            Objects.requireNonNull(type, "代理类型不能为空");
            Objects.requireNonNull(host, "代理主机不能为空");
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("代理端口无效（1-65535）");
            }
            this.proxySelector = new FixedProxySelector(type, host, port);
            return this;
        }

        public RequestBuilder<T> sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public <U> RequestBuilder<U> responseHandler(HttpResponse.BodyHandler<U> responseHandler) {
            RequestBuilder<U> newBuilder = new RequestBuilder<>(this.url, this.method);
            newBuilder.headers = this.headers;
            newBuilder.jsonBody = this.jsonBody;
            newBuilder.connectTimeout = this.connectTimeout;
            newBuilder.responseTimeout = this.responseTimeout;
            newBuilder.retryTimes = this.retryTimes;
            newBuilder.retryDelay = this.retryDelay;
            newBuilder.redirectPolicy = this.redirectPolicy;
            newBuilder.proxySelector = this.proxySelector; // 复制代理配置
            newBuilder.sslContext = this.sslContext;
            newBuilder.retryPredicate = this.retryPredicate;
            newBuilder.responseHandler = Objects.requireNonNull(responseHandler, "响应处理器不能为空");
            return newBuilder;
        }

        // 同步请求（抛出非受检异常）
        public T sendSync() {
            HttpClient client = buildHttpClient();
            HttpRequest request = buildHttpRequest();
            return sendSyncWithRetry(client, request, retryTimes, retryDelay, 0);
        }

        // 异步请求
        public CompletableFuture<T> sendAsync() {
            HttpClient client = buildHttpClient();
            HttpRequest request = buildHttpRequest();
            return sendAsyncWithRetry(client, request, retryTimes, retryDelay, 0);
        }

        // ==================== 修复HttpClient构建逻辑 ====================
        private HttpClient buildHttpClient() {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .connectTimeout(connectTimeout)
                    .followRedirects(redirectPolicy)
                    .executor(DEFAULT_EXECUTOR);

            // 配置代理（传入 ProxySelector 实例，修复类型错误）
            if (proxySelector != null) {
                builder.proxy(proxySelector);
                log.debug("已配置代理：{}", proxySelector.select(URI.create(url)).get(0));
            }

            // 配置SSL上下文
            if (sslContext != null) {
                builder.sslContext(sslContext);
            }

            return builder.build();
        }

        // 构建HttpRequest（不变）
        private HttpRequest buildHttpRequest() {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(responseTimeout);
            headers.forEach(builder::header);

            switch (method.toUpperCase()) {
                case "GET":
                    builder.GET();
                    break;
                case "POST":
                    builder.POST(jsonBody != null ? HttpRequest.BodyPublishers.ofString(jsonBody) : HttpRequest.BodyPublishers.noBody());
                    break;
                case "PUT":
                    builder.PUT(jsonBody != null ? HttpRequest.BodyPublishers.ofString(jsonBody) : HttpRequest.BodyPublishers.noBody());
                    break;
                case "DELETE":
                    builder.DELETE();
                    break;
                default:
                    builder.method(method, jsonBody != null ? HttpRequest.BodyPublishers.ofString(jsonBody) : HttpRequest.BodyPublishers.noBody());
            }
            return builder.build();
        }

        // 同步重试逻辑（不变）
        private T sendSyncWithRetry(HttpClient client, HttpRequest request, int maxRetry, long retryDelay, int currentRetry) {
            try {
                HttpResponse<T> response = client.send(request, responseHandler);
                int statusCode = response.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    log.debug("请求成功 [{} {}]，状态码：{}", method, url, statusCode);
                    return response.body();
                }
                throw new HttpRetryException(
                        "请求返回非成功状态码",
                        statusCode,
                        url,
                        method,
                        currentRetry,
                        maxRetry
                );
            } catch (HttpRetryException e) {
                if (currentRetry < maxRetry && retryPredicate.test(e)) {
                    return retrySync(client, request, maxRetry, retryDelay, currentRetry, e);
                } else {
                    throw e;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new HttpRetryException(
                        "请求被中断",
                        409,
                        url,
                        method,
                        currentRetry,
                        maxRetry,
                        ie
                );
            } catch (Exception e) {
                if (currentRetry < maxRetry && retryPredicate.test(e)) {
                    return retrySync(client, request, maxRetry, retryDelay, currentRetry, e);
                } else {
                    throw new HttpRetryException(
                            "请求失败（重试耗尽）",
                            500,
                            url,
                            method,
                            currentRetry,
                            maxRetry,
                            e
                    );
                }
            }
        }

        // 同步重试辅助方法（不变）
        private T retrySync(HttpClient client, HttpRequest request, int maxRetry, long retryDelay, int currentRetry, Throwable ex) {
            int nextRetry = currentRetry + 1;
            log.warn("第{}次重试请求 [{} {}]，原因：{}", nextRetry, method, url, ex.getMessage());
            try {
                TimeUnit.MILLISECONDS.sleep(retryDelay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new HttpRetryException(
                        "重试时被中断",
                        409,
                        url,
                        method,
                        nextRetry,
                        maxRetry,
                        ie
                );
            }
            return sendSyncWithRetry(client, request, maxRetry, retryDelay, nextRetry);
        }

        // 异步重试逻辑（不变）
        private CompletableFuture<T> sendAsyncWithRetry(HttpClient client, HttpRequest request, int maxRetry, long retryDelay, int currentRetry) {
            return client.sendAsync(request, responseHandler)
                    .thenApply(response -> {
                        int statusCode = response.statusCode();
                        if (statusCode >= 200 && statusCode < 300) {
                            log.debug("异步请求成功 [{} {}]，状态码：{}", method, url, statusCode);
                            return response.body();
                        }
                        throw new HttpRetryException(
                                "异步请求返回非成功状态码",
                                statusCode,
                                url,
                                method,
                                currentRetry,
                                maxRetry
                        );
                    })
                    .exceptionallyCompose(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        if (currentRetry < maxRetry && retryPredicate.test(cause)) {
                            int nextRetry = currentRetry + 1;
                            log.warn("第{}次异步重试请求 [{} {}]，原因：{}", nextRetry, method, url, cause.getMessage());
                            return CompletableFuture.runAsync(() -> {
                                        try {
                                            TimeUnit.MILLISECONDS.sleep(retryDelay);
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }, DEFAULT_EXECUTOR)
                                    .thenCompose(v -> sendAsyncWithRetry(client, request, maxRetry, retryDelay, nextRetry));
                        } else {
                            return CompletableFuture.failedFuture(
                                    new HttpRetryException(
                                            "异步请求失败（重试耗尽）",
                                            500,
                                            url,
                                            method,
                                            currentRetry,
                                            maxRetry,
                                            cause
                                    )
                            );
                        }
                    });
        }
    }

    // ==================== 静态工厂方法（不变）====================
    public static <T> RequestBuilder<T> get(String url) {
        return new RequestBuilder<>(url, "GET");
    }

    public static <T> RequestBuilder<T> post(String url) {
        return new RequestBuilder<>(url, "POST");
    }

    public static <T> RequestBuilder<T> put(String url) {
        return new RequestBuilder<>(url, "PUT");
    }

    public static <T> RequestBuilder<T> delete(String url) {
        return new RequestBuilder<>(url, "DELETE");
    }

    // ==================== 非受检异常类（不变）====================
    @Getter
    public static class HttpRetryException extends RuntimeException {
        // Getter方法
        private final int statusCode;
        private final String url;
        private final String method;
        private final int currentRetry;
        private final int maxRetry;

        public HttpRetryException(String message, int statusCode, String url, String method, int currentRetry, int maxRetry) {
            super(message);
            this.statusCode = statusCode;
            this.url = url;
            this.method = method;
            this.currentRetry = currentRetry;
            this.maxRetry = maxRetry;
        }

        public HttpRetryException(String message, int statusCode, String url, String method, int currentRetry, int maxRetry, Throwable cause) {
            super(message, cause);
            this.statusCode = statusCode;
            this.url = url;
            this.method = method;
            this.currentRetry = currentRetry;
            this.maxRetry = maxRetry;
        }

        @Override
        public String toString() {
            return String.format(
                    "HttpRetryException{message='%s', statusCode=%d, method='%s', url='%s', currentRetry=%d, maxRetry=%d}",
                    getMessage(), statusCode, method, url, currentRetry, maxRetry
            );
        }
    }

    // ==================== 工具方法（不变）====================
    public static SSLContext createDefaultSslContext() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    public static void shutdownExecutor() {
        if (!DEFAULT_EXECUTOR.isShutdown()) {
            DEFAULT_EXECUTOR.shutdown();
            try {
                if (!DEFAULT_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    DEFAULT_EXECUTOR.shutdownNow();
                }
            } catch (InterruptedException e) {
                DEFAULT_EXECUTOR.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("HTTP工具类线程池已关闭");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 同步请求
        try {
            // 发送同步请求（无需声明 throws）
            String response = HttpEnhancedUtils.get("https://api.example.com/data")
                    .retryTimes(3)
                    .retryDelay(1000)
                    .sendSync().toString();
            log.info("响应结果：{}", response);
        } catch (HttpEnhancedUtils.HttpRetryException e) {
            // 按需捕获异常，获取详细上下文
            log.error("同步请求失败：{}，状态码：{}，请求：{} {}",
                    e.getMessage(),
                    e.getStatusCode(),
                    e.getMethod(),
                    e.getUrl(),
                    e); // 打印异常栈，包含根因

            // 业务异常处理（如返回默认值、告警等）
            String defaultResponse = "{\"code\":-1,\"msg\":\"请求失败\"}";
            log.info("使用默认响应：{}", defaultResponse);
        } catch (IllegalArgumentException e) {
            // 捕获参数非法异常（如重试次数为负数）
            log.error("参数配置错误：{}", e.getMessage());
        } finally {
            HttpEnhancedUtils.shutdownExecutor();
        }


        // 异步请求
        // 发送异步请求
        CompletableFuture<Object> future = HttpEnhancedUtils.post("https://api.example.com/submit")
                .jsonBody("{\"name\":\"test\"}")
                .retryTimes(3)
                .sendAsync();

        // 方式1：使用 exceptionally 捕获异常（返回默认值）
        /*future.exceptionally(ex -> {
            if (ex instanceof HttpRetryException retryEx) {
                log.error("异步请求失败：{}，重试次数：{}/{}",
                        retryEx.getMessage(),
                        retryEx.getCurrentRetry(),
                        retryEx.getMaxRetry());
            } else {
                log.error("异步请求未知异常", ex);
            }
            return "{\"code\":-1,\"msg\":\"异步请求失败\"}";
        }).thenAccept(response -> log.info("最终响应：{}", response));*/

        // 方式2：使用 whenComplete 处理结果和异常（更灵活）
        future.whenComplete((response, ex) -> {
            if (ex == null) {
                log.info("响应结果：{}", response);
            } else {
                if (ex instanceof HttpRetryException retryEx) {
                    log.error("异步请求失败：{}，URL：{}", retryEx.getMessage(), retryEx.getUrl());
                } else {
                    log.error("异步请求异常", ex);
                }
            }
        });

        // 等待异步任务完成
        Thread.sleep(5000);
        HttpEnhancedUtils.shutdownExecutor();


        // http代理
        try {
            // 配置HTTP代理（主机：127.0.0.1，端口：8888）
            String response = HttpEnhancedUtils.get("https://api.example.com/data")
                    .proxy("127.0.0.1", 8888) // 简化版HTTP代理
                    .retryTimes(1)
                    .sendSync().toString();
            log.info("响应结果：{}", response);
        } catch (HttpEnhancedUtils.HttpRetryException e) {
            log.error("请求失败：{}", e.getMessage(), e);
        } finally {
            HttpEnhancedUtils.shutdownExecutor();
        }

        // 配置SOCKS5代
        try {
            // 配置SOCKS5代理（类型：SOCKS，主机：192.168.1.1，端口：1080）
            String response = HttpEnhancedUtils.post("https://api.example.com/submit")
                    .proxy(Proxy.Type.SOCKS, "192.168.1.1", 1080) // 自定义代理类型
                    .jsonBody("{\"name\":\"test\"}")
                    .sendSync().toString();
            log.info("响应结果：{}", response);
        } catch (HttpEnhancedUtils.HttpRetryException e) {
            log.error("请求失败：{}", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("代理配置错误：{}", e.getMessage());
        } finally {
            HttpEnhancedUtils.shutdownExecutor();
        }
    }
}