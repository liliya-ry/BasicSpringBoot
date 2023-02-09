package logging;

import SpringBoot.interceptors.*;
import jakarta.servlet.http.*;
import SpringContainer.annotations.beans.Component;
import org.slf4j.*;

@Component
public class LoggingInterceptor extends HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null)
            LOGGER.error(ex.toString());

        long endTime = System.currentTimeMillis();
        long startTime = (long) request.getAttribute("startTime");
        long execTime = endTime - startTime;

        int status = response.getStatus();
        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        String user = (String) request.getAttribute("user");
        String logMsg = String.format("User: %s, Method: %s, URI: %s, Status: %s, Execution Time: %s ms", user, httpMethod, uri, status, execTime);

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoggingInfo loggingInfoAnn = (LoggingInfo) handlerMethod.getMethodAnnotation(LoggingInfo.class);
        if (loggingInfoAnn != null)
            logMsg = String.format(loggingInfoAnn.value(), logMsg);

        LOGGER.info(logMsg);
    }
}
