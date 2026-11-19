package bibliotheque;

import bibliotheque.adapter.out.memory.CatalogueJeuxMemoire;
import bibliotheque.adapter.out.memory.EmpruntRepositoryMemoire;
import bibliotheque.adapter.out.memory.EmprunteurRepositoryMemoire;
import bibliotheque.adapter.in.web.ServeurWeb;
import bibliotheque.application.service.ServiceEmprunt;
import bibliotheque.domain.model.Emprunt;
import bibliotheque.domain.model.Emprunteur;
import bibliotheque.domain.model.Jeu;
import bibliotheque.domain.model.StatutEmprunt;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppTest {
    @Test
    void createsLoanWithGeneratedBorrowerIDAndThirtyDayDeadline() {
        Clock Horloge = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        LocalDate Date_Emprunt = LocalDate.now(Horloge);
        CatalogueJeuxMemoire Catalogue = new CatalogueJeuxMemoire(List.of(new Jeu(4, "Jeu")));
        EmprunteurRepositoryMemoire Emprunteurs = new EmprunteurRepositoryMemoire();
        EmpruntRepositoryMemoire Emprunts = new EmpruntRepositoryMemoire();
        ServiceEmprunt Service = new ServiceEmprunt(
                Catalogue,
                Emprunteurs,
                Emprunts,
                Horloge);

        Emprunt Emprunt = Service.confirmerEmprunt(" Alice ", 4);

        assertEquals(1, Emprunt.getID_Emprunteur());
        assertEquals(1, Emprunt.getID_Emprunt());
        assertEquals(Date_Emprunt, Emprunt.getDate_Emprunt());
        assertEquals(Date_Emprunt.plusDays(30), Emprunt.getLimiteRetour());
        assertEquals(StatutEmprunt.EN_COURS, Emprunt.getStatutEmprunt());
        assertEquals("Alice", Emprunteurs.trouverTous().get(0).getNom());
        assertEquals(1, Emprunts.trouverTous().size());
    }

    @Test
    void refusesToCreateBorrowerWhenGameIsNotInCatalog() {
        EmprunteurRepositoryMemoire Emprunteurs = new EmprunteurRepositoryMemoire();
        ServiceEmprunt Service = new ServiceEmprunt(
                new CatalogueJeuxMemoire(List.of()),
                Emprunteurs,
                new EmpruntRepositoryMemoire());

        assertThrows(IllegalArgumentException.class,
                () -> Service.confirmerEmprunt("Alice", 1));
        assertEquals(0, Emprunteurs.trouverTous().size());
    }

    @Test
    void assignsDistinctIDsToBorrowers() {
        EmprunteurRepositoryMemoire Emprunteurs = new EmprunteurRepositoryMemoire();

        Emprunteur First = Emprunteurs.enregistrer("Alice");
        Emprunteur Second = Emprunteurs.enregistrer("Bob");

        assertNotEquals(First.getID_Emprunteur(), Second.getID_Emprunteur());
    }

    @Test
    void calculatesDeadlineFromLoanDate() {
        LocalDate Date_Emprunt = LocalDate.of(2026, 1, 1);
        Emprunt Emprunt = new Emprunt(1, 2, 3, Date_Emprunt, 10);

        assertEquals(LocalDate.of(2026, 1, 11), Emprunt.getLimiteRetour());
    }

    @Test
    void marksLoanOverdueAfterItsReturnDeadlineWithoutChangingDeadline() {
        LocalDate Date_Emprunt = LocalDate.now().minusDays(10);
        Emprunt Emprunt = new Emprunt(1, 2, 3, Date_Emprunt, 5);
        LocalDate Limite_Retour = Emprunt.getLimiteRetour();

        assertEquals(StatutEmprunt.EN_RETARD, Emprunt.getStatutEmprunt());
        assertEquals(Limite_Retour, Emprunt.getLimiteRetour());
    }

    @Test
    void returnedLoanKeepsReturnedStatusAndRecordsReturnDate() {
        Emprunt Emprunt = new Emprunt(1, 2, 3, LocalDate.now().minusDays(10), 5);
        Emprunt.marquerCommeRendu();

        assertEquals(StatutEmprunt.RENDU, Emprunt.getStatutEmprunt());
        assertEquals(LocalDate.now(), Emprunt.getLimiteRetour());
    }

    @Test
    void rejectsNonPositiveLoanDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new Emprunt(1, 2, 3, LocalDate.now(), 0));
    }

    @Test
    void assignsDistinctIDsToLoans() {
        EmpruntRepositoryMemoire Emprunts = new EmpruntRepositoryMemoire();
        LocalDate Date_Emprunt = LocalDate.now();

        Emprunt First = Emprunts.enregistrer(1, 2, Date_Emprunt, 10);
        Emprunt Second = Emprunts.enregistrer(1, 2, Date_Emprunt, 10);

        assertNotEquals(First.getID_Emprunt(), Second.getID_Emprunt());
    }

    @Test
    void webAdapterConfirmsLoanThroughApplicationPort() throws Exception {
        CatalogueJeuxMemoire Catalogue = new CatalogueJeuxMemoire(List.of(new Jeu(4, "Jeu")));
        EmprunteurRepositoryMemoire Emprunteurs = new EmprunteurRepositoryMemoire();
        EmpruntRepositoryMemoire Emprunts = new EmpruntRepositoryMemoire();
        ServiceEmprunt Service = new ServiceEmprunt(Catalogue, Emprunteurs, Emprunts);
        ServeurWeb Serveur = new ServeurWeb(0, Service);
        Serveur.demarrer();
        try {
            URI Adresse = URI.create("http://127.0.0.1:" + Serveur.getPort() + "/emprunts");
            HttpRequest Requete = HttpRequest.newBuilder(Adresse)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("nom=Alice+Martin&jeu=4"))
                    .build();
            HttpResponse<String> Reponse = HttpClient.newHttpClient()
                    .send(Requete, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, Reponse.statusCode());
            assertEquals(1, Emprunteurs.trouverTous().size());
            assertEquals(1, Emprunts.trouverTous().size());
            assertEquals("Alice Martin", Emprunteurs.trouverTous().get(0).getNom());
            assertEquals(LocalDate.now().plusDays(30),
                    Emprunts.trouverTous().get(0).getLimiteRetour());
        } finally {
            Serveur.arreter(0);
        }
    }
}
