package com.orderAPI

import java.sql.Timestamp

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat}

final case class Price(price:Int)
final case class Prices(prices:Seq[Price])
final case class Currency(USD: Float)
final case class Rate(success: Boolean, timestamp: Timestamp, base: String, date: DateTime, rates: Currency)
final case class BittrexResult(Bid: Float, Ask: Float, Last: Float)
final case class BittrexPrices(success: Boolean, message: String, result: BittrexResult)

trait Protocols extends SprayJsonSupport with DefaultJsonProtocol  {

  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp) = JsNumber(obj.getTime)

    def read(json: JsValue) = json match {
      case JsNumber(time) => new Timestamp(time.toLong)

      case _ => throw new DeserializationException("Long expected")
    }
  }

  implicit object DateFormat extends JsonFormat[DateTime] {
    def write(obj: DateTime) = JsString(obj.toString())
    def read(json: JsValue) = json match {
      case JsString(time) => {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        DateTime.parse(time, formatter)
      }

      case _ => throw new DeserializationException("Date expected")
    }
  }

  implicit val priceJsonFormat = jsonFormat1(Price)
  implicit val pricesJsonFormat = jsonFormat1(Prices)

  implicit val currencyFormat : RootJsonFormat[Currency] = jsonFormat1(Currency)
  implicit val rateFormat: RootJsonFormat[Rate] = jsonFormat5(Rate)

  implicit val bittrexResultFormat : RootJsonFormat[BittrexResult] = jsonFormat3(BittrexResult)
  implicit val bittrexPricesFormat : RootJsonFormat[BittrexPrices] = jsonFormat3(BittrexPrices)

}
