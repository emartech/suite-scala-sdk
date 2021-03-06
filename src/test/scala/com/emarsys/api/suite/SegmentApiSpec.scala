package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import com.emarsys.api.suite.SegmentApi.{BehaviorCriteriaLeaf, ContactCriteriaLeaf, CreateRequest}
import com.emarsys.escher.akka.http.config.EscherConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class SegmentApiSpec extends AsyncWordSpec with Matchers with ScalaFutures {

  implicit val system       = ActorSystem("segment-api-test-system")
  implicit val materializer = ActorMaterializer()
  implicit val executor     = system.dispatcher

  val escherConfig = new EscherConfig(ConfigFactory.load().getConfig("ems-api.escher"))

  object TestSegmentApi {

    def apply(eConfig: EscherConfig,
              response: HttpResponse)(
      implicit
      sys: ActorSystem,
      mat: Materializer,
      ex: ExecutionContextExecutor) =

      new SuiteClient with SegmentApi {
        override implicit val system       = sys
        override implicit val materializer = mat
        override implicit val executor     = ex
        override val escherConfig          = eConfig

        override lazy val connectionFlow = Flow[HttpRequest].map(_ => response)
      }
  }

  val createdResponse = """{"replyCode":0,"replyText":"OK","data":{"id":"100024015"}}"""
  val validationFailedResponse =
    """{"replyCode":1008,"replyText":"Validation error: Criteria cannot have children.","data":""}"""
  val customerId = 123

  "Segment Api" when {

    "create segment called vith valid payload" should {
      "return with valid response" in {

        val contactCriteriaLeaf  = ContactCriteriaLeaf("criteria", Right("email"), "contains", "@gmail.com")
        val behaviorCriteriaLeaf = BehaviorCriteriaLeaf("criteria")
        val payloadOneLeaf       = CreateRequest("segment", Some(contactCriteriaLeaf), Some(behaviorCriteriaLeaf), "", None)

        segmentApi(StatusCodes.OK, createdResponse)
          .create(customerId, payloadOneLeaf)
          .map(response => response.id shouldEqual 100024015)
      }

      "return with validation error response" in {
        recoverToSucceededIf[Exception] {
          segmentApi(StatusCodes.BadRequest, validationFailedResponse).create(
            customerId,
            CreateRequest("segment", Some(ContactCriteriaLeaf("criteria", Right("email"), "contains", "@gmail.com")), Some(BehaviorCriteriaLeaf("criteria")), "", None))
        }
      }
    }
  }

  def segmentApi(httpStatus: StatusCode, requestEntity: String) =
    TestSegmentApi(escherConfig,
                   HttpResponse(httpStatus, Nil, HttpEntity(ContentTypes.`application/json`, requestEntity)))
}
