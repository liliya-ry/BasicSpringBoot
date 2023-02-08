package org.example.SpringFramework.SpringBoot.servlet;

import java.util.regex.*;

public class PathInfo {
    static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([\\w\\d]+)}");
    String[] pathParts;
    String[] paramNames;

    PathInfo(String requestPath) {
        pathParts = requestPath.split("/");
        paramNames = new String[pathParts.length];

        for (int i = 0; i < pathParts.length; i++) {
            Matcher matcher = VARIABLE_PATTERN.matcher(pathParts[i]);
            paramNames[i] = matcher.find() ? matcher.group(1) : null;
        }
    }

    String getMethodType() {
        return pathParts[0];
    }
}
