package bibliotheque.adapter.out.memory;

import bibliotheque.application.port.out.EmpruntRepository;
import bibliotheque.domain.model.Emprunt;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EmpruntRepositoryMemoire implements EmpruntRepository {
    private final AtomicInteger Prochain_ID = new AtomicInteger(1);
    private final List<Emprunt> Emprunts = new CopyOnWriteArrayList<>();

    @Override
    public Emprunt enregistrer(int ID_Emprunteur, int ID_Jeu, LocalDate Date_Emprunt,
            int Duree_Emprunt_Jours) {
        Emprunt Emprunt = new Emprunt(
                Prochain_ID.getAndIncrement(),
                ID_Emprunteur,
                ID_Jeu,
                Date_Emprunt,
                Duree_Emprunt_Jours);
        Emprunts.add(Emprunt);
        return Emprunt;
    }

    public List<Emprunt> trouverTous() {
        return List.copyOf(Emprunts);
    }
}
