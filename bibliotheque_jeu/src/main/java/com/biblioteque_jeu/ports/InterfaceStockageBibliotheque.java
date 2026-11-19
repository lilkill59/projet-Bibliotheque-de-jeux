package com.biblioteque_jeu.ports;

import java.util.List;

import com.biblioteque_jeu.domaine.Emprunt;
import com.biblioteque_jeu.domaine.Jeu;
import com.biblioteque_jeu.domaine.JeuStocke;

public interface InterfaceStockageBibliotheque {

    Jeu creerJeu(String id, String nom, String type, int quantite);

    void emprunterJeu(String emprunteur, String idJeu);

    void rendreJeu(String emprunteur, String idJeu);

    List<JeuStocke> getJeuxBibliotheque();

    List<Jeu> getJeuxDisponibles();

    List<Emprunt> getEmprunts();

    int getQuantite(String idJeu);

    void ajouterStock(String idJeu, int quantite);

    void retirerStock(String idJeu, int quantite);

    void supprimerJeu(String idJeu);
}
