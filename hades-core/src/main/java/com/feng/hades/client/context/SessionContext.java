package com.feng.hades.client.context;

import com.feng.hades.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * Created by Liuyb on 2015/10/12.
 */
public class SessionContext {
    private final static ThreadLocal<Stack<Tracer>> tracers = new ThreadLocal<Stack<Tracer>>();
    private final static ThreadLocal<String> traceId = new ThreadLocal<String>();
    private final static Logger logger = LoggerFactory.getLogger(SessionContext.class);

    public static String getTraceId() {
        return traceId.get();
    }

    public static Tracer getCurrentTracerAdapter() {
        if (tracers.get() == null) {
            throw new IllegalAccessError("Empty Tracer Stack!");
        }
        Stack stack = tracers.get();
        return tracers.get().peek();
    }

    public static void addTracerAdapter(Tracer tracer) {
        if (tracers.get() == null) {
            throw new IllegalAccessError("Empty Tracer Stack!");
        }
//        logger.debug("add client!" +"SPANID:"+ tracer.getSpanId());
        Stack stack = tracers.get();
        tracers.get().push(tracer);
    }

    public static void removeTracerAdapter() {
        if (tracers.get() == null) {
            throw new IllegalAccessError("Empty Tracer Stack!");
        }
        Stack stack = tracers.get();
        tracers.get().pop();
    }

    public static boolean isTraceEntry() {
        return tracers.get() == null || tracers.get().size() == 0;
    }

    public static void initSessionContext(String TRACEID) {
//        logger.info("InitSessionContext");
        tracers.set(new Stack<Tracer>());
        traceId.set(TRACEID);
        return;

    }
}
