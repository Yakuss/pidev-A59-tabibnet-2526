module com.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires mysql.connector.j;
    requires jakarta.mail;
    requires org.json;
    requires jdk.httpserver;
    requires java.net.http;
    requires vosk;
    requires java.desktop;
    requires jdk.jsobject;
    requires javafx.web;  
    requires com.github.librepdf.openpdf;
    requires org.apache.pdfbox;
    requires okhttp3;
    requires com.google.gson;

    // Standard controller access
    opens com.pidev.controllers to javafx.fxml;
    opens com.pidev to javafx.fxml;

    // CRITICAL: This allows TableView to read your Patient/BaseUser properties
    opens com.pidev.models to javafx.base, javafx.fxml;

    exports com.pidev;
    exports com.pidev.controllers;
    exports com.pidev.models;
    exports com.pidev.services;
    exports com.pidev.utils;
}