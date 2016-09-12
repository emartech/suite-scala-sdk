package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.emarsys.formats.SuiteSdkFormats._
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait ContactApi extends SuiteClient {

  import ContactApi._

  def getData(customerId: Int, entity: GetDataRequest): Future[GetDataResponse] = {
    val path    = "contact/getdata"
    val request = RequestBuilding.Post(Uri(baseUrl(customerId) + path), entity)

    run[GetDataRawResponse](request).map(getDataResponseTransformer)
  }

  val getDataResultTransformer: (GetDataRawResult) => GetDataResult = {
    case GetDataRawResult(Right(r), e) => GetDataResult(r, e)
    case GetDataRawResult(Left(_), e)  => GetDataResult(Nil, e)
  }

  val getDataResponseTransformer: (GetDataRawResponse) => GetDataResponse = {
    case GetDataRawResponse(c, t, Right(d)) => GetDataResponse(c, t, getDataResultTransformer(d))
    case GetDataRawResponse(c, t, Left(_))  => GetDataResponse(c, t, GetDataResult(Nil, Nil))
  }
}

object ContactApi {

  type GetDataResultPart = List[Map[String, Option[String]]]

  case class GetDataRequest(keyId: String, keyValues: List[String], fields: Option[List[String]])
  case class GetDataResponse(replyCode: Int, replyText: String, data: GetDataResult)

  case class GetDataError(key: String, errorCode: Int, errorMsg: String)
  case class GetDataResult(result: GetDataResultPart, errors: List[GetDataError])
  case class GetDataRawResult(result: Either[Boolean, GetDataResultPart], errors: List[GetDataError])
  case class GetDataRawResponse(replyCode: Int, replyText: String, data: Either[String, GetDataRawResult])

  def apply(eConfig: EscherConfig)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) = {

    new SuiteClient with ContactApi {
      override implicit val system       = sys
      override implicit val materializer = mat
      override implicit val executor     = ex
      override val escherConfig          = eConfig
    }
  }
}
