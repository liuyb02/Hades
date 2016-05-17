package com.egb.hades.agent;
import com.egb.hades.client.transformer.HadesPreProcessAdapter;
import com.egb.hades.client.transformer.HadesTransformerFactory;
import com.egb.hades.client.transformer.TransformerType;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * Created by Liuyb on 2015/10/8.
 * 注册自定义的Transformer到虚拟机
 */
public class HadesAgent {
    private static volatile Instrumentation instrumentation;

    public static void premain(String options, Instrumentation inst) {
        instrumentation = inst;
        if("DELEGATE".equals(options)) {
            ClassFileTransformer innerTransformer = null;
//            String sauronMode = HadesConfig.getSauronMode();
            String sauronMode = "ANNOTATION";
            if (sauronMode == null || sauronMode.toUpperCase().equals("ANNOTATION") ) {
                innerTransformer = HadesTransformerFactory.getTransformer(TransformerType.ANNOTATION);
            } else if (sauronMode.toUpperCase().equals("METHODNAME")) {
                innerTransformer = HadesTransformerFactory.getTransformer(TransformerType.METHODNAME);
            }
            ClassFileTransformer sauronTransformer = new HadesPreProcessAdapter(innerTransformer);
            instrumentation.addTransformer(sauronTransformer);
            instrumentation = null;
        }
    }

    public static void agentmain(String options, Instrumentation inst) {
        instrumentation = inst;
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
