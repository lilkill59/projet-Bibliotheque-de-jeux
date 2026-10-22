package com.biblioteque_jeu;
import java.util.ArrayList;
import java.util.List;

public class StockageJeu {
    private ArrayList<Jeu> jeux;
    private int taille = 0;

    public StockageJeu() {
        this.jeux = new ArrayList<>();
    }

    public void ajouterJeu(Jeu jeu) {
        jeux.add(jeu);
        taille++;
    }

    public Jeu getJeu(int index) {
        if (index >= 0 && index < jeux.size()) {
            return jeux.get(index);
        } else {
            return null;
        }
    }

    public void supprimerJeu(int index) {
        if (index >= 0 && index < jeux.size()) {
            jeux.remove(index);
            taille--;
        }
    }

    public int getTaille() {
        return jeux.size();
    }
}
