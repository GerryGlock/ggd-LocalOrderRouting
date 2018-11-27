package com.orderAPI

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport{

  import DefaultJsonProtocol._

  implicit val priceJsonFormat = jsonFormat1(Price)
  implicit val pricesJsonFormat = jsonFormat1(Prices)


}
