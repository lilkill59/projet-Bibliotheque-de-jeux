package com.biblioteque_jeu.domaine;

public class Emprunt {

    private final String emprunteur;
    private final String idJeu;

    public Emprunt(String emprunteur, String idJeu) {
        this.emprunteur = emprunteur;
        this.idJeu = idJeu;
    }

    public String getEmprunteur() {
        return emprunteur;
    }

    public String getIdJeu() {
        return idJeu;
    }
}
