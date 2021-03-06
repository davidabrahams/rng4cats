package org.dabr.randomcats.examples

import cats.data.EitherK
import cats.free.Free
import cats.implicits._

/**
 * TODO: actually run sequentialWriteFree in the main method
 */
object FreeRandom {

  sealed trait RandomDSL[A]
  case object RandomLong extends RandomDSL[Long]

  import HashedStoreAlgebra._

  type Algebra[A] = EitherK[RandomDSL, HashedStoreDSL, A]

  /**
   * Returns true if all writes were successful
   */
  def sequentialWriteFree(
      writes: List[(Key, Value)]
  ): Free[Algebra, Boolean] =
    writes
      .traverse {
        case (k, v) =>
          Free.inject[RandomDSL, Algebra](RandomLong).map { long =>
            (k, v, Hash(long))
          }
      }
      .flatMap { list =>
        list.traverse {
          case (k, v, h) =>
            Free.inject[HashedStoreDSL, Algebra](Put(k, h, v))
        }
      }
      .map { list =>
        list.forall(identity)
      }
}

object FreeExample {
  def main(args: Array[String]): Unit = {
    println("Hello World")
  }
}
