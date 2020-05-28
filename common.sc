interp.configureCompiler(_.settings.nowarnings.value = true)

// DEPENDENCIES

import $plugin.$ivy.`org.typelevel::kind-projector:0.10.3`
import $ivy.`org.slf4j:slf4j-nop:1.7.21`
import $ivy.`org.postgresql:postgresql:42.2.10`
import $ivy.`org.tpolecat::doobie-postgres:0.8.8`
import $ivy.`org.tpolecat::doobie-quill:0.8.8`
import $ivy.`org.tpolecat::doobie-hikari:0.8.8`   
import $ivy.`io.getquill::quill-jdbc:3.5.1`
import $ivy.`org.scalatest::scalatest:3.1.2`

// DOOBIE INTERPRETER

import cats._, cats.syntax.all._, cats.data._, cats.instances.all._
import cats.effect.IO, cats.effect.Blocker
import fs2.Stream
import _root_.doobie._, _root_.doobie.implicits._

implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

val xa = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",     // driver classname
  "jdbc:postgresql:world",     // connect URL (driver-specific)
  "postgres",                  // user
  "",                          // password
  Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
)

val y = xa.yolo

// QUILL

import _root_.io.getquill.{ idiom => _, _ }
import _root_.doobie.quill.DoobieContext
import _root_.doobie.util.ExecutionContexts

val dc = new DoobieContext.Postgres(Literal) // Literal naming scheme

// TIMED

import java.util.concurrent.TimeUnit

def timedAux[A](code: => A, times: Int = 1): (A, Long) = {
    def aux(code: => A): (A, Long) = {
        val start = System.nanoTime
        val result: A = code
        val end = System.nanoTime
        (result, end-start)
    }

    val (a, time) = aux(code)
    val avg = (time :: List.fill(times-1)(aux(code)).map(_._2)).reduce(_ + _) / times
    (a, avg)
}

implicit class TimeOp[A](code: => A){
  def timed: (A, Long) =
      timedAux(code, 5)  
  def timed(times: Int = 1): (A, Long) = 
      timedAux(code, times)
}

implicit class ShowTimes[A](result: (A, Long)){
  
  def millis: A = {
      val mill = TimeUnit.NANOSECONDS.toMillis(result._2)
      println(s"$mill millis")
      result._1
  }
  
  def nanos: A = {
      println(s"${result._2} nanos")
      result._1
  }
}

// CATS UTILS

implicit class WithFilterFS2[F[_]: Applicative, A](st: Stream[F, A]){
    def withFilter(f: A => Boolean): Stream[F, A] =
        st.filter(f)
}

implicit class WithFilterCats[F[_]: FunctorFilter, A](f: F[A]){
    def withFilter(p: A => Boolean): F[A] =
        f.filter(p)
}

implicit def MonadTraverse[M[_]: Monad, F[_]: Monad: Traverse, S]: Monad[λ[T => M[F[T]]]] =
    new Monad[λ[T => M[F[T]]]]{
        def flatMap[A, B](p: M[F[A]])(f: A => M[F[B]]) =
            p.flatMap(_.traverse(f).map(_.flatten))

        def pure[A](x: A): M[F[A]] =
            x.pure[F].pure[M]

        def tailRecM[A, B](a: A)(f: A => M[F[Either[A,B]]]): M[F[B]] =
            ??? // TBD
    }

implicit def FunctorFilterM[M[_]: Functor, F[_]: Applicative: FunctorFilter] =
    new FunctorFilter[λ[T => M[F[T]]]]{
        def functor = new Functor[λ[T => M[F[T]]]]{
            def map[A, B](p: M[F[A]])(f: A => B) =
                p.map(_.map(f))
        }
        def mapFilter[A, B](fa: M[F[A]])(f: A => Option[B]): M[F[B]] =
            fa.map(_.mapFilter(f))
}

implicit def MonadStream[F[_]: Applicative] = new Monad[Stream[F, ?]]{
    def flatMap[A, B](p: Stream[F, A])(f: A => Stream[F, B]) =
        p.flatMap(f)
    def pure[A](x: A): fs2.Stream[F,A] =
        Stream.eval(x.pure[F])
    def tailRecM[A, B](a: A)(f: A => Stream[F,Either[A,B]]): Stream[F,B] =
        f(a).flatMap(_.fold(tailRecM(_)(f),Stream.emit)) // TBD
}

implicit def FunctorFilterStream[F[_]: Applicative] = new FunctorFilter[Stream[F, ?]]{
    def functor = new Functor[Stream[F, ?]]{
        def map[A, B](p: Stream[F, A])(f: A=>B) =
            p.map(f)
    }
    def mapFilter[A, B](fa: Stream[F,A])(f: A => Option[B]): Stream[F,B] =
        fa.collect{ a => f(a) match {
            case Some(b) => b
        }}
}

implicit def convertToListOption[F[_]: Functor, G[_], A](fa: F[Option[A]]): F[List[A]] = 
    fa.map(_.toList)

implicit def convertToListId[F[_]: Functor, A](fa: F[A]): F[List[A]] = 
    fa.map(List(_))


// DOOBIE UTILS

implicit class MkFragmentFromList(list: List[Fragment]){
    def mkFragment(sep: Fragment, before: Boolean = true, after: Boolean = true): Fragment = {
        val sep2 = (if (before) fr0" " else fr0"") ++ sep ++ (if (after) fr0" " else fr0"")
        list.flatMap(List(_, sep2)).dropRight(1).combineAll
    }
}