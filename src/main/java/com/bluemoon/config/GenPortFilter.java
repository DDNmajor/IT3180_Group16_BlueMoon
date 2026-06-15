package com.bluemoon.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class GenPortFilter implements Filter {

    @Value("${bluemoon.internal.port:8081}")
    private int internalPort;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (req.getRequestURI().startsWith("/thu-ho/gen")) {
            if (req.getLocalPort() != internalPort) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
