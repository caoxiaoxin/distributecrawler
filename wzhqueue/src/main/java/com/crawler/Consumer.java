package com.crawler;

import com.util.HttpHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zephery
 * @since 2017/10/26 8:56
 * Description:
 */
public class Consumer {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    public static final CuratorFramework client = CuratorFrameworkFactory.builder().connectString("119.23.46.71:2181")
            .sessionTimeoutMs(30000)
            .connectionTimeoutMs(30000)
            .canBeReadOnly(false)
            .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
            .defaultData(null)
            .build();
    private static SimpleDistributedQueue queue = new SimpleDistributedQueue(client, "/Queue");

    public static void begin() {
        try {
            System.out.println(client.getChildren().forPath("/Queue").size());
            crawler();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static void crawler() throws Exception {
        String url = new String(queue.poll());
        String content = HttpHelper.getInstance().get(url);
        logger.info(url + " " + Jsoup.parse(content).title());
    }

    public static void main(String[] args) {
        client.start();
        begin();
    }
}
