package demo;

import javax.servlet.http.HttpServletRequest;

public interface RateLimiter {
    boolean permitRequest(HttpServletRequest request);
}
