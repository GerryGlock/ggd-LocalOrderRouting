package com.orderAPI

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.util.Timeout
import com.orderAPI.PriceRegistryActor.GetPrice
import akka.pattern.ask
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class PriceRoutes(val actorRef: ActorRef)(implicit system: ActorSystem, executionContext: ExecutionContext, materializer: ActorMaterializer) extends Protocols {

  lazy val log = Logging(system,classOf[PriceRoutes])

  def priceRegistryActor: ActorRef = actorRef

  implicit lazy val timeout = Timeout.apply(5, TimeUnit.MINUTES)

  lazy val priceRoutes:Route =
    pathPrefix("price"){
      parameters('crypto.as[String], 'currency.as[String]){ (crypto, currency)=>{
            get {
              log.info("gets in get")
              log.info(s"${crypto}"+s"${currency}")
              val price = (priceRegistryActor ? GetPrice(crypto, currency)).mapTo[Option[BittrexPrices]]
              onComplete(price) {
                case Success(Some(ok)) => complete(StatusCodes.OK, ok)
                case Success(None) => complete(StatusCodes.NotFound, "pos no ta")
                case Failure(ko) => complete(StatusCodes.BadRequest, s"BadRequest ${ko.getMessage}")
              }
            }
          }
      }
    }
}

