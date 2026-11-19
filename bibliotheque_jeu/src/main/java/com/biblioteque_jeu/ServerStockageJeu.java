package com.biblioteque_jeu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.biblioteque_jeu.adapters.memoire.StockageJeuMemoire;
import com.biblioteque_jeu.domaine.Jeu;
import com.biblioteque_jeu.ports.InterfaceStockageJeu;

public class ServerStockageJeu {

    private final HttpServer serveur;

    public ServerStockageJeu(int port, InterfaceStockageJeu stockage) throws IOException {
        serveur = HttpServer.create(new InetSocketAddress(port), 0);
        serveur.createContext("/jeux", exchange -> gererJeux(exchange, stockage));
        serveur.createContext("/taille", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                repondreJson(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
                return;
            }
            repondreJson(exchange, 200,
                    "{\"taille\":" + stockage.getTaille() + "}");
        });
    }

    public void demarrer() {
        serveur.start();
    }

    public void arreter() {
        serveur.stop(0);
    }

    public static void main(String[] args) throws Exception {
        InterfaceStockageJeu stockage = new StockageJeuMemoire();

        stockage.ajouterJeu(new Jeu("J1", "Minecraft", "Sandbox"));
        stockage.ajouterJeu(new Jeu("J2", "Catan", "Jeu de société"));
        stockage.ajouterJeu(new Jeu("J3", "UNO", "Cartes"));
        stockage.ajouterJeu(new Jeu("J4", "The Legend of Zelda: Breath of the Wild", "Action-aventure"));
        stockage.ajouterJeu(new Jeu("J5", "Hades", "Roguelike"));
        stockage.ajouterJeu(new Jeu("J6", "Stardew Valley", "Simulation"));
        stockage.ajouterJeu(new Jeu("J7", "Super Mario Odyssey", "Plateforme"));

        ServerStockageJeu serveur = new ServerStockageJeu(8081, stockage);
        serveur.demarrer();

        System.out.println("Serveur des jeux démarré sur http://127.0.0.1:8081");
        System.out.println("GET    /jeux");
        System.out.println("GET    /taille");
        System.out.println("POST   /jeux?id=J8&nom=Nom&type=Type");
        System.out.println("DELETE /jeux?id=J8");

        Runtime.getRuntime().addShutdownHook(new Thread(serveur::arreter));
        Thread.currentThread().join();
    }

    private void gererJeux(HttpExchange exchange, InterfaceStockageJeu stockage)
            throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            repondreJson(exchange, 200, jeuxEnJson(stockage));
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            String id = parametre(exchange, "id");
            String nom = parametre(exchange, "nom");
            String type = parametre(exchange, "type");

            try {
                if (id == null || id.isBlank() || nom == null || nom.isBlank()
                        || type == null || type.isBlank()) {
                    throw new IllegalArgumentException("Les paramètres id, nom et type sont requis");
                }
                Jeu jeu = new Jeu(id, nom, type);
                stockage.ajouterJeu(jeu);
                repondreJson(exchange, 201,
                        "{\"id\":\"" + echapperJson(id)
                        + "\",\"message\":\"Jeu créé\"}");
            } catch (IllegalArgumentException e) {
                repondreJson(exchange, 400,
                        "{\"erreur\":\"" + echapperJson(e.getMessage()) + "\"}");
            }
            return;
        }

        if ("DELETE".equals(exchange.getRequestMethod())) {
            String id = parametre(exchange, "id");
            if (stockage.supprimerJeuParId(id)) {
                repondreJson(exchange, 200, "{\"message\":\"Jeu supprimé\"}");
            } else {
                repondreJson(exchange, 404, "{\"erreur\":\"Jeu introuvable\"}");
            }
            return;
        }

        repondreJson(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
    }

    private String jeuxEnJson(InterfaceStockageJeu stockage) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < stockage.getTaille(); i++) {
            if (i > 0) {
                json.append(',');
            }
            Jeu jeu = stockage.getJeu(i);
            json.append("{\"id\":\"").append(echapperJson(jeu.getId()))
                .append("\",\"nom\":\"").append(echapperJson(jeu.getNom()))
                .append("\",\"type\":\"").append(echapperJson(jeu.getType()))
                .append("\"}");
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
                return URLDecoder.decode(parties[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String echapperJson(String texte) {
        if (texte == null) {
            return "";
        }
        return texte.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private void repondreJson(HttpExchange exchange, int statut, String contenu)
            throws IOException {
        byte[] corps = contenu.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statut, corps.length);
        exchange.getResponseBody().write(corps);
        exchange.close();
    }
}