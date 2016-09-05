package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.emarsys.escher.akka.http.EscherDirectives
import fommil.sjs.FamilyFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.emarsys.api.Config.suiteConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait SuiteClient extends EscherDirectives {

  implicit val system:       ActorSystem
  implicit val materializer: Materializer
  implicit val executor:     ExecutionContextExecutor

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, _] = Http().outgoingConnectionHttps(suiteConfig.host)

  protected def sendRequest(request: HttpRequest): Future[HttpResponse] = {
    Source.single(request).via(connectionFlow).runWith(Sink.head)
  }

  protected def createCustomerHeader(customerId: Int) = RawHeader("X-SUITE-CUSTOMERID", customerId.toString)

  def baseUrl(customerId: Int) =
    s"https://${suiteConfig.host}:${suiteConfig.port}${suiteConfig.suiteApiPath}/$customerId/"

  def run[S](request: HttpRequest)(implicit um: Unmarshaller[ResponseEntity, S]): Future[S] = {
    for {
    signed   <- signRequest(suiteConfig.serviceName)(executor, materializer)(request)
    response <- sendRequest(signed)
    result   <- response.status match {
            case OK => Unmarshal(response.entity).to[S]
            case status => Unmarshal(response.entity).to[String].map { responseBody =>
              system.log.error("Request to {} failed with status: {} / body: {}", request.uri, status, responseBody)
              throw new Exception(s"Suite client request failed for ${request.uri}")
            }
          }
    } yield result
  }
}