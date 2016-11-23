package com.emarsys.api.suite

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.emarsys.formats.SuiteSdkFormats._
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

private[suite] trait SegmentApi extends SuiteClient {

  import ContactApi._

  def create(customerId: Int) = {

  }
}

object SegmentApi {

  def apply(eConfig: EscherConfig)(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContextExecutor) = {

    new SuiteClient with SegmentApi {
      override implicit val system       = sys
      override implicit val materializer = mat
      override implicit val executor     = ex
      override val escherConfig          = eConfig
    }
  }
}
