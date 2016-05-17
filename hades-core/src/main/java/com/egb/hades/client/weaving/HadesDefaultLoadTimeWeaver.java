package com.egb.hades.client.weaving;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;

/**
 * Created by Liuyb on 2015/11/20.
 */
public class HadesDefaultLoadTimeWeaver implements HadesLoadTimeWeaver {
    private Logger logger = LoggerFactory.getLogger(HadesDefaultLoadTimeWeaver.class);
    private HadesLoadTimeWeaver loadTimeWeaver;
    private ClassLoader classLoader = null;
    public HadesDefaultLoadTimeWeaver(ClassLoader loader){
        if(loader == null) throw new IllegalArgumentException("ClassLoader Can't be null!");
        classLoader = loader;
        if(JavaAgentInstrumentationWeaver.isInstrumentationAvailable()){
            loadTimeWeaver = new JavaAgentInstrumentationWeaver();
        } else {
            /**
             * 如果没法找到JavaAgent就使用具体服务器classLoader的方法
             */
            loadTimeWeaver = createServerSpecificLoadTimeWeaver(loader);
        }
        if(loadTimeWeaver == null){
            logger.error("Can't initialize Sauron Load Time Weaver!");
        }
    }
    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        try {
            if(loadTimeWeaver == null)
                throw new IllegalStateException("LoadTime Weaver is not initialized!");

            loadTimeWeaver.addTransformer(transformer);
        } catch (Exception e){
            logger.error("Add Transformer to Sauron Load Time Weaver Failed!");
        }

    }

    @Override
    public ClassLoader getInstrumentableClassLoader() {
        return classLoader;
    }

    /*
 * This method never fails, allowing to try other possible ways to use an
 * server-agnostic weaver. This non-failure logic is required since
 * determining a load-time weaver based on the ClassLoader name alone may
 * legitimately fail due to other mismatches. Specific case in point: the
 * use of WebLogicLoadTimeWeaver works for WLS 10 but fails due to the lack
 * of a specific method (addInstanceClassPreProcessor) for any earlier
 * versions even though the ClassLoader name is the same.
 */
    protected HadesLoadTimeWeaver createServerSpecificLoadTimeWeaver(ClassLoader classLoader) {
        String name = classLoader.getClass().getName();
        try {
            if (name.startsWith("weblogic")) {
//                return new WebLogicLoadTimeWeaver(classLoader);
            }
            else if (name.startsWith("org.glassfish")) {
                return new GlassFishClassLoaderWeaver(classLoader);
            }
            else if (name.startsWith("org.apache.catalina") || name.startsWith("com.egb.sauron")) {
                return new TomcatClassLoaderWeaver(classLoader);
            }
            else if (name.startsWith("org.jboss")) {
//                return new JBossLoadTimeWeaver(classLoader);
            }
            else if (name.startsWith("com.ibm")) {
//                return new WebSphereLoadTimeWeaver(classLoader);
            }
        }
        catch (IllegalStateException ex) {
            logger.info("Could not obtain server-specific LoadTimeWeaver: " + ex.getMessage());
        }
        return null;
    }
}
