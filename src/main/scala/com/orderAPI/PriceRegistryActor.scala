package com.orderAPI

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import spray.json._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import akka.pattern._

class PriceRegistryActor extends Actor with ActorLogging with Protocols {

  import PriceRegistryActor._

  implicit val system = context.system
  implicit val executionContext = context.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private def getUrlAkka[T](url: String, timeout: FiniteDuration = 15 seconds)(implicit parser: String => Option[T], system: ActorSystem, materializer: Materializer): Future[Option[T]] = {
    Http().singleRequest(HttpRequest(uri = url)).flatMap(resp => {
      if (resp.status.isFailure()) {
        Future.successful(None)
      } else {
        val bodyF = resp.entity.toStrict(timeout) map {_.data} map { _.utf8String }
        bodyF.map(body => parser(body))
      }
    })
  }

  private def getCrypto(crypto: String)(implicit system: ActorSystem, materializer: Materializer, execution: ExecutionContext): Future[Option[BittrexPrices]] = {
    val url = baseurl + marketurl + "USD-" + crypto
    log.info("getting crypto prices= "+url)
    implicit val transformer: String => Option[BittrexPrices] =
      result => { Try(result.parseJson.convertTo[BittrexPrices]).toOption }
    getUrlAkka(url) recover { case _ => None }
  }

  private def getCurrency(currency: String)(implicit system: ActorSystem, materializer: Materializer, execution: ExecutionContext): Future[Option[Rate]] = {
    val url = fixerbaseURL + "?access_key=" + fixerAccessKey + "&base=" + currency + "&symbols=" + "USD"
    log.info("getting eur usd rate= "+url)
    implicit val transformer: String => Option[Rate]=
      result => { Try(result.parseJson.convertTo[Rate]).toOption }
    getUrlAkka(url) recover { case _ => None }
  }

  def receive: Receive = {
    //Crypto = BTC or ETH
    //Currency = EUR
    case GetPrice(crypto, currency) =>

      val response = for {
        cryptor <- getCrypto(crypto)
        currencyr <- getCurrency(currency)
      } yield { for{
        a <- currencyr
        b <- cryptor
        _ = log.info(s"Latest ${b.result.Last}")
        _ = log.info(s"Bid ${}")
        _ = log.info(s"Ask ${b.result.Ask}")
      } yield
      b.copy(result = b.result.copy(
              `Ask` = b.result.Ask * a.rates.USD,
              `Bid` = b.result.Bid * a.rates.USD,
              `Last` = b.result.Last * a.rates.USD))
            }
      response pipeTo sender()
  }
}

object PriceRegistryActor{

  val baseurl = "https://bittrex.com/api/v1.1/public/getticker"
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
