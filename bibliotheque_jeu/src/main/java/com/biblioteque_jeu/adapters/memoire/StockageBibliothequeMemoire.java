package com.biblioteque_jeu.adapters.memoire;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.biblioteque_jeu.domaine.Emprunt;
import com.biblioteque_jeu.domaine.Jeu;
import com.biblioteque_jeu.domaine.JeuStocke;
import com.biblioteque_jeu.ports.InterfaceStockageBibliotheque;

public class StockageBibliothequeMemoire implements InterfaceStockageBibliotheque {

    private final Map<String, Jeu> jeuxDisponibles = new LinkedHashMap<>();
    private final Map<String, Integer> quantites = new LinkedHashMap<>();
    private final List<Emprunt> emprunts = new ArrayList<>();

    @Override
    public Jeu creerJeu(String id, String nom, String type, int quantite) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant du jeu est requis");
        }
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }

        Jeu jeu = new Jeu(id, nom, type);
        jeuxDisponibles.put(id, jeu);

        if (quantite > 0) {
            quantites.merge(id, quantite, Integer::sum);
        } else {
            quantites.remove(id);
        }
        nettoyerJeuxSansStockNiEmprunt();
        return jeu;
    }

    @Override
    public void emprunterJeu(String emprunteur, String idJeu) {
        if (emprunteur == null || emprunteur.isBlank()) {
            throw new IllegalArgumentException("L'emprunteur est requis");
        }

        Jeu jeu = jeuxDisponibles.get(idJeu);
        if (jeu == null) {
            throw new IllegalArgumentException("Le jeu n'existe pas");
        }

        boolean dejaEmprunte = emprunts.stream()
                .anyMatch(emprunt -> emprunt.getEmprunteur().equals(emprunteur)
                        && emprunt.getIdJeu().equals(idJeu));
        if (dejaEmprunte) {
            throw new IllegalStateException("Ce jeu est déjà emprunté par cette personne");
        }

        int quantite = quantites.getOrDefault(idJeu, 0);
        if (quantite <= 0) {
            throw new IllegalStateException("Le jeu n'est plus disponible en stock");
        }

        quantites.put(idJeu, quantite - 1);
        emprunts.add(new Emprunt(emprunteur, idJeu));

        if (quantites.get(idJeu) == 0) {
            quantites.remove(idJeu);
        }
        nettoyerJeuxSansStockNiEmprunt();
    }

    @Override
    public void rendreJeu(String emprunteur, String idJeu) {
        Emprunt empruntARendre = emprunts.stream()
                .filter(emprunt -> emprunt.getEmprunteur().equals(emprunteur)
                        && emprunt.getIdJeu().equals(idJeu))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cet emprunt n'existe pas"));

        emprunts.remove(empruntARendre);

        if (jeuxDisponibles.containsKey(idJeu)) {
            quantites.merge(idJeu, 1, Integer::sum);
        }
        nettoyerJeuxSansStockNiEmprunt();
    }

    @Override
    public List<JeuStocke> getJeuxBibliotheque() {
        List<JeuStocke> resultat = new ArrayList<>();
        for (Map.Entry<String, Integer> entree : quantites.entrySet()) {
            Jeu jeu = jeuxDisponibles.get(entree.getKey());
            resultat.add(new JeuStocke(jeu.getId(), entree.getValue()));
        }
        return resultat;
    }

    @Override
    public List<Jeu> getJeuxDisponibles() {
        return new ArrayList<>(jeuxDisponibles.values());
    }

    @Override
    public List<Emprunt> getEmprunts() {
        return new ArrayList<>(emprunts);
    }

    @Override
    public int getQuantite(String idJeu) {
        return quantites.getOrDefault(idJeu, 0);
    }

    @Override
    public void ajouterStock(String idJeu, int quantite) {
        if (!jeuxDisponibles.containsKey(idJeu)) {
            throw new IllegalArgumentException("Le jeu n'existe pas");
        }
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        quantites.merge(idJeu, quantite, Integer::sum);
    }

    @Override
    public void retirerStock(String idJeu, int quantite) {
        if (!jeuxDisponibles.containsKey(idJeu)) {
            throw new IllegalArgumentException("Le jeu n'existe pas");
        }
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }

        int stockActuel = quantites.getOrDefault(idJeu, 0);
        if (quantite > stockActuel) {
            throw new IllegalArgumentException("Quantité supérieure au stock disponible");
        }

        int nouveauStock = stockActuel - quantite;
        if (nouveauStock == 0) {
            quantites.remove(idJeu);
        } else {
            quantites.put(idJeu, nouveauStock);
        }
        nettoyerJeuxSansStockNiEmprunt();
    }

    @Override
    public void supprimerJeu(String idJeu) {
        boolean emprunte = emprunts.stream().anyMatch(emprunt -> emprunt.getIdJeu().equals(idJeu));
        if (emprunte) {
            throw new IllegalStateException("Impossible de supprimer un jeu actuellement emprunté");
        }
        jeuxDisponibles.remove(idJeu);
        quantites.remove(idJeu);
    }

    private void nettoyerJeuxSansStockNiEmprunt() {
        List<String> aSupprimer = new ArrayList<>();

        for (String idJeu : jeuxDisponibles.keySet()) {
            boolean enStock = quantites.containsKey(idJeu);
            boolean emprunte = emprunts.stream().anyMatch(emprunt -> emprunt.getIdJeu().equals(idJeu));

            if (!enStock && !emprunte) {
                aSupprimer.add(idJeu);
            }
        }

        for (String idJeu : aSupprimer) {
            jeuxDisponibles.remove(idJeu);
        }
    }
}
