# The *TemporalEMF* metamodeling framework

*TemporalEMF* is a temporal metamodeling framework that adds native temporal support for models.
reusing well-known concepts from temporal languages.
In *TemporalMEF* models and can be subjected to temporal queries to retrieve the model contents at different points in time.
We have built our framework on top of the [Eclipse Modeling Framework (EMF)](https://www.eclipse.org/emf/).

Behind the scenes, the history of a model is transparently stored in a B+-Tree. We rely in the [MVStore implementation of the H2 Database](https://www.h2database.com/html/mvstore.html) for this.

## Installation

The TemporalEMF time-aware metamodeling framework and persistence layer can be installed from its Update Site, using the following URL: [https://som-research.github.io/temporal-emf/updates/](https://som-research.github.io/temporal-emf/updates/)

## Building from Eclipse

See pre-defined maven launch configurations in `releng/launch` folder.

## Building from command line

To build the plugins and create an update site, simply execute:

```
mvn clean verify
```

To update the version of all plugins, features, and pom.xml files, simply execute (where x.x.x must be replaced by the desired version number):

```
mvn clean tycho-versions:set-version -Dnewversion=x.x.x-SNAPSHOT
```
