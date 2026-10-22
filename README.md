# 🎲 Bibliothèque de jeux de société

Projet de TP — *Architecture nouvelle génération* (Java / Maven), réalisé en binôme.

Une application web de gestion du stock de jeux de société d'une bibliothèque, ainsi que des emprunts associés. Les données sont conservées **en mémoire** (aucune base de données).

## 🧩 Fonctionnalités

### Gestion du stock
Chaque jeu possède :

| Champ | Description |
| --- | --- |
| ID du jeu | Identifiant unique |
| Nom du jeu | Nom du jeu de société |
| Stock | Quantité totale disponible |

### Gestion des emprunts

| Champ | Description |
| --- | --- |
| ID emprunt | Identifiant unique de l'emprunt |
| ID emprunteur | Identifiant de la personne qui emprunte |
| ID jeu | Jeu emprunté |
| Date d'emprunt | Date de création de l'emprunt |
| Date de retour max | Date limite de retour (emprunt + 1 mois) |
| Rendu ? | Statut de l'emprunt (rendu / non rendu / en retard) |

## 📏 Règles métier

1. **Un seul emprunt en cours du même jeu par personne** : un emprunteur ne peut pas emprunter deux fois le même jeu tant qu'il ne l'a pas rendu.
2. **Durée d'emprunt limitée à 1 mois** : la date de retour maximale est calculée à la date d'emprunt + 1 mois ; un retour après cette date marque l'emprunt comme « en retard ».
3. **Pas d'emprunt si le stock restant est à 0** : le stock restant = stock total − emprunts en cours ; un emprunt au-delà est refusé avec une notification explicite.

## 🛠️ Prérequis

- **Java 17** ou supérieur
- **Maven 3.8** ou supérieur

## 🚀 Démarrage

```bash
mvn spring-boot:run
```

L'application est ensuite accessible sur : `http://localhost:8080`

## ✅ Lancer les tests

```bash
mvn clean verify
```

Cette commande unique compile le projet, lance les tests unitaires et d'intégration, et produit le rapport de tests dans `target/site/jacoco/index.html`.

## 🏗️ Architecture

Architecture hexagonale (ports & adapters) :

- `domaine/` — règles métier et entités (aucune dépendance au web)
- `adapters/web/` — interface web (adapter d'entrée)
- `adapters/memoire/` — persistance en mémoire (adapter de sortie)
- `ports/` — interfaces entre le domaine et les adapters

Monolithe modulaire à déploiement unique.

## 📁 Contenu du dépôt

```
├── src/
│   ├── main/java/...   # code source (domaine, ports, adapters)
│   └── test/java/...   # tests unitaires et d'intégration
├── fiche-cadrage.md   # tableau des notions du cours
├── journal-prompts/   # journal des prompts IA (un fichier par membre)
├── bilan-individuel/  # bilan de 10 lignes par membre
└── pom.xml
```
