package bibliotheque.adapter.out.memory;

import bibliotheque.application.port.out.EmprunteurRepository;
import bibliotheque.domain.model.Emprunteur;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EmprunteurRepositoryMemoire implements EmprunteurRepository {
    private final AtomicInteger Prochain_ID = new AtomicInteger(1);
    private final List<Emprunteur> Emprunteurs = new CopyOnWriteArrayList<>();

    @Override
    public Emprunteur enregistrer(String Nom) {
        Emprunteur Emprunteur = new Emprunteur(Prochain_ID.getAndIncrement(), Nom);
        Emprunteurs.add(Emprunteur);
        return Emprunteur;
    }

    public List<Emprunteur> trouverTous() {
        return List.copyOf(Emprunteurs);
    }
}
