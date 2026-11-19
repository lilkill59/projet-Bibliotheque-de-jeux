package com.biblioteque_jeu.adapters.memoire;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.biblioteque_jeu.domaine.Jeu;
import com.biblioteque_jeu.ports.InterfaceStockageJeu;

public class StockageJeuMemoire implements InterfaceStockageJeu {

    private final Map<String, Jeu> jeux = new LinkedHashMap<>();

    @Override
    public void ajouterJeu(Jeu jeu) {
        if (jeu == null) {
            throw new IllegalArgumentException("Le jeu ne peut pas être null");
        }
        jeux.put(jeu.getId(), jeu);
    }

    @Override
    public Jeu getJeu(int index) {
        if (index < 0 || index >= jeux.size()) {
            return null;
        }
        return new ArrayList<>(jeux.values()).get(index);
    }

    @Override
    public Jeu getJeuParId(String id) {
        return jeux.get(id);
    }

    @Override
    public void supprimerJeu(int index) {
        Jeu jeu = getJeu(index);
        if (jeu != null) {
            jeux.remove(jeu.getId());
        }
    }

    @Override
    public boolean supprimerJeuParId(String id) {
        return jeux.remove(id) != null;
    }

    @Override
    public int getTaille() {
        return jeux.size();
    }

    @Override
    public List<Jeu> getJeux() {
        return new ArrayList<>(jeux.values());
    }
}
