package com.feng.hades.agent;

import com.feng.hades.client.dynamicloader.TracerLoader;
import com.feng.hades.client.transformer.HadesTransformerFactory;
import com.feng.hades.client.transformer.TransformerType;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Liuyb on 2015/10/8.
 * 注册自定义的Transformer到虚拟机
 */
public class HadesAgent {
    public static void premain(String options,Instrumentation instrumentation){
        ClassFileTransformer innerTransformer = null;
        if(options == null || options.equals("ANNOTATION"))
            innerTransformer = HadesTransformerFactory.getTransformer(TransformerType.ANNOTATION);
        else if(options.equals("METHODNAME")){
            innerTransformer = HadesTransformerFactory.getTransformer(TransformerType.ANNOTATION);
        } else {
            throw new IllegalArgumentException("Illegal Options Found!");
        }
        Thread loaderThread = new Thread(new TracerLoader());
        loaderThread.start();
        ClassFileTransformer hadesTransformer = new HadesPreProcessAdapter(innerTransformer);
        instrumentation.addTransformer(hadesTransformer);
    }
}
