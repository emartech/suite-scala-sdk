package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import com.emarsys.escher.akka.http.config.EscherConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class SegmentApiSpec extends AsyncWordSpec with Matchers with ScalaFutures {

  implicit val system       = ActorSystem("contact-api-test-system")
  implicit val materializer = ActorMaterializer()
  implicit val executor     = system.dispatcher

  val escherConfig = new EscherConfig(ConfigFactory.load().getConfig("ems-api.escher"))

  object TestSegmentApi {

    def apply(eConfig: EscherConfig,
              response: HttpResponse)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) =
      new SuiteClient with ContactApi {
        override implicit val system       = sys
        override implicit val materializer = mat
        override implicit val executor     = ex
        override val escherConfig          = eConfig

        override lazy val connectionFlow = Flow[HttpRequest].map(_ => response)
      }
  }

  val customerId = 123

  "Segment Api" when {

    "create segment called" should {
      "return" in {
        1 shouldEqual 1
      }
    }
  }

  def segmentApi(httpStatus: StatusCode, requestEntity: String) = {
    TestSegmentApi(escherConfig,
                   HttpResponse(httpStatus, Nil, HttpEntity(ContentTypes.`application/json`, requestEntity)))
  }
}