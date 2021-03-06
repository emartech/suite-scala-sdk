package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.model.StatusCodes._
import com.emarsys.api.suite.ContactFieldApi.FieldItem
import com.emarsys.escher.akka.http.config.EscherConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class ContactFieldApiSpec extends AsyncWordSpec with Matchers with ScalaFutures {

  implicit val system       = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()
  implicit val executor     = system.dispatcher

  val escherConfig = new EscherConfig(ConfigFactory.load().getConfig("ems-api.escher"))

  object TestContactFieldApi {

    def apply(eConfig: EscherConfig,
              response: HttpResponse)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) =
      new SuiteClient with ContactFieldApi {
        override implicit val system       = sys
        override implicit val materializer = mat
        override implicit val executor     = ex
        override val escherConfig          = eConfig

        override lazy val connectionFlow = Flow[HttpRequest].map(_ => response)
      }
  }

  val customerId        = 123
  val invalidResponse   = "invalid"
  val emptyDataResponse = """{"replyCode":1,"replyText":"Unauthorized","data":""}"""
  val validResponse     = """{
                        |  "replyCode": 0,
                        |  "replyText": "OK",
                        |  "data": [
                        |    {
                        |      "id": 0,
                        |      "name": "Interests",
                        |      "application_type": "interests",
                        |      "string_id": "interests"
                        |    },
                        |    {
                        |      "id": 9,
                        |      "name": "Title",
                        |      "application_type": "single choice",
                        |      "string_id": ""
                        |    },
                        |    {
                        |      "id": 11,
                        |      "name": "Predict something",
                        |      "application_type": "simple",
                        |      "string_id": ""
                        |    },
                        |    {
                        |      "id": 13,
                        |      "name": "another predict",
                        |      "application_type": "multi choice",
                        |      "string_id": ""
                        |    }
                        |  ]
                        |}""".stripMargin

  "ContactField Api" when {

    "contact fields list called" should {

      "return existing fields in case of successful response" in {
        contactField(OK, validResponse).list(customerId) map { response =>
          response.data shouldEqual List(
            FieldItem(0, "Interests", "interests", "interests"),
            FieldItem(9, "Title", "single choice", ""),
            FieldItem(11, "Predict something", "simple", ""),
            FieldItem(13, "another predict", "multi choice", "")
          )
        }
      }

      "return translated existing fields for in case of successful response" in {
        contactField(OK, validResponse).list(customerId, "en") map { response =>
          response.data shouldEqual List(
            FieldItem(0, "Interests", "interests", "interests"),
            FieldItem(9, "Title", "single choice", ""),
            FieldItem(11, "Predict something", "simple", ""),
            FieldItem(13, "another predict", "multi choice", "")
          )
        }
      }

      "return predict fields for customer" in {
        contactField(OK, validResponse).listPredictFields(customerId) map { response =>
          response.data shouldEqual List(
            FieldItem(11, "Predict something", "simple", ""),
            FieldItem(13, "another predict", "multi choice", "")
          )
        }
      }

      "return translated predict fields for customer" in {
        contactField(OK, validResponse).listPredictFields(customerId, "en") map { response =>
          response.data shouldEqual List(
            FieldItem(11, "Predict something", "simple", ""),
            FieldItem(13, "another predict", "multi choice", "")
          )
        }
      }

      "return empty list for data in case of empty data response" in {
        contactField(OK, emptyDataResponse).list(customerId, "en") map { response =>
          response.data shouldEqual List()
        }
      }

      "return empty list for data in case of empty data response and unsuccessful http status" in {
        recoverToSucceededIf[Exception] {
          contactField(Unauthorized, emptyDataResponse).list(customerId, "en")
        }
      }

      "return failure in case of failed response" in {
        recoverToSucceededIf[Exception] {
          contactField(Unauthorized, invalidResponse).list(customerId)
        }
      }
    }
  }

  private def contactField(httpStatus: StatusCode, requestEntity: String) = {
    TestContactFieldApi(escherConfig,
                        HttpResponse(httpStatus, Nil, HttpEntity(ContentTypes.`application/json`, requestEntity)))
  }
}
