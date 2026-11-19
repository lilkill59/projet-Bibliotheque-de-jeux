package bibliotheque.application.service;

import bibliotheque.application.port.in.CreerEmpruntUseCase;
import bibliotheque.application.port.out.CatalogueJeuxPort;
import bibliotheque.application.port.out.EmpruntRepository;
import bibliotheque.application.port.out.EmprunteurRepository;
import bibliotheque.domain.model.Emprunt;
import bibliotheque.domain.model.Emprunteur;
import bibliotheque.domain.model.Jeu;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ServiceEmprunt implements CreerEmpruntUseCase {
    private static final int DUREE_EMPRUNT_JOURS = 30;

    private final CatalogueJeuxPort Catalogue;
    private final EmprunteurRepository Emprunteurs;
    private final EmpruntRepository Emprunts;
    private final Clock Horloge;

    public ServiceEmprunt(CatalogueJeuxPort Catalogue,
            EmprunteurRepository Emprunteurs, EmpruntRepository Emprunts) {
        this(Catalogue, Emprunteurs, Emprunts, Clock.systemDefaultZone());
    }

    public ServiceEmprunt(CatalogueJeuxPort Catalogue,
            EmprunteurRepository Emprunteurs, EmpruntRepository Emprunts, Clock Horloge) {
        this.Catalogue = Objects.requireNonNull(Catalogue);
        this.Emprunteurs = Objects.requireNonNull(Emprunteurs);
        this.Emprunts = Objects.requireNonNull(Emprunts);
        this.Horloge = Objects.requireNonNull(Horloge);
    }

    @Override
    public List<Jeu> listerLesJeux() {
        return List.copyOf(Catalogue.listerLesJeux());
    }

    @Override
    public Emprunt confirmerEmprunt(String Nom, int ID_Jeu) {
        if (Nom == null || Nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'emprunteur est obligatoire.");
        }
        boolean JeuDisponible = Catalogue.listerLesJeux().stream()
                .anyMatch(Jeu -> Jeu.getID_Jeu() == ID_Jeu);
        if (!JeuDisponible) {
            throw new IllegalArgumentException("Veuillez choisir un jeu valide.");
        }

        Emprunteur Emprunteur = Emprunteurs.enregistrer(Nom);
        LocalDate Date_Emprunt = LocalDate.now(Horloge);
        return Emprunts.enregistrer(
                Emprunteur.getID_Emprunteur(),
                ID_Jeu,
                Date_Emprunt,
                DUREE_EMPRUNT_JOURS);
    }
}
