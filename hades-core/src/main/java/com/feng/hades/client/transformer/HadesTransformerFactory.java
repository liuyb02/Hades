package com.feng.hades.client.transformer;

import java.lang.instrument.ClassFileTransformer;

/**
 * Created by Liuyb on 2015/10/12.
 */
public class HadesTransformerFactory {
    public static ClassFileTransformer getTransformer(TransformerType type){
        switch(type){
            case ANNOTATION: return new AnnotationTransformer();
            case METHODNAME: return new MethodNameTransformer();
        }
        return null;
    }

}
