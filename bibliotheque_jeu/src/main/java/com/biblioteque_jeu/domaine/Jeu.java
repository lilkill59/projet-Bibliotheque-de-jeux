package com.biblioteque_jeu.domaine;

public class Jeu {

    private final String id;
    private final String nom;
    private final String type;

    public Jeu(String id, String nom, String type) {
        this.id = id;
        this.nom = nom;
        this.type = type;
    }

    public Jeu(String nom, String type) {
        this(nom, nom, type);
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getType() {
        return type;
    }
}
