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
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class PriceRoutes(val actorRef: ActorRef)(implicit system: ActorSystem, executionContext: ExecutionContext, materializer: ActorMaterializer) extends JsonSupport{

  lazy val log = Logging(system,classOf[PriceRoutes])

  def priceRegistryActor: ActorRef = actorRef

  implicit lazy val timeout = Timeout.apply(5, TimeUnit.MINUTES)


  lazy val priceRoutes:Route =
    pathPrefix("price"){
      parameters('crypto.as[String], 'currency.as[String]){ (crypto, currency)=>{
            get {
              val price = (priceRegistryActor ? GetPrice(crypto, currency)).mapTo[HttpResponse]
              onComplete(price) {
                case Success(ok) => complete(StatusCodes.OK, Unmarshal(ok.entity).to[String])
                case Failure(ko) => complete(StatusCodes.NotFound, s"pos no ta ${ko.getMessage}")
              }
            }
          }
      }
    }
}

