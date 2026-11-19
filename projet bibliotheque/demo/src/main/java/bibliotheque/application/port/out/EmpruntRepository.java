package bibliotheque.application.port.out;

import bibliotheque.domain.model.Emprunt;

import java.time.LocalDate;

public interface EmpruntRepository {
    Emprunt enregistrer(int ID_Emprunteur, int ID_Jeu, LocalDate Date_Emprunt,
            int Duree_Emprunt_Jours);
}
