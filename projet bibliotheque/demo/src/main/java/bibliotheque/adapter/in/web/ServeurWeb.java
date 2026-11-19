package bibliotheque.adapter.in.web;

import bibliotheque.application.port.in.CreerEmpruntUseCase;
import bibliotheque.domain.model.Emprunt;
import bibliotheque.domain.model.Jeu;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ServeurWeb {
    private final CreerEmpruntUseCase CasEmprunt;
    private final HttpServer Serveur;

    public ServeurWeb(int Port, CreerEmpruntUseCase CasEmprunt) throws IOException {
        this.CasEmprunt = Objects.requireNonNull(CasEmprunt);
        this.Serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", Port), 0);
        Serveur.createContext("/emprunts", this::creerEmprunt);
        Serveur.createContext("/", this::afficherAccueil);
    }

    public void demarrer() {
        Serveur.start();
    }

    public int getPort() {
        return Serveur.getAddress().getPort();
    }

    public void arreter(int DelaiSecondes) {
        Serveur.stop(DelaiSecondes);
    }

    private void afficherAccueil(HttpExchange Echange) throws IOException {
        if (!"GET".equals(Echange.getRequestMethod())) {
            repondre(Echange, 405, "Méthode non autorisée.", false);
            return;
        }
        repondre(Echange, 200, page(""), true);
    }

    private void creerEmprunt(HttpExchange Echange) throws IOException {
        if (!"POST".equals(Echange.getRequestMethod())) {
            repondre(Echange, 405, "Méthode non autorisée.", false);
            return;
        }

        if (CasEmprunt.listerLesJeux().isEmpty()) {
            repondre(Echange, 409, page("Le catalogue de jeux sera ajouté prochainement."), true);
            return;
        }

        Map<String, String> Champs;
        try {
            String Corps = new String(Echange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Champs = analyserFormulaire(Corps);
        } catch (IllegalArgumentException Exception) {
            repondre(Echange, 400, page("Le formulaire est invalide."), true);
            return;
        }

        int ID_Jeu;
        try {
            ID_Jeu = Integer.parseInt(Champs.getOrDefault("jeu", ""));
        } catch (NumberFormatException Exception) {
            repondre(Echange, 400, page("Veuillez choisir un jeu valide."), true);
            return;
        }

        try {
            Emprunt Emprunt = CasEmprunt.confirmerEmprunt(
                    Champs.getOrDefault("nom", ""), ID_Jeu);
            String Confirmation = "Emprunt confirmé : ID emprunteur "
                    + Emprunt.getID_Emprunteur()
                    + ", ID emprunt " + Emprunt.getID_Emprunt()
                    + ", statut en cours, date d'emprunt " + Emprunt.getDate_Emprunt()
                    + ", remise prévue le " + Emprunt.getLimiteRetour() + ".";
            repondre(Echange, 201, page(Confirmation), true);
        } catch (IllegalArgumentException Exception) {
            repondre(Echange, 400, page(Exception.getMessage()), true);
        }
    }

    private String page(String Message) {
        var Jeux = CasEmprunt.listerLesJeux();
        StringBuilder Options = new StringBuilder();
        for (Jeu Jeu : Jeux) {
            Options.append("<option value=\"")
                    .append(Jeu.getID_Jeu())
                    .append("\">")
                    .append(echapperHtml(Jeu.getNom()))
                    .append("</option>");
        }
        String Selection = Jeux.isEmpty()
                ? "<option value=\"\" selected disabled>Catalogue à venir</option>"
                : "<option value=\"\" selected disabled>Choisir un jeu</option>" + Options;
        String Bouton = Jeux.isEmpty()
                ? "<button type=\"submit\" disabled>Confirmer l'emprunt</button>"
                : "<button type=\"submit\">Confirmer l'emprunt</button>";
        String Notice = Jeux.isEmpty()
                ? "<p class=\"notice\">Le catalogue de jeux sera ajouté prochainement.</p>" : "";
        return """
                <!doctype html>
                <html lang="fr">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Emprunter un jeu</title>
                  <style>
                    body { font-family: system-ui, sans-serif; max-width: 36rem; margin: 4rem auto; padding: 0 1rem; color: #172033; background: #f4f6fb; }
                    main { background: white; padding: 2rem; border-radius: 1rem; box-shadow: 0 0.5rem 2rem #17203318; }
                    label { display: block; margin-top: 1rem; font-weight: 600; }
                    input, select, button { box-sizing: border-box; width: 100%%; margin-top: 0.4rem; padding: 0.75rem; border: 1px solid #cbd2e1; border-radius: 0.5rem; font: inherit; }
                    button { margin-top: 1.5rem; border: 0; background: #3559c7; color: white; font-weight: 700; cursor: pointer; }
                    button:disabled { background: #9aa4b8; cursor: not-allowed; }
                    .notice { color: #74500b; background: #fff3d4; padding: 0.75rem; border-radius: 0.5rem; }
                    .message { margin-top: 1rem; }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Nouvel emprunt</h1>
                    <form method="post" action="/emprunts">
                      <label for="nom">Votre nom</label>
                      <input id="nom" name="nom" required maxlength="100" autocomplete="name">
                      <label for="jeu">Jeu à emprunter</label>
                      <select id="jeu" name="jeu" required>
                        %s
                      </select>
                      %s
                    </form>
                    %s
                    <p class="message" role="status">%s</p>
                  </main>
                </body>
                </html>
                """.formatted(Selection, Bouton, Notice, echapperHtml(Message));
    }

    private static Map<String, String> analyserFormulaire(String Corps) {
        return Arrays.stream(Corps.split("&"))
                .map(Champ -> Champ.split("=", 2))
                .collect(Collectors.toMap(
                        Champ -> URLDecoder.decode(Champ[0], StandardCharsets.UTF_8),
                        Champ -> Champ.length > 1
                                ? URLDecoder.decode(Champ[1], StandardCharsets.UTF_8) : "",
                        (PremiereValeur, SecondeValeur) -> SecondeValeur));
    }

    private static String echapperHtml(String Valeur) {
        return Valeur.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void repondre(
            HttpExchange Echange, int Code, String Contenu, boolean Html) throws IOException {
        byte[] Donnees = Contenu.getBytes(StandardCharsets.UTF_8);
        Echange.getResponseHeaders().set("Content-Type",
                Html ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8");
        Echange.sendResponseHeaders(Code, Donnees.length);
        try (OutputStream CorpsReponse = Echange.getResponseBody()) {
            CorpsReponse.write(Donnees);
        }
    }
}
