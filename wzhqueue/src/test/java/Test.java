/**
 * @author Zephery
 * @since 2017/10/22 13:54
 * Description:
 */
class Worker extends Thread {

    //工作者名
    private String name;
    //工作时间
    private long time;

    public Worker(String name, long time) {
        this.name = name;
        this.time = time;
    }

    @Override
    public void run() {
        // TODO 自动生成的方法存根
        try {
            System.out.println(name+"开始工作");
            Thread.sleep(time);
            System.out.println(name+"工作完成，耗费时间="+time);
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }
}
public class Test {
    public static void main(String[] args) throws InterruptedException {
        // TODO 自动生成的方法存根

        Worker worker0 = new Worker("worker0", (long) (Math.random()*2000+3000));
        Worker worker1 = new Worker("worker1", (long) (Math.random()*2000+3000));
        Worker worker2 = new Worker("worker2", (long) (Math.random()*2000+3000));

        worker0.start();
        worker1.start();

        worker0.join();
        worker1.join();
        System.out.println("准备工作就绪");

        worker2.start();
    }  }