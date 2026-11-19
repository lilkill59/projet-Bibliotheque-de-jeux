package bibliotheque.adapter.out.memory;

import bibliotheque.application.port.out.CatalogueJeuxPort;
import bibliotheque.domain.model.Jeu;

import java.util.List;

public class CatalogueJeuxMemoire implements CatalogueJeuxPort {
    private final List<Jeu> Jeux;

    public CatalogueJeuxMemoire(List<Jeu> Jeux) {
        this.Jeux = List.copyOf(Jeux);
    }

    @Override
    public List<Jeu> listerLesJeux() {
        return Jeux;
    }
}
