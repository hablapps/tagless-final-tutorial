trait BaseExpr[Repr[_]]{

    def bool(b: Boolean): Repr[Boolean]
    def int(i: Int): Repr[Int]
    def str(s: String): Repr[String]

    def ===(a1: Repr[Int], a2: Repr[Int]): Repr[Boolean]
    def >(i1: Repr[Int], i2: Repr[Int]): Repr[Boolean]
    // lots of relational operators more ...
}

trait ADTExpr[Repr[_]]{

    def tuple2[A, B](a: Repr[A], b: Repr[B]): Repr[(A, B)]

    def none[A]: Repr[Option[A]]
    def some[A](a: Repr[A]): Repr[Option[A]]
    def exists[A](o: Repr[Option[A]])(
        cond: Repr[A] => Repr[Boolean]): Repr[Boolean]
}

trait MultisetExpr[Repr[_]]{
    def table[A](name: String): Repr[List[A]]
    def from[A, B](q: Repr[List[A]])(f: Repr[A] => Repr[List[B]]): Repr[List[B]]
    def where[A](cond: Repr[Boolean])(q: Repr[List[A]]): Repr[List[A]]
    def select[A](a: Repr[A]): Repr[List[A]]
    // omitted: multiset union, empty test, ...
}

trait QUEΛ[Repr[_]] extends BaseExpr[Repr]
    with ADTExpr[Repr]
    with MultisetExpr[Repr]

object QUEΛ{
    def apply[Repr[_]](implicit Q: QUEΛ[Repr]) = Q
}


// Case classes, as before

case class Country(code: String, name: String, capital: Option[Int])
case class City(id: Int, name: String, countryCode: String, population: Int)

// Data models as type constructor (representation) classes

trait CityModel[Repr[_]]{
    def cityId(city: Repr[City]): Repr[Int]
    def cityName(city: Repr[City]): Repr[String]
    def cityCountry(country: Repr[City]): Repr[String]
    def cityPopulation(city: Repr[City]): Repr[Int]
}

trait CountryModel[Repr[_]]{
    def countryName(country: Repr[Country]): Repr[String]
    def countryCapital(country: Repr[Country]): Repr[Option[Int]]
}

trait WorldModel[Repr[_]] extends CityModel[Repr] with CountryModel[Repr]{
    def allCountries: Repr[List[Country]]
    def allCities: Repr[List[City]]
}

object WorldModelSyntax{
    def allCountries[Repr[_]](implicit W: WorldModel[Repr]) =
        W.allCountries
    def allCities[Repr[_]](implicit W: WorldModel[Repr]) =
        W.allCities

    implicit class CountryFields[Repr[_]](country: Repr[Country])(implicit W: WorldModel[Repr]){
        def name = W.countryName(country)
        def capital = W.countryCapital(country)
    }

    implicit class CityFields[Repr[_]](city: Repr[City])(implicit W: WorldModel[Repr]){
        def id = W.cityId(city)
        def name = W.cityName(city)
        def population = W.cityPopulation(city)
    }
}

object QUEΛSyntax{
    implicit class ComprehensionOps[Repr[_], A](la: Repr[List[A]])(implicit Q: QUEΛ[Repr]){
        def flatMap[B](cont: Repr[A] => Repr[List[B]]): Repr[List[B]] =
            Q.from(la)(cont)
        def map[B](f: Repr[A] => Repr[B]): Repr[List[B]] =
            Q.from(la)(a => Q.select(f(a)))
        def withFilter(p: Repr[A] => Repr[Boolean]): Repr[List[A]] =
            Q.from(la)(a => Q.where(p(a))(Q.select(a)))
    }

    implicit class BinOps[Repr[_]](a1: Repr[Int])(implicit Q: QUEΛ[Repr]){
        def ===(a2: Repr[Int]): Repr[Boolean] = Q.===(a1, a2)
        def >(a2: Repr[Int]): Repr[Boolean] = Q.>(a1, a2)
    }

    implicit def Tuple2QUEΛ[Repr[_], A, B](t2: (Repr[A], Repr[B]))(implicit Q: QUEΛ[Repr]): Repr[(A, B)] =
        Q.tuple2(t2._1, t2._2)

    implicit class OptionOps[Repr[_], A](o: Repr[Option[A]])(implicit Q: QUEΛ[Repr]){
        def exists(cond: Repr[A] => Repr[Boolean]): Repr[Boolean] =
            Q.exists(o)(cond)
    }

    implicit def IntQUEΛ[Repr[_]](i: Int)(implicit Q: QUEΛ[Repr]): Repr[Int] =
        Q.int(i)
}

