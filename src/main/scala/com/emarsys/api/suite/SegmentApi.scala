package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.emarsys.api.suite.SuiteClient.SuiteRawResponse
import com.emarsys.formats.SuiteSdkFormats._
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait SegmentApi extends SuiteClient {

  import SegmentApi._

  def create(customerId: Int, payload: CreateRequest): Future[CreateResponse] = {
    val path    = "filter"
    val request = RequestBuilding.Put(Uri(baseUrl(customerId) + path), payload)

    run[CreateRawResponseData](request) map createTransformer
  }
}

object SegmentApi {
  case class CreateRequest(name: String,
                           contactCriteria: ContactCriteria,
                           description: String,
                           baseContactListId: Option[Int])

  sealed trait ContactCriteria
  case class ContactCriteriaBranch(`type`: String, children: List[ContactCriteria]) extends ContactCriteria
  case class ContactCriteriaLeaf(`type`: String, field: String, operator: String, value: String)
      extends ContactCriteria

  case class CreateRawResponseData(id: String)
  case class CreateResponse(id: Int)

  val createTransformer: SuiteRawResponse[CreateRawResponseData] => CreateResponse = r => CreateResponse(r.data.id.toInt)

  def apply(eConfig: EscherConfig)(
    implicit
    sys: ActorSystem,
    mat: Materializer,
    ex: ExecutionContextExecutor): SegmentApi = {

    new SuiteClient with SegmentApi {
      override implicit val system       = sys
      override implicit val materializer = mat
      override implicit val executor     = ex
      override val escherConfig          = eConfig
    }
  }
}
