package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.emarsys.formats.SuiteSdkFormats._
import com.emarsys.escher.akka.http.config.EscherConfig
import com.emarsys.api.suite.DataTransformers._

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait SegmentApi extends SuiteClient {

  import SegmentApi._

  def create(customerId: Int, payload: SegmentCreatePayload): Future[GetDataResponse] = {
    val path = "filter"
    val request = RequestBuilding.Put(Uri(baseUrl(customerId) + path), payload)

    run[GetDataRawResponse](request).map(getDataResponseTransformer)
  }

}

object SegmentApi {
  case class SegmentCreatePayload(name: String, contactCriteria: ContactCriteria,
                                  description: String, baseContactListId: Option[Int])

  sealed trait ContactCriteria
  case class ContactCriteriaBranch(typef: String, children: List[ContactCriteria]) extends ContactCriteria
  case class ContactCriteriaLeaf(typef: String, field: String, operator: String, value: String) extends ContactCriteria

  case class SegmentCreateRawResponse(string: String)

  def apply(eConfig: EscherConfig)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) = {

    new SuiteClient with SegmentApi {
      override implicit val system       = sys
      override implicit val materializer = mat
      override implicit val executor     = ex
      override val escherConfig          = eConfig
    }
  }
}
