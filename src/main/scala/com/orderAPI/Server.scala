package com.orderAPI

import akka.http.scaladsl.Http
import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure

import scala.concurrent.ExecutionContext

object Server extends App {

  implicit val system: ActorSystem = ActorSystem("MyCryptoPrices")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val actor = system.actorOf(PriceRegistryActor.props, "PriceActor")

  val pRoutes = new PriceRoutes(actor)

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(pRoutes.priceRoutes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)

}
