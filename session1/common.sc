// DEPENDENCIES

import $plugin.$ivy.`org.spire-math:kind-projector_2.13.0-RC1:0.9.10`
import $ivy.`org.slf4j:slf4j-nop:1.7.21`
import $ivy.`org.postgresql:postgresql:42.2.10`
import $ivy.`org.tpolecat::doobie-postgres:0.8.8`
import $ivy.`org.tpolecat::doobie-quill:0.8.8`
import $ivy.`io.getquill::quill-jdbc:3.5.1`

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

def timedAux[A](code: => A, nano: Boolean = false): A = {
    val start = System.nanoTime
    val result: A = code
    val end = System.nanoTime
    val nanos = end -start
    val millis = TimeUnit.NANOSECONDS.toMillis(nanos)
    println(if (nano) s"$nanos nanos" else s"$millis millis")
    result
}

implicit class TimeOp[A](code: => A){
  def timed: A = timedAux(code)
  def timedNano: A = timedAux(code, true)
}

// UTILS

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

implicit def FunctorFilterState[M[_]: Functor, F[_]: Applicative: FunctorFilter] =
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
