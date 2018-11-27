package com.orderAPI

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.pattern.pipe

import scala.concurrent.Future

final case class Price(price:Int)
final case class Prices(prices:Seq[Price])


class PriceRegistryActor extends Actor with ActorLogging {

  import PriceRegistryActor._
  implicit val system = context.system
  implicit val executionContext = context.dispatcher

  def receive: Receive = {
    //Crypto = BTC or ETH
    //Currency = EUR
    case GetPrice(crypto, currency) =>
      val currencyUSDrateQuery = fixerbaseURL +"?access_key=" + fixerAccessKey + "&base=" + currency +"&symbol=" + "USD"
      val query = baseurl + marketurl + "USD-" + crypto
      for{
        responseUSDRate <- Http ().singleRequest (HttpRequest (uri = currencyUSDrateQuery))
        
      }
        //val query = baseurl + priceurl + priceCoinUrl + crypto + priceCurrencyUrl + currency
        val responseUSDRate: Future[HttpResponse] = Http ().singleRequest (HttpRequest (uri = currencyUSDrateQuery))
        val responseFuture: Future[HttpResponse] = Http ().singleRequest (HttpRequest (uri = query))
        responseFuture pipeTo sender ()
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
  final case class GetPrice(typeOrd:String, crypto:String, currency:String)

  def props: Props = Props[PriceRegistryActor]

}
