package com.egb.hades.client.listener;

import com.egb.hades.client.transformer.HadesPreProcessAdapter;
import com.egb.hades.client.transformer.HadesTransformerFactory;
import com.egb.hades.client.transformer.TransformerType;
import com.egb.hades.client.weaving.HadesDefaultLoadTimeWeaver;
import com.egb.hades.client.weaving.HadesLoadTimeWeaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.instrument.ClassFileTransformer;

/**
 * Created by Liuyb on 2015/11/19.
 */
public class HadesLoadTimeWeavingListener implements ServletContextListener {
    private Logger logger = LoggerFactory.getLogger(HadesLoadTimeWeavingListener.class);
    private ClassLoader classLoader = null;

    public HadesLoadTimeWeavingListener(){
        this(Thread.currentThread().getContextClassLoader());
    }

    public HadesLoadTimeWeavingListener(ClassLoader loader){
        this.classLoader = loader;
    }
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            HadesLoadTimeWeaver loadTimeWeaver = new HadesDefaultLoadTimeWeaver(classLoader);
            loadTimeWeaver.addTransformer(new HadesPreProcessAdapter(getClassFileTransformer()));
        } catch (Exception ex) {
            logger.debug("Sauron is Not Weaving into Host Application ! Skip Processing!");
            logger.info(ex.getMessage());
        }
//        JvmMonitor.run();
    }

    private ClassFileTransformer getClassFileTransformer(){
        ClassFileTransformer innerTransformer = null;
        innerTransformer = HadesTransformerFactory.getTransformer(TransformerType.ANNOTATION);
        return innerTransformer;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
//        WatchableConfigClient.close();
//        logger.debug("ConfigClient Closed!");
    }
}
