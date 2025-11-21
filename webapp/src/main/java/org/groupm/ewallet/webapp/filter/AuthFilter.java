package org.groupm.ewallet.webapp.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthFilter",
        urlPatterns = {"/dashboard.xhtml", "/portfolio.xhtml", "/transactions.xhtml"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("userEmail") != null);

        String loginURI = req.getContextPath() + "/login.xhtml";

        if (loggedIn || req.getRequestURI().equals(loginURI)) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(loginURI);
        }
    }
}