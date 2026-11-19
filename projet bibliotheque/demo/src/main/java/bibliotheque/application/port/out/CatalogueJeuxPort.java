package bibliotheque.application.port.out;

import bibliotheque.domain.model.Jeu;

import java.util.List;

public interface CatalogueJeuxPort {
    List<Jeu> listerLesJeux();
}
