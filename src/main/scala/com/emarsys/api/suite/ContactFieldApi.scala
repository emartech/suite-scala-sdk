package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.emarsys.formats.SuiteSdkFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait ContactFieldApi extends SuiteClient {

  import ContactFieldApi._

  def list(customerId: Int): Future[ListResponse] = {
    val path    = "field"
    val request = RequestBuilding.Get(Uri(baseUrl(customerId) + path))

    run[ListRawResponse](request).map(listResponseTransformer)
  }

  def list(customerId: Int, languageCode: String): Future[ListResponse] = {
    val path    = s"field/translate/$languageCode"
    val request = RequestBuilding.Get(Uri(baseUrl(customerId) + path))

    run[ListRawResponse](request).map(listResponseTransformer)
  }

  val listResponseTransformer: (ListRawResponse) => ListResponse = {
    case ListRawResponse(c, t, Right(r)) => ListResponse(c, t, r)
    case ListRawResponse(c, t, Left(_)) => ListResponse(c, t, Nil)
  }
}

object ContactFieldApi {

  case class FieldItem(id: Int, name: String, application_type: String, string_id: String)
  case class ListResponse(replyCode: Int, replyText: String, data: List[FieldItem])
  case class ListRawResponse(replyCode: Int, replyText: String, data: Either[String, List[FieldItem]])

  def apply(eConfig: EscherConfig)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) =
    new SuiteClient with ContactFieldApi {
      override implicit val system       = sys
      override implicit val materializer = mat
      override implicit val executor     = ex
      override val escherConfig          = eConfig
    }
}
