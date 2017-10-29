package com.crawler;

import com.util.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zephery
 * @since 2017/10/26 8:55
 * Description:
 */
public class Producer {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    public static final CuratorFramework client = CuratorFrameworkFactory.builder().connectString("119.23.46.71:2181")
            .sessionTimeoutMs(1000)
            .connectionTimeoutMs(1000)
            .canBeReadOnly(false)
            .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
            .defaultData(null)
            .build();
    private static SimpleDistributedQueue queue = new SimpleDistributedQueue(client, "/Queue");
    private static Integer j = 0;

    public static void begin(String url) {
        try {
            String content = HttpHelper.getInstance().get(url);
            resolveweb(content);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static void resolveweb(String content) throws Exception {
        Elements elements = Jsoup.parse(content).select("a");
        for (Element element : elements) {
            String url = element.attr("href");
            if (StringUtils.isNotEmpty(url) && !url.contains("javascript") && !url.contains("jump")) {
                logger.info(url + " " + String.valueOf(j++));
                queue.offer(url.getBytes());
            }
        }
    }

    public static void main(String[] args) {
        client.start();
        for (int i = 0; i < 100; i++) {
            begin("https://www.cnblogs.com/#p" + String.valueOf(i));
        }
//        begin("http://www.sina.com.cn/");
    }
}