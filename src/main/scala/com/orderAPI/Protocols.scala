package com.orderAPI

import java.sql.Timestamp

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime

final case class Rate(success: Boolean, timestamp: Timestamp, base: String, dateTime: DateTime, rates: Currency)
final case class Currency(usd: Float)
final case class BittrexPrices(success: Boolean, message: String, result: BittrexResult)
final case class BittrexResult(bid: Float, ask: Float, latest: Float)

trait Protocols extends SprayJsonSupport with DefaultJsonProtocol  {

  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp) = JsNumber(obj.getTime)

    def read(json: JsValue) = json match {
      case JsNumber(time) => new Timestamp(time.toLong)

      case _ => throw new DeserializationException("Date expected")
    }
  }

  implicit object DateFormat extends JsonFormat[DateTime] {
    def write(obj: DateTime) = JsString(obj.toString())
    def read(json: JsValue) = json match {
      case JsString(time) => DateTime.parse(json.toString())

      case _ => throw new DeserializationException("Date expected")
    }
  }

  implicit val currencyFormat : RootJsonFormat[Currency] = jsonFormat1(Currency)
  implicit val rateFormat: RootJsonFormat[Rate] = jsonFormat5(Rate)

  implicit val bittrexResultFormate : RootJsonFormat[BittrexResult] = jsonFormat3(BittrexResult)
  implicit val bittrexPricesFormat : RootJsonFormat[BittrexPrices] = jsonFormat3(BittrexPrices)

}
