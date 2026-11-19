package bibliotheque;

import bibliotheque.adapter.in.web.ServeurWeb;
import bibliotheque.adapter.out.memory.CatalogueJeuxMemoire;
import bibliotheque.adapter.out.memory.EmpruntRepositoryMemoire;
import bibliotheque.adapter.out.memory.EmprunteurRepositoryMemoire;
import bibliotheque.application.service.ServiceEmprunt;

import java.io.IOException;
import java.util.List;

public final class App {
    private static final int PORT = 8080;

    private App() {
    }

    public static void main(String[] Args) throws IOException {
        CatalogueJeuxMemoire Catalogue = new CatalogueJeuxMemoire(List.of());
        EmprunteurRepositoryMemoire Emprunteurs = new EmprunteurRepositoryMemoire();
        EmpruntRepositoryMemoire Emprunts = new EmpruntRepositoryMemoire();
        ServiceEmprunt Service = new ServiceEmprunt(Catalogue, Emprunteurs, Emprunts);
        ServeurWeb Serveur = new ServeurWeb(PORT, Service);
        Serveur.demarrer();
        System.out.println("Bibliothèque disponible sur http://localhost:" + PORT);
    }
}
