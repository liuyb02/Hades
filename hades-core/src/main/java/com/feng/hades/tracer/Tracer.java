package com.feng.hades.tracer;

/**
 * Created by Liuyb on 2015/10/13.
 */
public interface Tracer {
    String getTracerName();
    void beforeMethodExecute();
    void afterMethodExecute();
    void catchMethodException(Exception ex);
    void catchMethodExceptionFinally();
    String printTraceLog();
}
