package test;

import com.egb.hades.client.annotations.TraceClass;
import com.egb.hades.client.annotations.TraceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Liuyb on 2015/10/8.
 */
@TraceClass
public class ExampleClass {
    private Logger logger = LoggerFactory.getLogger(ExampleClass.class);

    @TraceMethod
    public String doSomething1() throws Exception {
        System.err.println("abcdef");
        for (int i = 0; i < 50 ; i++) {
            doSomething2(33);
        }
        try {
            Thread.sleep(Math.round(Math.random() % 5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExampleClass2 exampleClass2 = new ExampleClass2();
        exampleClass2.doSomeWork();
        return "This is a example";
    }
    @TraceMethod
    private void doSomething2(long i){
        System.out.println("ghijk");
        doSomething3(101);
        doSomething4(i);
    }
    @TraceMethod
    private void doSomething3(int i){
        System.out.println("qwerty");
        doSomething4(44);
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @TraceMethod
    private void doSomething4(long i){
        System.out.println("oiuioy");
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
