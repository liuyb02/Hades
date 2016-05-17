package com.egb.hades.client.weaving;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Liuyb on 2015/11/20.
 */
public class GlassFishClassLoaderWeaver implements HadesLoadTimeWeaver {
    private static final String INSTRUMENTABLE_LOADER_CLASS_NAME = "org.glassfish.api.deployment.InstrumentableClassLoader";
    private ClassLoader instrumentableLoaderClass;
    private Method addTransformerMethod;

    public GlassFishClassLoaderWeaver(ClassLoader classLoader){
        if(classLoader == null) throw new IllegalArgumentException("ClassLoader can't be null!");
        Class<?> instrumentableLoaderClass;
        try {
            instrumentableLoaderClass = classLoader.loadClass(INSTRUMENTABLE_LOADER_CLASS_NAME);
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "Could not initialize GlassFishLoadTimeWeaver because GlassFish API classes are not available", ex);
        }
        try {
            this.addTransformerMethod = instrumentableLoaderClass.getMethod("addTransformer", ClassFileTransformer.class);
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not initialize GlassFishLoadTimeWeaver because GlassFish API classes are not available", ex);
        }

        ClassLoader clazzLoader = null;
        // Detect transformation-aware ClassLoader by traversing the hierarchy
        // (as in GlassFish, Spring can be loaded by the WebappClassLoader).
        for (ClassLoader cl = classLoader; cl != null && clazzLoader == null; cl = cl.getParent()) {
            if (instrumentableLoaderClass.isInstance(cl)) {
                clazzLoader = cl;
            }
        }

        if (clazzLoader == null) {
            throw new IllegalArgumentException(classLoader + " and its parents are not suitable ClassLoaders: A [" +
                    instrumentableLoaderClass.getName() + "] implementation is required.");
        }

        this.instrumentableLoaderClass = clazzLoader;
    }
    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        try {
            this.addTransformerMethod.invoke(this.instrumentableLoaderClass, transformer);
        }
        catch (InvocationTargetException ex) {
            throw new IllegalStateException("GlassFish addTransformer method threw exception", ex.getCause());
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not invoke GlassFish addTransformer method", ex);
        }
    }

    @Override
    public ClassLoader getInstrumentableClassLoader() {
        return this.instrumentableLoaderClass;
    }
}
