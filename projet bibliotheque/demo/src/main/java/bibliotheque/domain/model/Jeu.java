package bibliotheque.domain.model;

import java.util.Objects;

public class Jeu {
    private final int ID_Jeu;
    private final String Nom;

    public Jeu(int ID_Jeu, String Nom) {
        if (ID_Jeu <= 0) {
            throw new IllegalArgumentException("L'ID du jeu doit être positif.");
        }
        this.ID_Jeu = ID_Jeu;
        this.Nom = Objects.requireNonNull(Nom).strip();
        if (this.Nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom du jeu est obligatoire.");
        }
    }

    public int getID_Jeu() {
        return ID_Jeu;
    }

    public String getNom() {
        return Nom;
    }
}
