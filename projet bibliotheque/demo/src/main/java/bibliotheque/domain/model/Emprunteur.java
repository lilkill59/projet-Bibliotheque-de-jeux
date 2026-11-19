package bibliotheque.domain.model;

import java.util.Objects;

public class Emprunteur {
    private final int ID_Emprunteur;
    private final String Nom;

    public Emprunteur(int ID_Emprunteur, String Nom) {
        if (ID_Emprunteur <= 0) {
            throw new IllegalArgumentException("L'identifiant de l'emprunteur doit être positif.");
        }
        this.Nom = Objects.requireNonNull(Nom).strip();
        if (this.Nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'emprunteur est obligatoire.");
        }
        this.ID_Emprunteur = ID_Emprunteur;
    }

    public int getID_Emprunteur() {
        return ID_Emprunteur;
    }

    public String getNom() {
        return Nom;
    }
}
