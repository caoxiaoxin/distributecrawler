package com.curatortest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author Zephery
 * @since 2017/10/22 14:46
 * Description:
 */
public class DisLock {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(DisLock.class);
    private static String lockPath = "/curator_recipes_lock_path";
    private static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("119.23.46.71:2181")
            .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

    public static void main(String[] args) {
        client.start();
        final InterProcessMutex lock = new InterProcessMutex(client, lockPath);
        final CountDownLatch down = new CountDownLatch(1);
        for (int i = 0; i < 30; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        down.await();//让所有线程都等待
                        lock.acquire();
                    } catch (Exception e) {
                        logger.error("" + e);
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
                    String orderNo = sdf.format(new Date());
                    System.out.println("product id :" + orderNo);
                    try {
                        lock.release();
                    } catch (Exception e) {
                        logger.error("" + e);
                    }
                }
            }).start();
        }
        System.out.println("finish");
        down.countDown();//计数器减1，所有开始同时启动。

    }
}