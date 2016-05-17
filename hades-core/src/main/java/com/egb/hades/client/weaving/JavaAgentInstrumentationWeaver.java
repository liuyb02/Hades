package com.egb.hades.client.weaving;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Liuyb on 2015/11/20.
 */
public class JavaAgentInstrumentationWeaver implements HadesLoadTimeWeaver {
    private Logger logger = LoggerFactory.getLogger(JavaAgentInstrumentationWeaver.class);
    private static final String JAVA_AGENT_CLASS_NAME = "com.egb.sauron.agent.SauronAgent";
    Instrumentation inst = null;
    public JavaAgentInstrumentationWeaver(){
        getInstrumentation();
        if(inst == null) {
            throw new IllegalStateException("Java Agent is not found!");
        }
    }
    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        if(inst == null){
            throw new IllegalStateException("Java Agent Instrumentation is not ready!");
        } else {
            inst.addTransformer(transformer,true);
        }

    }

    @Override
    public ClassLoader getInstrumentableClassLoader() {
        return null;
    }

    private void getInstrumentation() {
        try {
            if(isInstrumentationAvailable()) {
                Class agentClass = null;
                agentClass = Class.forName(JAVA_AGENT_CLASS_NAME);
                Method getInstrumentationMethod = agentClass.getMethod("getInstrumentation", null);
                inst = (Instrumentation) getInstrumentationMethod.invoke(agentClass, null);
                if (inst == null)
                    logger.error("Instrumentation is not functional!");
            } else {
                logger.error("Java Agent is required for Instrumentation.");
            }
        } catch (ClassNotFoundException e) {
            logger.debug("ClassNotFoundException",e);
        } catch (NoSuchMethodException e) {
            logger.debug("NoSuchMethodException",e);
        } catch (IllegalAccessException e) {
            logger.debug("IllegalAccessException",e);
        } catch (InvocationTargetException e) {
            logger.debug("InvocationTargetException",e);
        }
    }

    public static boolean isInstrumentationAvailable(){
        try{
            Class.forName(JAVA_AGENT_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
