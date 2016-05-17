package com.egb.hades.client.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by Liuyb on 2015/10/8.
 * 该类负责在Java虚拟机加载时通过自定义的ClassFileTransformer修改类方法，增加相应的监控方法
 */
public class HadesPreProcessAdapter implements ClassFileTransformer {
    private Logger logger = LoggerFactory.getLogger(HadesPreProcessAdapter.class);
    private final ClassFileTransformer classFileTransformer;

    public HadesPreProcessAdapter(ClassFileTransformer classFileTransformer) {
        assert classFileTransformer != null;
        this.classFileTransformer = classFileTransformer;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            byte[] transformed = classFileTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            if (transformed != null && logger.isDebugEnabled()) {
                logger.debug("Transformer of class [" + this.classFileTransformer.getClass().getName() +
                        "] transformed class [" + className + "]; bytes in=" +
                        classfileBuffer.length + "; bytes out=" + transformed.length);
            }
            return transformed;
        } catch (IllegalClassFormatException e) {
            logger.warn("Error weaving class [" + className + "] with " +
                    "transformer of class [" + this.classFileTransformer.getClass().getName() + "]", e);
            e.printStackTrace();
        }


        return null;
    }
}
