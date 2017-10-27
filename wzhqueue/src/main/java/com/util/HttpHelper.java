package com.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Zephery
 * @since 2017/10/26 8:56
 * Description:
 */
public class HttpHelper {

    private static final HttpHelper instance = new HttpHelper();
    private static final String CONFIG_FILE = "load.properties";
    private static Configuration configuration;
    private static Logger log = LoggerFactory.getLogger(HttpHelper.class);
    /**
     * 重定向标示位
     */
    private Boolean isRediect = true;
    //创建httpclient实例
    private CloseableHttpClient httpClient = null;

    private HttpHelper() {
        try {
            initHttpClient();
        } catch (ConfigurationException e) {
            log.info("configuratio exception.", e);
        }
    }

    public synchronized static HttpHelper getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
//		HttpHelper.getInstance().doPost("http://192.168.11.248:8080/crawlers/crawler/send_urls", null, "");

    }

    public boolean isRediect() {
        return isRediect;
    }

    public HttpHelper setRediect(boolean isRediect) {
        this.isRediect = isRediect;
        return this;
    }

    /**
     * 描述：创建httpClient连接池，并初始化httpclient
     */
    private void initHttpClient() throws ConfigurationException {
        configuration = new PropertiesConfiguration(CONFIG_FILE);
        //创建httpclient连接池
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(configuration.getInt("http.max.total"));    //设置连接池线程最大数量
        httpClientConnectionManager.setDefaultMaxPerRoute(configuration.getInt("http.max.route"));    //设置单个路由最大的连接线程数量
        //创建http request的配置信息
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(configuration.getInt("http.request.timeout"))
                .setSocketTimeout(configuration.getInt("http.socket.timeout"))
                .setCookieSpec(CookieSpecs.DEFAULT).build();
        //设置重定向策略
        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy() {
            /**
             * false 禁止重定向  true 允许
             */
            @Override
            public boolean isRedirected(HttpRequest request,
                                        HttpResponse response, HttpContext context)
                    throws ProtocolException {
                // TODO Auto-generated method stub
                return isRediect ? super.isRedirected(request, response, context) : isRediect;
            }
        };
        //初始化httpclient客户端
        httpClient = HttpClients.custom().setConnectionManager(httpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                //.setUserAgent(NewsConstant.USER_AGENT)
                .setRedirectStrategy(redirectStrategy)
                .build();
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public String doPost(String url, Map<String, String> params, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        log.info(" post url=" + url);
        try {
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
            }
            HttpPost httpPost = new HttpPost(url);
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            log.error("to request addr=" + url + ", " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String doPost(String url, List<NameValuePair> pairs, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        log.info(" post url=" + url);
        try {
            HttpPost httpPost = new HttpPost(url);
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            log.error("to request addr=" + url + ", " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String doPost(String url, String data, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        log.info(" post url=" + url);
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new NStringEntity(data, charset));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            log.error("to request addr=" + url + ", " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String doPostLongWait(String url, List<NameValuePair> pairs, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        log.info(" post url=" + url);
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(300000)
                    .setConnectTimeout(30000).build();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            log.error("to request addr=" + url + ", " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAborted(String url) {
        return false;
    }

    public String get(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        HttpContext httpContext = new BasicHttpContext();

        HttpGet httpGet = new HttpGet(url);

        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(httpGet, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (log.isDebugEnabled()) {
                log.debug("response code is =>" + statusCode);
                if (statusCode == 404) {
                    System.out.println(url);
                }
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);

            response.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            httpGet.releaseConnection();
            if (null != response) {
                try {
                    //关闭response
                    response.close();
                } catch (IOException e) {
                    // TODO 这里写异常处理的代码
                    e.printStackTrace();
                }
            }
        }

    }


    public Integer getStatusCode(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        HttpContext httpContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(httpGet, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (log.isDebugEnabled()) {
                log.debug("response code is =>" + statusCode);
                if (statusCode == 404) {
                    System.out.println(url);
                }
                if (statusCode == 400) {
                    return statusCode;
                }
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);

            response.close();
            return statusCode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            httpGet.releaseConnection();
            if (null != response) {
                try {
                    //关闭response
                    response.close();
                } catch (IOException e) {
                    // TODO 这里写异常处理的代码
                    e.printStackTrace();
                }
            }
        }

    }

    public String getRediectUrl(String url) {
        HttpGet httpGet = new HttpGet(url);
        HttpContext context = new BasicHttpContext();
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet, context);
            HttpHost targetHost = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            HttpUriRequest request = (HttpUriRequest) context.getAttribute(HttpClientContext.HTTP_REQUEST);
            return targetHost.toString() + request.getURI();
        } catch (IOException e) {
            log.info("io exception.", e);
        } finally {
            httpGet.releaseConnection();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("can't close io exception.");
                }
            }
        }
        return null;
    }

}
