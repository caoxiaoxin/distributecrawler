package com.curatortest;


import com.util.HttpHelper;
import com.wzhqueue.DistributedSimpleQueue;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zephery
 * @since 2017/10/22 16:05
 * Description:
 */
public class Crawler {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private static String lockPath = "/curator_recipes_lock_path";
    private static ZkClient zkClient = new ZkClient("119.23.46.71:2181", 500000, 500000, new SerializableSerializer());
    private static DistributedSimpleQueue<String> queue = new DistributedSimpleQueue<>(zkClient, "/Queue");
    private static void produce(String url) {
        try {
            if (zkClient.exists("/Queue")) {
                zkClient.deleteRecursive("/Queue");
                zkClient.createPersistent("/Queue");
            }
            String content = HttpHelper.getInstance().get(url);
            Elements elements = Jsoup.parse(content).select("a");
            System.out.println(elements.size());
            int count = 1;
            for (Element element : elements) {
                String uurl = element.attr("href");
                if (!uurl.contains("javascript")) {
                    queue.offer(uurl);
                    System.out.println(uurl);
                    count = count + 1;
                    if (count > 0) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private static void consume() {
        try {
            while (!queue.isEmpty()) {
                String url = queue.poll();
                System.out.println("consume: " + url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void print(String url) {
        String content = HttpHelper.getInstance().get(url);
        System.out.println(content);
    }

    public static void main(String[] args) {
        produce("http://www.sina.com.cn/");
//        consume();
    }
}