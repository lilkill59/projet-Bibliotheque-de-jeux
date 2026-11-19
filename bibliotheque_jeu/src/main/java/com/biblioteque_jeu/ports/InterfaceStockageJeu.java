package com.biblioteque_jeu.ports;

import java.util.List;

import com.biblioteque_jeu.domaine.Jeu;

public interface InterfaceStockageJeu {

    void ajouterJeu(Jeu jeu);

    Jeu getJeu(int index);

    Jeu getJeuParId(String id);

    void supprimerJeu(int index);

    boolean supprimerJeuParId(String id);

    int getTaille();

    List<Jeu> getJeux();
}
