package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Liuyb on 2015/10/8.
 */
public class AgentTest {
    public static void main(String[] args) throws Exception {

        int THREAD_NUMBER = 1000;
        int QUEUE_SIZE = 1000;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(100,1000,1000, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(QUEUE_SIZE));

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUMBER);
        for (int i = 0; i < THREAD_NUMBER; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    ExampleClass exampleClass = new ExampleClass();
                    try {
                        exampleClass.doSomething1();

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        executor.shutdown();



    }


}
