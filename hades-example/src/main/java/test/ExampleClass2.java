package test;

import com.feng.hades.client.annotations.TraceClass;
import com.feng.hades.client.annotations.TraceMethod;

/**
 * Created by Liuyb on 2015/10/9.
 */
@TraceClass
public class ExampleClass2 {
    @TraceMethod
    public void doSomeWork() throws Exception {
        System.out.println("12344123123123123123");
        if(Math.round(Math.random()*100 % 5) == 1) throw new Exception("用来测试的异常！");
    }
}
