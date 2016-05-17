package com.egb.hades.client.weaving;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Liuyb on 2015/11/20.
 */
public class TomcatClassLoaderWeaver implements HadesLoadTimeWeaver {

    private static final String INSTRUMENTABLE_LOADER_CLASS_NAME = "org.apache.tomcat.InstrumentableClassLoader";
    ClassLoader classLoader = null;
    Method addTransformerMethod = null;
    public TomcatClassLoaderWeaver(ClassLoader instrumentableLoader){
        if(instrumentableLoader == null) throw new IllegalArgumentException("ClassLoader can't be null!");
        this.classLoader = instrumentableLoader;

        Class<?> instrumentableLoaderClass;
        try {
            instrumentableLoaderClass = classLoader.loadClass(INSTRUMENTABLE_LOADER_CLASS_NAME);
            if (!instrumentableLoaderClass.isInstance(classLoader)) {
                // Could still be a custom variant of a convention-compatible ClassLoader
                instrumentableLoaderClass = classLoader.getClass();
            }
        }
        catch (ClassNotFoundException ex) {
            // We're on an earlier version of Tomcat, probably with Sauron's TomcatInstrumentableClassLoader
            instrumentableLoaderClass = classLoader.getClass();
        }

        try {
            this.addTransformerMethod = instrumentableLoaderClass.getMethod("addTransformer", ClassFileTransformer.class);
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not initialize TomcatLoadTimeWeaver because Tomcat API classes are not available", ex);
        }
    }
    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        try {
            this.addTransformerMethod.invoke(this.classLoader, transformer);
        }
        catch (InvocationTargetException ex) {
            throw new IllegalStateException("Tomcat addTransformer method threw exception", ex.getCause());
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not invoke Tomcat addTransformer method", ex);
        }
    }

    @Override
    public ClassLoader getInstrumentableClassLoader() {
        return this.classLoader;
    }
}
