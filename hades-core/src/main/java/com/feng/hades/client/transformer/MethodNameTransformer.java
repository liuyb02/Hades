package com.feng.hades.client.transformer;


import com.feng.hades.client.annotations.TraceClass;
import com.feng.hades.client.annotations.TraceMethod;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by Liuyb on 2015/10/8.
 * 实现通过注解方式对加载前的类动态修改
 */
public class MethodNameTransformer implements ClassFileTransformer {
    private Logger logger = LoggerFactory.getLogger(MethodNameTransformer.class);
    private ClassPool classPool = ClassPool.getDefault();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //className包名中以/区分，需要改成以.区分才能被javassist识别
        String fixedClassName = className;
        if (className.indexOf("/") != -1) {
            fixedClassName = className.replaceAll("/", ".");
        }
        try {
            CtClass classToBeModified = classPool.get(fixedClassName);
            TraceClass clazzAnnotation = (TraceClass) classToBeModified.getAnnotation(TraceClass.class);
            //处理类级别的Trace注解
            if (clazzAnnotation != null) {
                addPackageImport();
                addGlobalVariables(classPool, classToBeModified, fixedClassName);
            } else {
                //没有注解的直接跳过
                return null;
            }
            CtMethod[] methods = classToBeModified.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                CtMethod method = methods[i];
                addMethodLocalVariables(method);
                //处理Method级别的Trace注解
                if (method.getAnnotation(TraceMethod.class) != null) {
                    //运行前处理
                    method.insertBefore(HADES_CODE_BEFORE_METHOD_EXECUTE(method.getLongName()));
                    //正常成功后处理
                    method.insertAfter(HADES_CODE_AFTER_METHOD_EXECUTE(), false);
                    //异常捕捉处理
                    method.addCatch(HADES_CODE_CATCH_METHOD_EXECUTE(), classPool.getCtClass("java.lang.Exception"));
                    //catch后的finally段处理
                    method.insertAfter(HADES_CODE_AFTER_METHOD_EXECUTE_FINALLY(), true);

                }
            }
            //返回修改后的class字节码
            return classToBeModified.toBytecode();
        } catch (NotFoundException e) {
//            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addPackageImport() {
        classPool.importPackage("com.feng.hades.tracer.Tracer");
        classPool.importPackage("com.feng.hades.tracer.TracerAdapter");
        classPool.importPackage("com.feng.hades.client.context.SessionContext");
    }

    private void addGlobalVariables(ClassPool pool, CtClass ctClass, String className) throws NotFoundException, CannotCompileException {
        //添加日志logger
        boolean isLoggerFound = false;
        try {
            CtField loggerField = ctClass.getField("logger");
            isLoggerFound = true;
        } catch (NotFoundException e) {
            isLoggerFound = false;
        }
        if (!isLoggerFound) {
            //实现代码>>>> Logger logger = LoggerFactory.getLogger(className);
            CtField newLoggerField = new CtField(pool.getCtClass("org.slf4j.Logger"), "logger", ctClass);
            ctClass.addField(newLoggerField, CtField.Initializer.byExpr("org.slf4j.LoggerFactory.getLogger(\"" + className + "\")"));
        }
    }

    private void addMethodLocalVariables(CtMethod method) throws CannotCompileException, NotFoundException {

        method.addLocalVariable("HADES_LOCAL_CONTEXT", classPool.getCtClass("com.feng.hades.tracer.Tracer"));

    }


    private final String HADES_CODE_BEFORE_METHOD_EXECUTE(String methodName) {
        return  "HADES_LOCAL_CONTEXT = new TracerAdapter(\""+methodName+"\");" +
                "HADES_LOCAL_CONTEXT.beforeMethodExecute();";
    }

    private String HADES_CODE_AFTER_METHOD_EXECUTE() {
        return "HADES_LOCAL_CONTEXT.afterMethodExecute();";
    }

    private String HADES_CODE_CATCH_METHOD_EXECUTE() {
        return  "SessionContext.getCurrentTracerAdapter().catchMethodException($e);" +
                "throw $e;";
    }

    private String HADES_CODE_AFTER_METHOD_EXECUTE_FINALLY() {
        return "SessionContext.getCurrentTracerAdapter().catchMethodExceptionFinally();" ;
    }


}
