package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import com.emarsys.api.Config.suiteConfig
import com.emarsys.api.suite.DataTransformers.{GetDataResponse, GetDataResult}
import com.emarsys.api.suite.SegmentApi.{
  ContactCriteriaLeaf,
  SegmentCreatePayload
}
import com.emarsys.escher.akka.http.config.EscherConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.{ExecutionContextExecutor, Future}

class SegmentApiSpec extends AsyncWordSpec with Matchers with ScalaFutures {

  implicit val system = ActorSystem("segment-api-test-system")
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  val escherConfig = new EscherConfig(
    ConfigFactory.load().getConfig("ems-api.escher"))

  object TestSegmentApi {

    def apply(eConfig: EscherConfig, response: HttpResponse)(
        implicit sys: ActorSystem,
        mat: Materializer,
        ex: ExecutionContextExecutor) =
      new SuiteClient with SegmentApi {
        override implicit val system = sys
        override implicit val materializer = mat
        override implicit val executor = ex
        override val escherConfig = eConfig

        override lazy val connectionFlow = Flow[HttpRequest].map(_ => response)
      }
  }

  val createdResponse = """{"replyCode":0,"replyText":"OK","data":""}"""
  val validationFailedResponse = """{"replyCode":1008,"replyText":"Validation error: Criteria cannot have children.","data":""}"""
  val customerId = 123

  "Segment Api" when {

    "create segment called" should {
      "return with valid response" in {
        segmentApi(StatusCodes.OK, createdResponse)
          .create(customerId,
                  SegmentCreatePayload("segment",
                                       ContactCriteriaLeaf("criteria",
                                                           "email",
                                                           "contains",
                                                           "@gmail.com"),
                                       "",
                                       None))
          .map(response => {
            response.replyCode shouldEqual 0
            response.replyText shouldEqual "OK"
          }
        )
      }

//      "return with validation error response" in {
//        segmentApi(StatusCodes.BadRequest, validationFailedResponse)
//          .create(customerId,
//            SegmentCreatePayload("segment",
//              ContactCriteriaLeaf("criteria",
//                "email",
//                "contains",
//                "@gmail.com"),
//              "",
//              None))
//          .map(response => {
//            response.replyCode shouldEqual 1008
//            response.replyText shouldEqual "Validation error: Criteria cannot have children."
//          }
//          )
//      }
    }
  }

  def segmentApi(httpStatus: StatusCode, requestEntity: String) = {
    TestSegmentApi(
      escherConfig,
      HttpResponse(httpStatus,
                   Nil,
                   HttpEntity(ContentTypes.`application/json`, requestEntity)))
  }
}
