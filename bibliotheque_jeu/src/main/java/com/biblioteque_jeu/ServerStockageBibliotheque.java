package com.biblioteque_jeu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.biblioteque_jeu.adapters.memoire.StockageBibliothequeMemoire;
import com.biblioteque_jeu.domaine.Emprunt;
import com.biblioteque_jeu.domaine.JeuStocke;
import com.biblioteque_jeu.ports.InterfaceStockageBibliotheque;

public class ServerStockageBibliotheque {

    private final HttpServer serveur;

    public ServerStockageBibliotheque(int port, InterfaceStockageBibliotheque stockage)
            throws IOException {
        serveur = HttpServer.create(new InetSocketAddress(port), 0);
        serveur.createContext("/", exchange -> gererPage(exchange));
        serveur.createContext("/bibliotheque", exchange -> gererGetListe(exchange, stockage));
        serveur.createContext("/emprunts", exchange -> gererEmprunts(exchange, stockage));
        serveur.createContext("/gestion", exchange -> gererPageGestion(exchange));
        serveur.createContext("/gestion-stock", exchange -> gererGestionStock(exchange, stockage));
        serveur.createContext("/catalogue", exchange -> gererCatalogue(exchange));
    }

    public void demarrer() {
        serveur.start();
    }

    public void arreter() {
        serveur.stop(0);
    }

    public static void main(String[] args) throws Exception {
        InterfaceStockageBibliotheque stockage = new StockageBibliothequeMemoire();
        stockage.creerJeu("J1", "Minecraft", "Sandbox", 3);
        stockage.creerJeu("J2", "Catan", "Jeu de société", 2);
        stockage.creerJeu("J3", "UNO", "Cartes", 4);
        stockage.creerJeu("J4", "The Legend of Zelda: Breath of the Wild", "Action-aventure", 1);
        stockage.creerJeu("J5", "Hades", "Roguelike", 2);
        stockage.creerJeu("J6", "Stardew Valley", "Simulation", 3);
        stockage.creerJeu("J7", "Celeste", "Plateforme", 2);
        stockage.creerJeu("J8", "Among Us", "Social Deduction", 4);

        ServerStockageBibliotheque serveur = new ServerStockageBibliotheque(8080, stockage);
        serveur.synchroniserJeu("POST", "J1", "Minecraft", "Sandbox");
        serveur.synchroniserJeu("POST", "J2", "Catan", "Jeu de société");
        serveur.synchroniserJeu("POST", "J3", "UNO", "Cartes");
        serveur.synchroniserJeu("POST", "J4", "The Legend of Zelda: Breath of the Wild", "Action-aventure");
        serveur.synchroniserJeu("POST", "J5", "Hades", "Roguelike");
        serveur.synchroniserJeu("POST", "J6", "Stardew Valley", "Simulation");
        serveur.synchroniserJeu("POST", "J7", "Celeste", "Plateforme");
        serveur.synchroniserJeu("POST", "J8", "Among Us", "Social Deduction");
        serveur.demarrer();

        System.out.println("Interface disponible sur http://localhost:8080");
        Runtime.getRuntime().addShutdownHook(new Thread(serveur::arreter));
        Thread.currentThread().join();
    }

    private void gererPage(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())
                || !"/".equals(exchange.getRequestURI().getPath())) {
            repondreHtml(exchange, 404, "Page introuvable");
            return;
        }

        String page = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Bibliothèque de jeux</title>
                <style>
                    :root { color-scheme: dark; }
                    * { box-sizing: border-box; }
                    body { margin: 0; font-family: system-ui, sans-serif; background: #101827; color: #f7f8fc; }
                    main { max-width: 1000px; margin: auto; padding: 32px 20px; }
                    .grille { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; }
                    .jeu { background: #1d293d; border: 1px solid #34435b; border-radius: 12px; padding: 18px; }
                    .jeu h2 { margin: 0 0 8px; }
                    .stock { color: #70d6a5; font-weight: 700; }
                    input, button { width: 100%; font: inherit; }
                    input { margin-top: 6px; padding: 10px; border: 1px solid #596984; border-radius: 8px; background: #111827; color: white; }
                    button { margin-top: 12px; padding: 10px; border: 0; border-radius: 8px; cursor: pointer; background: #4f46e5; color: white; font-weight: 700; }
                    button:disabled { background: #4b5563; cursor: not-allowed; }
                    .erreur { color: #ff8d8d; }
                    table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                    th, td { padding: 10px; border-bottom: 1px solid #34435b; text-align: left; }
                </style>
            </head>
            <body>
                <main>
                    <h1>🎲 Stock de la bibliothèque</h1>
                    <p><a href="/gestion" style="color:#9db4ff">Gérer le stock</a></p>
                    <p id="message"></p>
                    <section id="jeux" class="grille"></section>
                    <section>
                        <h2>Emprunts en cours</h2>
                        <table>
                            <thead><tr><th>Emprunteur</th><th>Jeu</th><th>Action</th></tr></thead>
                            <tbody id="emprunts"></tbody>
                        </table>
                    </section>
                </main>
                <script>
                    async function charger() {
                        const [stockResponse, empruntsResponse, jeuxResponse] = await Promise.all([
                            fetch('/bibliotheque'),
                            fetch('/emprunts'),
                            fetch('/catalogue').catch(() => null)
                        ]);

                        const stock = await stockResponse.json();
                        const emprunts = await empruntsResponse.json();
                        let catalogue = [];
                        if (jeuxResponse) {
                            try { catalogue = await jeuxResponse.json(); } catch (e) { catalogue = []; }
                        }
                        const noms = new Map(catalogue.map(jeu => [jeu.id, jeu]));

                        document.getElementById('jeux').innerHTML = stock.map(entree => {
                            const jeu = noms.get(entree.id);
                            const nom = jeu ? jeu.nom : entree.id;
                            const type = jeu ? jeu.type : 'Informations non disponibles';
                            return `<article class="jeu">
                                <h2>${echapper(nom)}</h2>
                                <p>${echapper(type)}</p>
                                <p>Identifiant : <strong>${echapper(entree.id)}</strong></p>
                                <p class="stock">Stock : ${entree.quantite}</p>
                                <input id="emprunteur-${echapper(entree.id)}" placeholder="Nom de l'emprunteur">
                                <button ${entree.quantite <= 0 ? 'disabled' : ''}
                                    onclick="emprunter('${echapper(entree.id)}')">Emprunter</button>
                            </article>`;
                        }).join('');

                        document.getElementById('emprunts').innerHTML = emprunts.map(emprunt => `
                            <tr>
                                <td>${echapper(emprunt.emprunteur)}</td>
                                <td>${echapper(emprunt.idJeu)}</td>
                                <td><button onclick="rendre('${echapper(emprunt.emprunteur)}','${echapper(emprunt.idJeu)}')">Rendre</button></td>
                            </tr>`).join('');
                    }

                    async function emprunter(idJeu) {
                        const nom = document.getElementById('emprunteur-' + idJeu).value;
                        await envoyer('/emprunts?emprunteur=' + encodeURIComponent(nom) + '&idJeu=' + encodeURIComponent(idJeu), 'POST');
                        await charger();
                    }

                    async function rendre(emprunteur, idJeu) {
                        await envoyer('/emprunts?emprunteur=' + encodeURIComponent(emprunteur) + '&idJeu=' + encodeURIComponent(idJeu), 'DELETE');
                        await charger();
                    }

                    async function envoyer(url, methode) {
                        const response = await fetch(url, { method: methode });
                        const texte = await response.text();
                        let resultat;
                        try {
                            resultat = JSON.parse(texte);
                        } catch (e) {
                            resultat = { erreur: 'Réponse invalide du serveur (' + response.status + ')' };
                        }
                        afficherMessage(resultat.erreur || resultat.message || '', Boolean(resultat.erreur));
                        return resultat;
                    }

                    function afficherMessage(message, erreur = false) {
                        const element = document.getElementById('message');
                        element.textContent = message;
                        element.className = erreur ? 'erreur' : '';
                    }

                    function echapper(texte) {
                        const element = document.createElement('span');
                        element.textContent = texte == null ? '' : texte;
                        return element.innerHTML;
                    }

                    charger();
                </script>
            </body>
            </html>
            """;
        repondreHtml(exchange, 200, page);
    }

    private void gererPageGestion(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())
                || !"/gestion".equals(exchange.getRequestURI().getPath())) {
            repondreHtml(exchange, 404, "Page introuvable");
            return;
        }

        String page = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Gestion de la bibliothèque</title>
                <style>
                    :root { color-scheme: dark; }
                    * { box-sizing: border-box; }
                    body { margin: 0; font-family: system-ui, sans-serif; background: #101827; color: #f7f8fc; }
                    main { max-width: 1000px; margin: auto; padding: 32px 20px; }
                    h1, h2 { margin: 0 0 16px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                    th, td { padding: 10px; border-bottom: 1px solid #34435b; text-align: left; }
                    input, button { width: 100%; font: inherit; }
                    input { margin-top: 6px; padding: 10px; border: 1px solid #596984; border-radius: 8px; background: #111827; color: white; }
                    button { margin-top: 12px; padding: 10px; border: 0; border-radius: 8px; cursor: pointer; background: #4f46e5; color: white; font-weight: 700; }
                    button:disabled { background: #4b5563; cursor: not-allowed; }
                    .erreur { color: #ff8d8d; }
                </style>
            </head>
            <body>
                <main>
                    <h1>🎮 Gestion de la bibliothèque</h1>
                    <p id="message"></p>
                    <section>
                        <h2>Ajouter ou modifier un jeu</h2>
                        <input id="idJeu" placeholder="Identifiant du jeu">
                        <input id="nomJeu" placeholder="Nom du jeu">
                        <input id="typeJeu" placeholder="Type de jeu">
                        <input id="quantiteJeu" type="number" placeholder="Quantité en stock" min="0">
                        <button onclick="modifierJeu()">Enregistrer</button>
                    </section>
                    <section>
                        <h2>Liste des jeux</h2>
                        <table>
                            <thead><tr><th>Identifiant</th><th>Nom</th><th>Type</th><th>Stock</th><th>Action</th></tr></thead>
                            <tbody id="listeJeux"></tbody>
                        </table>
                    </section>
                </main>
                <script>
                    async function chargerGestion() {
                        const [stockResponse, jeuxResponse] = await Promise.all([
                            fetch('/bibliotheque'),
                            fetch('http://127.0.0.1:8081/jeux').catch(() => null)
                        ]);
                        const stock = await stockResponse.json();
                        let jeux = [];
                        if (jeuxResponse) {
                            try { jeux = await jeuxResponse.json(); } catch (e) { jeux = []; }
                        }
                        const noms = new Map(jeux.map(jeu => [jeu.id, jeu]));

                        document.getElementById('listeJeux').innerHTML = stock.map(entree => {
                            const jeu = noms.get(entree.id);
                            return `<tr>
                                <td>${echapper(entree.id)}</td>
                                <td>${echapper(jeu ? jeu.nom : entree.id)}</td>
                                <td>${echapper(jeu ? jeu.type : '')}</td>
                                <td>${entree.quantite}</td>
                                <td>
                                    <input id="gestion-${echapper(entree.id)}" type="number" min="1" value="1">
                                    <button onclick="modifierStock('${echapper(entree.id)}','ajouter')">Ajouter</button>
                                    <button onclick="modifierStock('${echapper(entree.id)}','retirer')">Retirer</button>
                                    <button onclick="supprimerJeu('${echapper(entree.id)}')">Supprimer</button>
                                </td>
                            </tr>`;
                        }).join('');
                    }

                    async function modifierJeu() {
                        const id = document.getElementById('idJeu').value;
                        const nom = document.getElementById('nomJeu').value;
                        const type = document.getElementById('typeJeu').value;
                        const quantite = document.getElementById('quantiteJeu').value;

                        if (!id || !nom || !type || quantite === '') {
                            afficherMessage('Tous les champs doivent être remplis', true);
                            return;
                        }

                        await envoyer('/gestion-stock?action=creer&id=' + encodeURIComponent(id)
                            + '&nom=' + encodeURIComponent(nom) + '&type=' + encodeURIComponent(type)
                            + '&quantite=' + encodeURIComponent(quantite), 'POST');
                        await chargerGestion();
                    }

                    async function modifierStock(idJeu, action) {
                        const quantite = document.getElementById('gestion-' + idJeu).value;
                        await envoyer('/gestion-stock?action=' + action + '&id=' + encodeURIComponent(idJeu)
                            + '&quantite=' + encodeURIComponent(quantite), 'POST');
                        await chargerGestion();
                    }

                    async function supprimerJeu(idJeu) {
                        await envoyer('/gestion-stock?action=supprimer&id=' + encodeURIComponent(idJeu), 'POST');
                        await chargerGestion();
                    }

                    async function envoyer(url, methode) {
                        const response = await fetch(url, { method: methode });
                        const texte = await response.text();
                        let resultat;
                        try {
                            resultat = JSON.parse(texte);
                        } catch (e) {
                            resultat = { erreur: 'Réponse invalide du serveur (' + response.status + ')' };
                        }
                        afficherMessage(resultat.erreur || resultat.message || '', Boolean(resultat.erreur));
                        return resultat;
                    }

                    function afficherMessage(message, erreur = false) {
                        const element = document.getElementById('message');
                        element.textContent = message;
                        element.className = erreur ? 'erreur' : '';
                    }

                    function echapper(texte) {
                        const element = document.createElement('span');
                        element.textContent = texte == null ? '' : texte;
                        return element.innerHTML;
                    }

                    chargerGestion();
                </script>
            </body>
            </html>
            """;
        repondreHtml(exchange, 200, page);
    }

    private void gererGetListe(HttpExchange exchange,
                               InterfaceStockageBibliotheque stockage) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            repondreJson(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
            return;
        }
        repondreJson(exchange, 200, stockEnJson(stockage));
    }

    private void gererEmprunts(HttpExchange exchange,
                               InterfaceStockageBibliotheque stockage) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            repondreJson(exchange, 200, empruntsEnJson(stockage));
            return;
        }

        String emprunteur = parametre(exchange, "emprunteur");
        String idJeu = parametre(exchange, "idJeu");
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                stockage.emprunterJeu(emprunteur, idJeu);
                repondreJson(exchange, 201, "{\"message\":\"Jeu emprunté\"}");
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                stockage.rendreJeu(emprunteur, idJeu);
                repondreJson(exchange, 200, "{\"message\":\"Jeu rendu\"}");
            } else {
                repondreJson(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            repondreJson(exchange, 400, "{\"erreur\":\"" + echapperJson(e.getMessage()) + "\"}");
        }
    }

    private void gererGestionStock(HttpExchange exchange,
                                  InterfaceStockageBibliotheque stockage) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            repondreJson(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
            return;
        }

        String action = parametre(exchange, "action");
        String idJeu = parametre(exchange, "id");

        try {
            if ("creer".equals(action)) {
                String nom = parametre(exchange, "nom");
                String type = parametre(exchange, "type");
                int quantite = Integer.parseInt(parametre(exchange, "quantite"));
                stockage.creerJeu(idJeu, nom, type, quantite);
                synchroniserJeu("POST", idJeu, nom, type);

                repondreJson(exchange, 201, "{\"message\":\"Jeu ajouté au stock\"}");

            } else if ("ajouter".equals(action)) {
                int quantite = Integer.parseInt(parametre(exchange, "quantite"));
                stockage.ajouterStock(idJeu, quantite);
                repondreJson(exchange, 200, "{\"message\":\"Stock augmenté\"}");

            } else if ("retirer".equals(action)) {
                int quantite = Integer.parseInt(parametre(exchange, "quantite"));
                stockage.retirerStock(idJeu, quantite);
                repondreJson(exchange, 200, "{\"message\":\"Stock réduit\"}");

            } else if ("supprimer".equals(action)) {
                stockage.supprimerJeu(idJeu);
                synchroniserJeu("DELETE", idJeu, null, null);

                repondreJson(exchange, 200, "{\"message\":\"Jeu supprimé\"}");

            } else {
                repondreJson(exchange, 400, "{\"erreur\":\"Action inconnue\"}");
            }
        } catch (IllegalArgumentException | IllegalStateException |
                 NullPointerException e) {
            repondreJson(exchange, 400,
                    "{\"erreur\":\"" + echapperJson(e.getMessage()) + "\"}");
        }
    }

    private void gererCatalogue(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            repondreJson(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
            return;
        }

        try {
            HttpResponse<String> reponse = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://127.0.0.1:8081/jeux"))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            repondreJson(exchange, reponse.statusCode(), reponse.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repondreJson(exchange, 503, "{\"erreur\":\"Serveur des jeux indisponible\"}");
        } catch (java.net.ConnectException e) {
            repondreJson(exchange, 503, "{\"erreur\":\"Serveur des jeux indisponible\"}");
        } catch (java.io.IOException e) {
            repondreJson(exchange, 503, "{\"erreur\":\"Serveur des jeux indisponible\"}");
        }
    }

    private void synchroniserJeu(String methode, String id, String nom, String type) {
        try {
            String url;
            if ("POST".equals(methode)) {
                url = "http://127.0.0.1:8081/jeux?id="
                        + URLEncoder.encode(id, StandardCharsets.UTF_8)
                        + "&nom=" + URLEncoder.encode(nom, StandardCharsets.UTF_8)
                        + "&type=" + URLEncoder.encode(type, StandardCharsets.UTF_8);
            } else {
                url = "http://127.0.0.1:8081/jeux?id="
                        + URLEncoder.encode(id, StandardCharsets.UTF_8);
            }

            HttpRequest.Builder requete = HttpRequest.newBuilder().uri(java.net.URI.create(url));
            HttpRequest httpRequest = "POST".equals(methode)
                    ? requete.POST(HttpRequest.BodyPublishers.noBody()).build()
                    : requete.DELETE().build();

            HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (java.io.IOException ignored) {
        }
    }

    private String stockEnJson(InterfaceStockageBibliotheque stockage) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < stockage.getJeuxBibliotheque().size(); i++) {
            if (i > 0) json.append(',');
            JeuStocke jeu = stockage.getJeuxBibliotheque().get(i);
            json.append("{\"id\":\"").append(echapperJson(jeu.getId()))
                .append("\",\"quantite\":").append(jeu.getQuantite()).append('}');
        }
        return json.append(']').toString();
    }

    private String empruntsEnJson(InterfaceStockageBibliotheque stockage) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < stockage.getEmprunts().size(); i++) {
            if (i > 0) json.append(',');
            Emprunt emprunt = stockage.getEmprunts().get(i);
            json.append("{\"emprunteur\":\"").append(echapperJson(emprunt.getEmprunteur()))
                .append("\",\"idJeu\":\"").append(echapperJson(emprunt.getIdJeu())).append("\"}");
        }
        return json.append(']').toString();
    }

    private String parametre(HttpExchange exchange, String nom) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) return null;
        for (String paire : query.split("&")) {
            String[] parties = paire.split("=", 2);
            if (parties.length == 2 && nom.equals(parties[0])) {
                return URLDecoder.decode(parties[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String echapperJson(String texte) {
        if (texte == null) return "";
        return texte.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private void repondreHtml(HttpExchange exchange, int statut, String contenu) throws IOException {
        repondre(exchange, statut, "text/html; charset=UTF-8", contenu);
    }

    private void repondreJson(HttpExchange exchange, int statut, String contenu) throws IOException {
        repondre(exchange, statut, "application/json; charset=UTF-8", contenu);
    }

    private void repondre(HttpExchange exchange, int statut, String type, String contenu) throws IOException {
        byte[] corps = contenu.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statut, corps.length);
        exchange.getResponseBody().write(corps);
        exchange.close();
    }
}
