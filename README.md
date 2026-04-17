# PNJGui

**FR 🇫🇷 | EN 🇬🇧**

---

## Présentation | About

**PNJGui** est un plugin Minecraft développé pour **Spigot 1.8.8**. Il permet aux joueurs d'acheter, invoquer, vendre et améliorer leur **garde du corps personnel** (PNJ) via une interface graphique intuitive.

**PNJGui** is a Minecraft plugin made for **Spigot 1.8.8**. It allows players to buy, summon, sell, and upgrade their **personal bodyguard NPC** through a simple graphical interface.

---

## Dépendances | Dependencies

- [Citizens 2](https://www.spigotmc.org/resources/citizens.13811/)
- [Sentry (addon Citizens)](https://www.spigotmc.org/resources/sentry.24265/)
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [EssentialsX](https://essentialsx.net/)

---

## 🎮 Commandes | Commands

| Commande | Description | Permission requise |
|---------|-------------|--------------------|
| `/gui` ou `/g` | Ouvre l'interface de gestion du garde du corps | `gui.gui` |
| `/gui reload` ou `/gr` | Recharge la configuration du plugin | `gui.reload` |

---

## Permissions

```yml
gui.*:
  description: Donne accès à toutes les permissions du plugin.
  children:
    gui.gui: true
    gui.reload: true

gui.gui:
  description: Permet d'utiliser la commande /gui.
  default: op

gui.reload:
  description: Permet de recharger la configuration.
  default: op
```

---

## Prix & équipement | Prices & Equipment

Le fichier `config.yml` vous permet de personnaliser :

### Prix

| Action | Prix |
|--------|------|
| Achat du garde du corps | 25 000 |
| Appel du garde du corps | 250 |
| Vente | 12 500 |
| Amélioration Armure Niveau 2 | 25 000 |
| Amélioration Armure Niveau 3 | 25 000 |
| Amélioration Arme Niveau 2 | 12 500 |
| Amélioration Arme Niveau 3 | 12 500 |

### Équipement par niveau

| Niveau | Armure | Arme |
|--------|--------|------|
| Basique | Cuir | Épée en pierre |
| Intermédiaire | Fer | Épée en fer |
| Avancé | Or | Épée en diamant |

---

## Configuration

Toutes les valeurs sont modifiables dans le fichier `config.yml`. Après modification, utilisez la commande `/gui reload` pour appliquer les changements sans redémarrer le serveur.

---

## Installation

1. Placez le fichier `PNJGui.jar` dans le dossier `plugins/` de votre serveur.
2. Assurez-vous que **Citizens**, **Sentry**, **Vault** et **Essentials** sont installés.
3. Redémarrez le serveur.
4. (Optionnel) Configurez les permissions via un plugin comme LuckPerms.
5. Utilisez `/gui` pour ouvrir l’interface.

---

## Auteur | Author

Développé par **sami37**  
Plugin version : `1.0.0`  
Compatibilité : Spigot 1.8.8

---

## Licence

Ce plugin est libre de droit pour un usage personnel ou serveur. La redistribution sans autorisation est interdite.

---
