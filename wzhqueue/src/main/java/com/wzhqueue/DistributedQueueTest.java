package com.wzhqueue;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.Serializable;

public class DistributedQueueTest {

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("119.23.46.71:2181", 5000, 5000, new SerializableSerializer());
        if (zkClient.exists("/Queue")) {
            zkClient.deleteRecursive("/Queue");
        }
        DistributedSimpleQueue<SendObject> queue = new DistributedSimpleQueue<SendObject>(zkClient, "/Queue");
        new Thread(new ConsumerThread(queue)).start();
        new Thread(new ProducerThread(queue)).start();

    }

}

class ConsumerThread implements Runnable {
    private DistributedSimpleQueue<SendObject> queue;

    public ConsumerThread(DistributedSimpleQueue<SendObject> queue) {
        this.queue = queue;
    }

    public void run() {
        for (int i = 0; i < 10000; i++) {
            try {
                Thread.sleep((int) (Math.random() * 5000));// 随机睡眠一下
                SendObject sendObject = (SendObject) queue.poll();
                System.out.println("消费一条消息成功：" + sendObject);
            } catch (Exception e) {
            }
        }
    }
}

class ProducerThread implements Runnable {

    private DistributedSimpleQueue<SendObject> queue;

    public ProducerThread(DistributedSimpleQueue<SendObject> queue) {
        this.queue = queue;
    }

    public void run() {
        for (int i = 0; i < 10000; i++) {
            try {
                Thread.sleep((int) (Math.random() * 5000));// 随机睡眠一下
                SendObject sendObject = new SendObject(String.valueOf(i), "content" + i);
                queue.offer(sendObject);
                System.out.println("发送一条消息成功：" + sendObject);
            } catch (Exception e) {
            }
        }
    }

}

class SendObject implements Serializable {

    private static final long serialVersionUID = 1L;

    public SendObject(String id, String content) {
        this.id = id;
        this.content = content;
    }

    private String id;

    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "SendObject [id=" + id + ", content=" + content + "]";
    }

}
