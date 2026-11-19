package bibliotheque.application.port.in;

import bibliotheque.domain.model.Emprunt;
import bibliotheque.domain.model.Jeu;

import java.util.List;

public interface CreerEmpruntUseCase {
    List<Jeu> listerLesJeux();

    Emprunt confirmerEmprunt(String Nom, int ID_Jeu);
}
