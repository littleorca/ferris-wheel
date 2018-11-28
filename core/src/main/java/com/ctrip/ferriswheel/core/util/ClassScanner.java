package com.ctrip.ferriswheel.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

public class ClassScanner {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("com/ctrip/ferriswheel/");
        while (resources.hasMoreElements()) {
            URL res = resources.nextElement();
            System.out.println(res);
            if (res.getProtocol().equals("file")) {
                scanFile(new File(res.toURI()));
            } else if (res.getProtocol().equals("jar")) {
                scanJar(res);
            }
        }
    }

    private static void scanFile(File parent) throws IOException {
        File[] files = parent.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scanFile(f);
            } else {
                System.out.println("File => "+f.getCanonicalPath());
            }
        }
    }

    private static void scanJar(URL res) {

    }
}
