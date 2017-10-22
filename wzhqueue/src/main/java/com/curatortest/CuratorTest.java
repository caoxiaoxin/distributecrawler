package com.curatortest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * @author Zephery
 * @since 2017/10/22 19:56
 * Description:
 */
public class CuratorTest {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(CuratorTest.class);
    private static final String Node_NAME = "n_";//顺序节点的名称
    public static final CuratorFramework client = CuratorFrameworkFactory.builder().connectString("119.23.46.71:2181")
            .sessionTimeoutMs(30000)
            .connectionTimeoutMs(30000)
            .canBeReadOnly(false)
            .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
            .defaultData(null)
            .build();

    public static void nodesList(String parentPath) throws Exception {
        List<String> paths = client.getChildren().forPath(parentPath);
        for (String path : paths) {
            System.out.println(new String(path.getBytes()));
        }
    }

    public static void poll(String parentPath) throws Exception {
        List<String> paths = client.getChildren().forPath(parentPath);
        paths.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getNodeNumber(o1, Node_NAME).compareTo(getNodeNumber(o2, Node_NAME));
            }
        });
        for (String path : paths) {
            System.out.println(new String(path.getBytes()));
        }
    }
    private static String getNodeNumber(String str, String nodeName) {
        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += Node_NAME.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;

    }

    public static void main(String[] args) {
        try {
            client.start();
            nodesList("/Queue");
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}