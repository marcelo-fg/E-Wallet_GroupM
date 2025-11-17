package org.example.webservice;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class ApiConnector extends Application {
    // Rien à ajouter ici : Jakarta découvre automatiquement tes ressources REST
}