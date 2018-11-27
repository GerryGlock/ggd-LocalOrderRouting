package com.orderAPI

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import spray.json._

import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure


final case class Price(price:Int)
final case class Prices(prices:Seq[Price])


class PriceRegistryActor extends Actor with ActorLogging with Protocols {

  import PriceRegistryActor._
  implicit val system = context.system
  implicit val executionContext = context.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  def receive: Receive = {
    //Crypto = BTC or ETH
    //Currency = EUR
    case GetPrice(crypto, currency) =>
      val currencyUSDrateQuery = fixerbaseURL +"?access_key=" + fixerAccessKey + "&base=" + currency +"&symbol=" + "USD"
      val query = baseurl + marketurl + "USD-" + crypto
      //val query = baseurl + priceurl + priceCoinUrl + crypto + priceCurrencyUrl + currency

      Http ().singleRequest (HttpRequest (uri = currencyUSDrateQuery)) andThen{
        case Success(resp) => {
          (resp.entity.dataBytes.map(_.utf8String)).map(_.parseJson.convertTo[Rate]).runForeach{rate =>
            Http ().singleRequest (HttpRequest (uri = query)) andThen{
              case Success(ans) => {
                (ans.entity.dataBytes.map(_.utf8String)).map(_.parseJson.convertTo[BittrexPrices]).runForeach{
                  res => res.result.copy(ask = res.result.ask*rate.rates.usd, bid = res.result.bid*rate.rates.usd, latest = res.result.latest * rate.rates.usd)
                }
              }
              case Failure(error) => Failure
            }
          }
        }
        case Failure(exception) => Failure
      }

  }
}

object PriceRegistryActor{

  val baseurl = "https://bittrex.com/api/v1.1/public/"
  val fixerbaseURL = "http://data.fixer.io/api/latest"
  val marketurl = "?market="
  //val priceCoinUrl = "fsym="
  //val priceCurrencyUrl = "&tsyms="
  val apikey = "-e7c4e7a115da98000e04253c926a3493541098bd9bb5acc6c4a4acf097eae121"
  val fixerAccessKey = "d7ca3e75dd5dbb789fa4fbd70f53568a"
  final case class ActionPerformed(description: String)
  final case class GetPrice(crypto:String, currency:String)

  def props: Props = Props[PriceRegistryActor]

}
