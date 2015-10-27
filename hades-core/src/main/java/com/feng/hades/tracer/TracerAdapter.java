package com.feng.hades.tracer;

import com.feng.hades.client.context.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Liuyb on 2015/10/12.
 */
public class TracerAdapter implements Tracer {

    private String traceId;
    private String spanId;
    private STATUS status;
    private String methodName;
    private long spanCount = 0;
    private Logger logger = LoggerFactory.getLogger(TracerAdapter.class);
    private Exception exception;
    private static List<Class> tracerList = new LinkedList<Class>();
    private Map<String,Tracer> tracerPool = new HashMap<String, Tracer>();
    private final static ReadWriteLock lock = new ReentrantReadWriteLock();

    private final static String tracerName = "TracerAdapter";
    private final String pkgPrefix = "com.feng.hades.tracer.";

    public String getTracerName(){
        return tracerName;
    }

    private TracerAdapter(String traceId, String spanId, String methodName){
        this.traceId = traceId;
        this.spanId = spanId;
        this.spanCount = 0;
        this.status = STATUS.SUCCESS;
        this.methodName = methodName;
    }

    public TracerAdapter(String methodName){
        if(SessionContext.isTraceEntry()){
            this.traceId = UUID.randomUUID().toString();//构成规则可以修改
            this.spanId = "0";
            this.spanCount = 0;
            this.status = STATUS.SUCCESS;
            this.methodName = methodName;
            SessionContext.initSessionContext(this.traceId);
            SessionContext.addTracerAdapter(this);
        } else {
            this.traceId = SessionContext.getTraceId();
            this.spanId =
                    ((TracerAdapter)SessionContext.getCurrentTracerAdapter()).getSpanId()
                            + "."
                            + ((TracerAdapter)SessionContext.getCurrentTracerAdapter()).getNextSpanCount();
            this.spanCount = 0;
            this.status = STATUS.SUCCESS;
            this.methodName = methodName;
            SessionContext.addTracerAdapter(this);
        }


        for(Class tracerClz: tracerList){
            try {
                Tracer tracer = (Tracer)tracerClz.newInstance();
                tracerPool.put(tracerName,tracer);

            }  catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *
     * @param status
     * @param ex 对于报错需要提供具体exception
     */
    public void setFailedStatus(STATUS status,Exception ex){
        this.status = status;
        this.exception = ex;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getNextSpanCount(){
        return ""+(++spanCount);
    }

    public void beforeMethodExecute() {
        lock.readLock().lock();
        for(String tracerName: tracerPool.keySet()){
            tracerPool.get(tracerName).beforeMethodExecute();
        }
    }

    public void afterMethodExecute() {
        for(String tracerName: tracerPool.keySet()){
            tracerPool.get(tracerName).afterMethodExecute();
        }
    }

    public void catchMethodException(Exception ex) {
        for(String tracerName: tracerPool.keySet()){
            tracerPool.get(tracerName).catchMethodException(ex);
        }
        setFailedStatus(STATUS.FAILED,ex);
    }

    public void catchMethodExceptionFinally() {
        for(String tracerName: tracerPool.keySet()){
            tracerPool.get(tracerName).catchMethodExceptionFinally();
        }
        lock.readLock().unlock();
        logger.info(printTraceLog());
        SessionContext.removeTracerAdapter();
    }

    public String printTraceLog() {
        StringBuffer traceLog = new StringBuffer();
        traceLog.append("HADES: TRACEID = ")
                .append(getTraceId())
                .append(" SPANID = ")
                .append(getSpanId())
                .append(" ")
                .append(methodName);
        if (status.equals(STATUS.SUCCESS)) {
            traceLog.append(" invoke Successfully!");
        } else {
            traceLog.append(" invoke failed!")
                    .append(" Due To Exception ")
                    .append(exception);
        }
        //添加其他tracer的输出
        for(String tracerName: tracerPool.keySet()){
            traceLog.append(tracerPool.get(tracerName).printTraceLog());
        }
        return traceLog.toString();
    }


    public static void setTracerList(List<Class> tracers) {
        lock.writeLock().lock();
        tracerList = tracers;
        lock.writeLock().unlock();
    }

    public enum STATUS{
        SUCCESS,FAILED
    }

}
