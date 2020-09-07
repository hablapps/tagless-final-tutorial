# Tagless-final, or abstraction without guilt

This repository offers an introduction to the tagless-final approach, as well as a comparison with alternative approaches - the MTL style, in particular. The introduction is largely based on the following two papers:

* [Sound and Efficient Language-Integrated Query - Maintaining the ORDER](https://dl.acm.org/doi/pdf/10.1145/2847538.2847542). By Oleg Kiselyov and Tatsuya Katsushima
* [Finally, safely-extensible and efficient language-integrated query](http://okmij.org/ftp/meta-programming/Sqr/sqr.pdf). By Kenichi Suzuki, Oleg Kiselyov and Yukiyoshi Kameyama

The material is structured around a number of variations on a theme of Language-Integrated Query. In particular, we will focus on six different ways to perform a query in Scala over a relational database:

* [Variation 1](Variation1.SQL.ipynb). Plain JDBC
* [Variation 2](Variation2.ListComprehensions.ipynb). In-memory queries
* [Variation 3](Variation3.Repositories.ipynb). Repositories
* [Variation 4](Variation4.MTL.ipynb). MTL-based repositories
* [Variation 5](Variation5.QDSL.ipynb). Quoted DSLs: Quill
* [Variation 6a](Variation6a.TaglessFinal.ipynb). Tagless-final: QUEΛ
* [Variation 6b](Variation6b.TaglessFinal.ipynb). Tagless-final: From QUEΛ to SQL

Each variation exposes the solution to the problem, and both its advantages and disadvantages. These are mainly framed in the trade-offs between modularity and efficiency conveyed by the use of the different techniques. Tagless-final is presented as one of the techniques to achieve abstraction, without sacrifying efficiency.

### Set-up

Throughout this introduction, we will play with the _world_ database used in the documentation of [doobie](https://tpolecat.github.io/doobie), a most convenient JDBC wrapper for Scala. You can find instructions [here](https://tpolecat.github.io/doobie/docs/01-Introduction.html) for the set-up of a postgres database server and the installation of the _world_ database. The script [common.sc](./common.sc) contains several utilities as well as a ready-to-use doobie transactor for issuing SQL queries to the world database.
 
### Presentations

* Madrid Scala Meetup group, 9/6/20 ([slides](https://docs.google.com/presentation/d/13aIIeicm4r-9iorLmVBuezPnzkyh1E-bDDxuNtp8e4w/edit?usp=sharing), [video (Spanish)](https://youtu.be/IjrR5isbGo0))
* Scala Bay meetup, 5/9/20 ([slides](https://docs.google.com/presentation/d/1ZfYILkFhTdlGiG8QmmkoaebH4DdbTKGK3P9jbl8CH48/edit?usp=sharing))