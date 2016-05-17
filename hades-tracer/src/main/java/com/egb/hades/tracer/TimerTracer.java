package com.egb.hades.tracer;

/**
 * Created by Liuyb on 2015/10/13.
 */
public class TimerTracer implements Tracer {
    private final static String tracerName = "TimerTracer";
    private Timer timer = new Timer();


    public TimerTracer(){
        this.timer.init();
    }

    public String getTracerName() {
        return tracerName;
    }

    public void beforeMethodExecute() {
        timer.startTimer();
    }

    public void afterMethodExecute() {

    }

    public void catchMethodException(Exception ex) {

    }

    public void catchMethodExceptionFinally() {
        timer.stopTimer();
    }


    public String printTraceLog() {
        StringBuffer traceLog = new StringBuffer();
        traceLog.append("  Duration :")
                .append(getTimer().getDuration())
                .append(" ms");
        return traceLog.toString();
    }

    public Timer getTimer(){
        return timer;
    }

    public class Timer{
        private long startTime;
        private long endTime;
        public void startTimer(){
            this.startTime = System.currentTimeMillis();
            this.endTime = 0;
        }
        public void stopTimer(){
            this.endTime = System.currentTimeMillis();
        }
        public long getDuration(){
            return endTime - startTime;
        }

        public void init(){
            this.startTime = 0;
            this.endTime = 0;
        }
    }
}
