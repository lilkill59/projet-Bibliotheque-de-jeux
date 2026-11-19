package bibliotheque.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class Emprunt {
    private final int ID_Emprunt;
    private final int ID_Emprunteur;
    private final int ID_Jeu;
    private final LocalDate Date_Emprunt;
    private LocalDate Limite_Retour;
    private StatutEmprunt Statut_Emprunt;

    public Emprunt(int ID_Emprunt, int ID_Emprunteur, int ID_Jeu,
            LocalDate Date_Emprunt, int Duree_Emprunt_Jours) {
        if (ID_Emprunt <= 0 || ID_Emprunteur <= 0 || ID_Jeu <= 0) {
            throw new IllegalArgumentException("Les identifiants doivent être positifs.");
        }
        if (Duree_Emprunt_Jours <= 0) {
            throw new IllegalArgumentException("La durée de l'emprunt doit être positive.");
        }
        this.ID_Emprunt = ID_Emprunt;
        this.ID_Emprunteur = ID_Emprunteur;
        this.ID_Jeu = ID_Jeu;
        this.Date_Emprunt = Objects.requireNonNull(Date_Emprunt);
        this.Limite_Retour = this.Date_Emprunt.plusDays(Duree_Emprunt_Jours);
        this.Statut_Emprunt = StatutEmprunt.EN_COURS;
    }

    public int getID_Emprunt() {
        return ID_Emprunt;
    }

    public int getID_Emprunteur() {
        return ID_Emprunteur;
    }

    public int getID_Jeu() {
        return ID_Jeu;
    }

    public LocalDate getDate_Emprunt() {
        return Date_Emprunt;
    }

    public LocalDate getLimiteRetour() {
        return Limite_Retour;
    }

    public StatutEmprunt getStatutEmprunt() {
        if (Statut_Emprunt != StatutEmprunt.RENDU
                && LocalDate.now().isAfter(Limite_Retour)) {
            Statut_Emprunt = StatutEmprunt.EN_RETARD;
        }
        return Statut_Emprunt;
    }

    public void marquerCommeRendu() {
        if (Statut_Emprunt != StatutEmprunt.RENDU) {
            Limite_Retour = LocalDate.now();
        }
        Statut_Emprunt = StatutEmprunt.RENDU;
    }
}
