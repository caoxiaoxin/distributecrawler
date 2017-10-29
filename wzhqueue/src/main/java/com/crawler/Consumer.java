package com.crawler;

import com.util.HttpHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author Zephery
 * @since 2017/10/26 8:56
 * Description:
 */
public class Consumer {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private static final CuratorFramework client = CuratorFrameworkFactory.builder().connectString("119.23.46.71:2181")
            .sessionTimeoutMs(1000)
            .connectionTimeoutMs(1000)
            .canBeReadOnly(false)
            .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
            .defaultData(null)
            .build();
    private static SimpleDistributedQueue queue = new SimpleDistributedQueue(client, "/SinaQueue");
    private static Integer i = 0;
    private static final Integer CORE = Runtime.getRuntime().availableProcessors();
    //声明为一个数组型的阻塞队列，这里限制大小为
    private static final BlockingQueue<Runnable> queuelength = new ArrayBlockingQueue<>(1000);

    static class CBCrawler implements Runnable {
        private String url;

        public CBCrawler(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            String content = HttpHelper.getInstance().get(url);
            logger.info(url + " " + Jsoup.parse(content).title());
            sleepUtil(4);
        }

        public static void sleepUtil(Integer time) {
            try {
                Thread.sleep(time * 1000);
            } catch (Exception e) {
                logger.error("线程sleep异常", e);
            }
        }
    }

    public static void begin() {
        try {
            ExecutorService es = new ThreadPoolExecutor(CORE, CORE,
                    0L, TimeUnit.MILLISECONDS,
                    queuelength);
            while (client.getChildren().forPath("/SinaQueue").size() > 0) {
                CBCrawler crawler = new CBCrawler(new String(queue.take()));
                es.submit(crawler);
                i = i + 1;
                logger.info(String.valueOf(i) + " is finished\n" + " queue size is" + queuelength.size());
            }
            if (!es.isShutdown()) {
                es.shutdown();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        client.start();
        begin();
        client.close();
        logger.info("start time: " + start);
        long end = System.currentTimeMillis();
        logger.info("end time: " + end);
        logger.info("take time: " + String.valueOf(end - start));
    }
}
