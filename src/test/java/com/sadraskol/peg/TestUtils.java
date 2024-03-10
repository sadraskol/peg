package com.sadraskol.peg;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {
    public static String readFile(String filename) {
        try {
            URL resource = getUrl(filename);
            var fileUri = resource.toURI();
            return Files.readString(Path.of(fileUri));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot find file " + filename + " because of " + exception.getMessage(), exception);
        }
    }

    private static URL getUrl(String name) {
        URL resource = TestUtils.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Cannot find file " + name + " in current class loader");
        }
        return resource;
    }
}
