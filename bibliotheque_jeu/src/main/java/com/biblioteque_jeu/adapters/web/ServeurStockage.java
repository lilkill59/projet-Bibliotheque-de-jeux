package com.biblioteque_jeu.adapters.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.biblioteque_jeu.domaine.Jeu;
import com.biblioteque_jeu.ports.InterfaceStockageJeu;

public class ServeurStockage {

    private final HttpServer serveur;

    public ServeurStockage(int port, InterfaceStockageJeu stockage) throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

        serveur.createContext("/jeux", exchange -> gererJeux(exchange, stockage));
        serveur.createContext("/taille", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                repondre(exchange, 405, "Méthode non autorisée");
                return;
            }
            repondre(exchange, 200, String.valueOf(stockage.getTaille()));
        });
    }

    public void demarrer() {
        serveur.start();
    }

    public void arreter() {
        serveur.stop(0);
    }

    private void gererJeux(HttpExchange exchange, InterfaceStockageJeu stockage) throws IOException {
        String methode = exchange.getRequestMethod();

        switch (methode) {
            case "GET" -> repondre(exchange, 200, lireJeux(stockage));
            case "POST" -> ajouterDepuisParametres(exchange, stockage);
            case "DELETE" -> supprimerDepuisParametres(exchange, stockage);
            default -> repondre(exchange, 405, "Méthode non autorisée");
        }
    }

    private void ajouterDepuisParametres(HttpExchange exchange, InterfaceStockageJeu stockage) throws IOException {
        String nom = parametre(exchange, "nom");
        String type = parametre(exchange, "type");

        if (nom == null || nom.isBlank() || type == null || type.isBlank()) {
            repondre(exchange, 400, "Les paramètres nom et type sont requis");
            return;
        }

        stockage.ajouterJeu(new Jeu(nom, type));
        repondre(exchange, 201, "Jeu ajouté");
    }

    private void supprimerDepuisParametres(HttpExchange exchange, InterfaceStockageJeu stockage) throws IOException {
        String indexTexte = parametre(exchange, "index");

        try {
            int index = Integer.parseInt(indexTexte);
            stockage.supprimerJeu(index);
            repondre(exchange, 200, "Jeu supprimé");
        } catch (NumberFormatException | NullPointerException e) {
            repondre(exchange, 400, "Le paramètre index doit être un entier");
        }
    }

    private String lireJeux(InterfaceStockageJeu stockage) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < stockage.getTaille(); i++) {
            if (i > 0) {
                json.append(',');
            }
            Jeu jeu = stockage.getJeu(i);
            json.append('{')
                .append("\"index\":").append(i).append(',')
                .append("\"nom\":\"").append(echapperJson(jeu.getNom())).append("\",")
                .append("\"type\":\"").append(echapperJson(jeu.getType())).append("\"}");
        }

        return json.append(']').toString();
    }

    private String parametre(HttpExchange exchange, String nom) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }

        for (String paire : query.split("&")) {
            String[] parties = paire.split("=", 2);
            if (parties.length == 2 && nom.equals(parties[0])) {
                return java.net.URLDecoder.decode(parties[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String echapperJson(String texte) {
        return texte.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private void repondre(HttpExchange exchange, int statut, String contenu) throws IOException {
        byte[] corps = contenu.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statut, corps.length);
        exchange.getResponseBody().write(corps);
        exchange.close();
    }
}
