# Projet DAL

## Endpoints:

**GET** localhost:8080/dal/banque/

    Retourne la seule banque possible


**POST** localhost:8080/dal/banque/

    Cree notre banque si elle n'existe pas

**GET** localhost:8080/dal/banque/accounts
    
    Retourne la liste des comptes de nore banque

**POST** localhost:8080/dal/banque/accounts

    Cree un compte dans notre banque

**POST** localhost:8080/dal/banque/stocks

    Ajoute un stock passé dans le body, à la liste des stocks du compte d'utilisateur connecté

**POST** localhost:8080/dal/banque/transformation

    Transforme un stock passé dans le body, en un autre en se basant sur des regles de transformation