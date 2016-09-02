package com.emarsys.api.suite

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.emarsys.escher.akka.http.EscherDirectives
import fommil.sjs.FamilyFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.emarsys.api.Config.suiteConfig
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait SuiteClient extends EscherDirectives {

  implicit val system:       ActorSystem
  implicit val materializer: Materializer
  implicit val executor:     ExecutionContextExecutor

  protected def sendRequest(request: HttpRequest): Source[HttpResponse, NotUsed] = {
    Source.single(request).via(Http().outgoingConnectionHttps(suiteConfig.host))
  }

  protected def createCustomerHeader(customerId: Int) = RawHeader("X-SUITE-CUSTOMERID", customerId.toString)

  def baseUrl(customerId: Int) =
    s"https://${suiteConfig.host}:${suiteConfig.port}${suiteConfig.suiteApiPath}/$customerId/"

  def run[S](request: HttpRequest)(implicit um: Unmarshaller[ResponseEntity, S]): Future[S] = {
    for {
      signed   <- signRequest(suiteConfig.serviceName)(executor, materializer)(request)
      response <- sendRequest(signed).runWith(Sink.head)
      result   <- Unmarshal(response.entity).to[S]
    } yield result
  }

}

private[suite] trait ContactFieldApi extends SuiteClient {

  import ContactFieldApi._

  def list(customerId: Int): Future[ListResult] = {
    val path    = "field"
    val request = RequestBuilding.Get(Uri(baseUrl(customerId) + path))

    run[ListResult](request)
  }
}

object ContactFieldApi {

  case class FieldItem(id: Int, name: String, application_type: String)
  case class ListResult(replyCode: Int, replyText: String, data: List[FieldItem])

  def apply(
      eConfig: EscherConfig)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor): ContactFieldApi =
    new SuiteClient with ContactFieldApi {
      override implicit val system: ActorSystem                = sys
      override implicit val materializer: Materializer         = mat
      override implicit val executor: ExecutionContextExecutor = ex
      override val escherConfig: EscherConfig                  = eConfig
    }
}
