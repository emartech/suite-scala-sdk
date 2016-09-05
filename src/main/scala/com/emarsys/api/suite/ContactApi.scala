package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import fommil.sjs.FamilyFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait ContactApi extends SuiteClient {

  import ContactApi._

  def getData(customerId: Int, entity: GetDataRequest): Future[GetDataResponse] = {
    val path    = "contact/getdata"
    val request = RequestBuilding.Post(Uri(baseUrl(customerId) + path), entity)

    run[GetDataResponse](request)
  }
}

object ContactApi {

  type GetDataResultPart = List[Map[String, Option[String]]]

  case class GetDataRequest(keyId: String, keyValues: List[String], fields: Option[List[String]])

  case class GetDataError(key: String, errorCode: String, errorMsg: String)

  case class GetDataResult(result: GetDataResultPart, errors: List[GetDataError])

  case class GetDataResponse(replyCode: Int, replyText: String, data: GetDataResult)

  def apply(eConfig: EscherConfig)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) = {

    new SuiteClient with ContactApi {
      override implicit val system = sys
      override implicit val materializer = mat
      override implicit val executor = ex
      override val escherConfig = eConfig
    }
  }
}
