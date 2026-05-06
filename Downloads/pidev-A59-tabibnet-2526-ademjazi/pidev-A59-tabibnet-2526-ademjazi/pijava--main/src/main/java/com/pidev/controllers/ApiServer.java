package com.pidev.controllers;

import com.pidev.models.Feedback;
import com.pidev.models.RendezVous;
import com.pidev.services.RendezVousService;
import com.pidev.services.FeedbackService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiServer {
    private static HttpServer server;
    private static RendezVousService rdvService = new RendezVousService();
    private static FeedbackService feedbackService = new FeedbackService();

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(8085), 0);
            server.createContext("/api/rendezvous", new RendezVousHandler());
            server.createContext("/api/rendezvous/cancel", new CancelRdvHandler());
            server.createContext("/api/rendezvous/status", new StatusRdvHandler());
            server.createContext("/api/feedback", new FeedbackHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Le Mini Serveur Web (API Java) a demarre sur http://localhost:8085");
        } catch (IOException e) {
            System.out.println("Erreur au demarrage du serveur : " + e.getMessage());
        }
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class RendezVousHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> p = parseFormData(exchange.getRequestBody());
                String nom = p.get("nomPatient");
                String medecin = p.get("medecin");
                String date = p.get("date");
                String heure = p.get("heure");

                if (nom != null && date != null && heure != null && medecin != null) {
                    RendezVous rdv = new RendezVous();
                    rdv.setNomPatient(nom);
                    rdv.setMedecin(medecin);
                    rdv.setDate(date);
                    rdv.setHeure(heure);
                    rdv.setStatut("En attente");
                    try {
                        rdvService.ajouter(rdv);
                        sendRes(exchange, 200, "{\"success\":true,\"message\":\"Rendez-vous cree avec succes\"}");
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                        sendRes(exchange, 500, "{\"success\":false,\"error\":\"Erreur SQL\"}");
                    }
                } else {
                    sendRes(exchange, 400, "{\"success\":false,\"error\":\"Donnees manquantes\"}");
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                StringBuilder json = new StringBuilder("[");
                for (RendezVous r : rdvService.getAll()) {
                    boolean match = false;
                    if (query != null && query.contains("nomPatient=")) {
                        String searchedName = getQueryParam(query, "nomPatient");
                        if (r.getNomPatient().toLowerCase().contains(searchedName.toLowerCase())) match = true;
                    } else if (query != null && query.contains("medecin=")) {
                        String searchedMedecin = getQueryParam(query, "medecin");
                        if (r.getMedecin().toLowerCase().contains(searchedMedecin.toLowerCase())) match = true;
                    } else if (query == null) {
                        match = true;
                    }
                    
                    if(match) {
                        json.append(String.format("{\"id\":%d,\"nomPatient\":\"%s\",\"medecin\":\"%s\",\"date\":\"%s\",\"heure\":\"%s\",\"statut\":\"%s\"},",
                                r.getId(), r.getNomPatient(), r.getMedecin(), r.getDate(), r.getHeure(), r.getStatut()));
                    }
                }
                if (json.length() > 1) json.setLength(json.length() - 1);
                json.append("]");
                sendRes(exchange, 200, json.toString());
            } else {
                sendRes(exchange, 405, "{}");
            }
        }
    }

    static class CancelRdvHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> p = parseFormData(exchange.getRequestBody());
                int id = Integer.parseInt(p.getOrDefault("id", "0"));

                if (id > 0) {
                    RendezVous r = rdvService.findById(id);
                    if(r != null) {
                        try {
                            rdvService.supprimer(r);
                            sendRes(exchange, 200, "{\"success\":true,\"message\":\"Rendez-vous annulé et supprimé de la BD\"}");
                        } catch (java.sql.SQLException e) {
                            e.printStackTrace();
                            sendRes(exchange, 500, "{\"success\":false,\"error\":\"Erreur SQL\"}");
                        }
                    } else {
                        sendRes(exchange, 404, "{\"success\":false,\"error\":\"RDV introuvable\"}");
                    }
                } else {
                    sendRes(exchange, 400, "{\"success\":false,\"error\":\"ID invalide\"}");
                }
            } else {
                sendRes(exchange, 405, "{}");
            }
        }
    }

    static class StatusRdvHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> p = parseFormData(exchange.getRequestBody());
                int id = Integer.parseInt(p.getOrDefault("id", "0"));
                String statut = p.get("statut");

                if (id > 0 && statut != null) {
                    RendezVous r = rdvService.findById(id);
                    if(r != null) {
                        r.setStatut(statut);
                        try {
                            rdvService.modifier(r);
                            sendRes(exchange, 200, "{\"success\":true,\"message\":\"Statut mis à jour !\"}");
                        } catch (java.sql.SQLException e) {
                            e.printStackTrace();
                            sendRes(exchange, 500, "{\"success\":false,\"error\":\"Erreur SQL\"}");
                        }
                    } else {
                        sendRes(exchange, 404, "{\"success\":false,\"error\":\"RDV introuvable\"}");
                    }
                } else {
                    sendRes(exchange, 400, "{\"success\":false,\"error\":\"ID/Statut invalide\"}");
                }
            } else {
                sendRes(exchange, 405, "{}");
            }
        }
    }

    static class FeedbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> p = parseFormData(exchange.getRequestBody());
                String commentaire = p.get("commentaire");
                int note = Integer.parseInt(p.getOrDefault("note", "0"));
                int rdvId = Integer.parseInt(p.getOrDefault("rendezVousId", "0"));

                if (commentaire != null && note > 0 && rdvId > 0) {
                    javafx.collections.ObservableList<Feedback> existingList = feedbackService.getByRendezVous(rdvId);
                    
                    if(!existingList.isEmpty()) {
                        // Un avis existe déjà pour ce RDV : on le remplace forcément (mise à jour)
                        Feedback existing = existingList.get(0);
                        feedbackService.modifier(existing, commentaire, note, rdvId);
                        sendRes(exchange, 200, "{\"success\":true,\"message\":\"L'avis existant a ete ecrase et mis a jour\"}");
                    } else {
                        // Aucun avis n'existe : on l'ajoute
                        feedbackService.ajouter(new Feedback(0, commentaire, note, rdvId));
                        sendRes(exchange, 200, "{\"success\":true,\"message\":\"Nouveau feedback soumis avec succes\"}");
                    }
                } else {
                    sendRes(exchange, 400, "{\"success\":false,\"error\":\"Donnees invalides\"}");
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                StringBuilder json = new StringBuilder("[");
                if (query != null && query.contains("rdvId=")) {
                    int rdvIdQuery = Integer.parseInt(getQueryParam(query, "rdvId"));
                    for (Feedback f : feedbackService.getByRendezVous(rdvIdQuery)) {
                        json.append(String.format("{\"id\":%d,\"commentaire\":\"%s\",\"note\":%d,\"rendezVousId\":%d},",
                                f.getId(), f.getCommentaire().replace("\"", "\\\""), f.getNote(), f.getRendezVousId()));
                    }
                }
                if (json.length() > 1) json.setLength(json.length() - 1);
                json.append("]");
                sendRes(exchange, 200, json.toString());
            } else {
                sendRes(exchange, 405, "{}");
            }
        }
    }

    private static String getQueryParam(String query, String param) {
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length > 1 && kv[0].equals(param)) return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
        }
        return "";
    }

    private static Map<String, String> parseFormData(InputStream is) throws IOException {
        String data = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : data.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length > 1) {
                map.put(java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static void sendRes(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
