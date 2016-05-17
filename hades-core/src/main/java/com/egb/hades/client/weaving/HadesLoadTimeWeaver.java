package com.egb.hades.client.weaving;

import java.lang.instrument.ClassFileTransformer;

/**
 * Created by Liuyb on 2015/11/20.
 */
public interface HadesLoadTimeWeaver {
    /**
     * Add a {@code ClassFileTransformer} to be applied by this
     * {@code LoadTimeWeaver}.
     * @param transformer the {@code ClassFileTransformer} to add
     */
    void addTransformer(ClassFileTransformer transformer);

    /**
     * Return a {@code ClassLoader} that supports instrumentation
     * through AspectJ-style load-time weaving based on user-defined
     * {@link ClassFileTransformer ClassFileTransformers}.
     * <p>May be the current {@code ClassLoader}, or a {@code ClassLoader}
     * created by this {@link HadesLoadTimeWeaver} instance.
     * @return the {@code ClassLoader} which will expose
     * instrumented classes according to the registered transformers
     */
    ClassLoader getInstrumentableClassLoader();
}
