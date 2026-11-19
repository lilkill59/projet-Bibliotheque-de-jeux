package bibliotheque.application.port.out;

import bibliotheque.domain.model.Emprunteur;

public interface EmprunteurRepository {
    Emprunteur enregistrer(String Nom);
}
