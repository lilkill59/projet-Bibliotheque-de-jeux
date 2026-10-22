package com.biblioteque_jeu;
import java.util.ArrayList;


public class StockageJeu {
    private ArrayList<Jeu> jeux;
    private static int taille = 0;

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

    public int getTaille() {
        return taille;
    }
}
