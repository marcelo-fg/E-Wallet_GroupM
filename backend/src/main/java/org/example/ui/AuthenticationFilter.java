package org.example.ui;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"*.xhtml"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Rien à initialiser pour l'instant
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestURI  = req.getRequestURI();

        // 1) Laisser passer les ressources statiques (CSS, JS, images, etc.)
        boolean resourceRequest =
                requestURI.startsWith(contextPath + "/javax.faces.resource") ||
                        requestURI.startsWith(contextPath + "/resources/");

        if (resourceRequest) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Vérifier l'authentification
        HttpSession session = req.getSession(false);
        boolean sessionExists = (session != null);
        boolean validRequest =
                sessionExists &&
                        session.getAttribute("uuid") != null &&
                        session.getAttribute("username") != null;

        // 3) URLs publiques (login / register)
        String loginURI    = contextPath + "/Login.xhtml";
        String registerURI = contextPath + "/Register.xhtml";

        boolean loginRequest    = requestURI.equals(loginURI);
        boolean registerRequest = requestURI.equals(registerURI);

        // Réinitialiser la session si on arrive sur login ou register
        if (loginRequest || registerRequest) {
            LoginBean.invalidateSession();
        }

        // 4) Logique finale : laisser passer ou rediriger
        if (validRequest || loginRequest || registerRequest) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(loginURI);
        }
    }

    @Override
    public void destroy() {
        // Rien à nettoyer pour l'instant
    }
}