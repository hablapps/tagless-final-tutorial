# Tagless-final or abstraction without guilt

This repository offers an introduction to the tagless-final approach, as well as a comparison with alternative approaches - the MTL style, in particular. The introduction is largely based on the following two papers:

* [Sound and Efficient Language-Integrated Query - Maintaining the ORDER](https://dl.acm.org/doi/pdf/10.1145/2847538.2847542). By Oleg Kiselyov and Tatsuya Katsushima
* [Finally, safely-extensible and efficient language-integrated query](http://okmij.org/ftp/meta-programming/Sqr/sqr.pdf). By Kenichi Suzuki, Oleg Kiselyov and Yukiyoshi Kameyama

The material is structured around a number of variations on a theme of Language-Integrated Query. In particular, we will focus on six different ways to perform a query in Scala over a relational database:

* [Variation 1](Variation1.JDBC.ipynb). Plain JDBC
* [Variation 2](Variation2.InMemory.ipynb). In-memory queries
* [Variation 3](Variation3.DAO.ipynb). DAOs
* [Variation 4](Variation4.MTL.ipynb). MTL-based repositories
* [Variation 5](Variation5.QDSL.ipynb). Quoted DSLs: Quill
* [Variation 6a](Variation6a.Tagless.ipynb). Tagless-final: QUEΛ
* [Variation 6b](Variation6b.Tagless.ipynb). Tagless-final: From QUEΛ to SQL

Each variation exposes the solution to the problem, and both its advantages and disadvantages. These are mainly framed in the trade-offs between modularity and efficiency conveyed by the use of the different techniques. Tagless-final is presented as one of the techniques to achieve abstraction, without sacrifying efficiency.

### Set-up

