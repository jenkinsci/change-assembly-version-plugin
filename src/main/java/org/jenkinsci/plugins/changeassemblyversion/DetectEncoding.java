package org.jenkinsci.plugins.changeassemblyversion;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * Created by jens on 5/3/16.
 */
public class DetectEncoding {
    public static void main(String[] args) throws java.io.IOException {
        byte[] buf = new byte[4096];
        String fileName = args[0];
        java.io.FileInputStream fis = new java.io.FileInputStream(fileName);

        // (1)
        UniversalDetector detector = new UniversalDetector(null);

        // (2)
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
        }

        // (5)
        detector.reset();
    }
}

}
