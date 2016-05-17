package com.egb.hades.client.dynamicloader;

import com.egb.hades.tracer.TracerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Liuyb on 2015/10/22.
 */
public class TracerLoader implements Runnable {
    private static Logger log = LoggerFactory.getLogger(TracerLoader.class);
    private static  String TRACER_JAR_PATH = "D:\\workspace\\egb-hades\\out\\artifacts\\hades_tracer_jar\\hades-tracer.jar";
    private static int TRACER_REFRESH_INTERVAL = 0;
    CountDownLatch latch = null;
    long lastModifiedTime = 0;
    static {
        Properties props = new Properties();
        try {
            InputStream inputStream = TracerLoader.class.getClassLoader().getResourceAsStream("\\hades-config.properties");
            if(inputStream == null)
                throw new FileNotFoundException("hades-config.properties Is Not Found!");
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TRACER_JAR_PATH = props.getProperty("TRACER_JAR_PATH");
        if(TRACER_JAR_PATH == null)
            throw new IllegalArgumentException("Tracer Jar Path is Not Available");
        TRACER_REFRESH_INTERVAL = Integer.valueOf(props.getProperty("TRACER_REFRESH_INTERVAL"));

    }
    public void run() {
        while(true) {
            updateTracerFromJar();
        }
    }
    private void updateTracerFromJar(){
        try {
            log.debug("#####################Loading Tracer Jar File################################");
            List<Class> tracerList = new LinkedList<Class>();
            File file = new File(TRACER_JAR_PATH);
            Long modTime = file.lastModified();
            if (modTime > lastModifiedTime) {
                JarFile jarFile = new JarFile(file);
                URL url = new URL("file:" + TRACER_JAR_PATH);
                URLClassLoader loader = new URLClassLoader(new URL[]{url});

                Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();
                    String name = entry.getName();
                    if (name != null && name.endsWith(".class")) {
                        Class clazz = loader.loadClass(name.replaceAll("/", ".").substring(0, name.length() - 6));
                        if(name.indexOf("$") == -1)
                            tracerList.add(clazz);
                    }

                }
                TracerAdapter.setTracerList(tracerList);
                log.debug("#####################"+tracerList.size()+" Tracers is Loaded!################################");

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //等待一定时间后重新刷新
                Thread.sleep(TRACER_REFRESH_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
