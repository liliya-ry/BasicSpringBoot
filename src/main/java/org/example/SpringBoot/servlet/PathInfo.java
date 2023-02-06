package org.example.SpringBoot.servlet;

import java.util.regex.Matcher;

import static org.example.SpringBoot.servlet.MappingsContainer.*;

public class PathInfo {
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
}
