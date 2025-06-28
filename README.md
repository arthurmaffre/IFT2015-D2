# IFT2015-D2
Devoir 2 pour le cours de IFT2015 sur nos ancêtres communs. Le projet vise à creer un simulateur de population et d'inférer les ancêtres communs.

## Mise en route

Le projet est codé en Java et utilise une architecture Maven. Les tests sont effectués avec Maven.

#### Prérequis

* **Java** (JDK 8 ou plus récent)
* **Maven** (mvn doit être dans ton path)

### Commande pour exécuter tous les tests

```bash
mvn test
```

Cette commande effectue tous les tests unitaires.

## Architecture du projet

```
- IFT2015-D2
    - src/
        - main/
            - java/
                - pedigree/
                    - AgeModel.java
                    - Sim.java
    - test/
        - java/
            - pedigree/
                - simtest.java
    - target/
        - ...
    - consignes.pdf
    - pom.xml
    - README.md
```

## Répartition du Travail

Je n'ai pas encore d'idée vraiment claire sur la répartition mais si ca devient plus clair je le mettrais ici.

## Organisation du REPO

Je propose que nous fassions une branche par feature, puis ca me dérange pas que on update direct sur le main. (A voir selon tes préférences)


## License

MIT License

Copyright (c) 2025 Arthur

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.