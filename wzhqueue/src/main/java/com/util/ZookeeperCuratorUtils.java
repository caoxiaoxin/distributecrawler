package com.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * @author Zephery
 * @since 2017/10/22 20:03
 * Description:
 */
public class ZookeeperCuratorUtils {

    private static CuratorFramework clientOne() {
        //zk 地址
        String connectString = "10.125.2.44:2181";
        // 连接时间 和重试次数
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start();
        return client;
    }


    private static void nodesList(CuratorFramework client, String parentPath) throws Exception {
        List<String> paths = client.getChildren().forPath(parentPath);
        for (String path : paths) {
            System.out.println(path);
        }
    }

    private static void createNode(CuratorFramework client, String path) throws Exception {

        Stat stat = client.checkExists().forPath(path);
        System.out.println(stat);
        String forPath = client.create().creatingParentsIfNeeded().forPath(path, "create init !".getBytes());
        // String forPath = client.create().forPath(path);
        System.out.println(forPath);
    }

    /**
     * 获取指定节点中信息
     *
     * @throws Exception
     */
    private static void getDataNode(CuratorFramework client, String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        System.out.println(stat);
        byte[] datas = client.getData().forPath(path);
        System.out.println(new String(datas));
    }


    private static void setDataNode(CuratorFramework client, String path, String message) throws Exception {

        Stat stat = client.checkExists().forPath(path);
        System.out.println(stat);
        client.setData().forPath(path, message.getBytes());
    }

    private static void deleteDataNode(CuratorFramework client, String path) throws Exception {

        Stat stat = client.checkExists().forPath(path);
        System.out.println("deleteNode : " + stat);

        Void forPath = client.delete().deletingChildrenIfNeeded().forPath(path);

        System.out.println(forPath);
    }

    public static void main(String[] args) throws Exception {
        // nodesList(clientOne(), "/");
        CuratorFramework client = clientOne();
        //nodesList(client, "/");
        //使用clientTwo来创建一个节点空间 查看是加密
        // createNode(client, "/usermanager");
        //setDataNode(client, "/usermanager", "test writer 测试写入效果!");
        //  getDataNode(client, "/usermanager");
        // createNode(client, "/three/two/testone");
        //  deleteDataNode(client, "/three");;
    }
}