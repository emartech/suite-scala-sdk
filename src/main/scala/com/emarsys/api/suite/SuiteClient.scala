package com.emarsys.api.suite

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.Flow
import com.emarsys.api.Config.suiteConfig

private[suite] trait SuiteClient extends RestClient {

  val serviceName = suiteConfig.serviceName
  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, _] = Http().outgoingConnectionHttps(suiteConfig.host)

  protected def createCustomerHeader(customerId: Int) = RawHeader("X-SUITE-CUSTOMERID", customerId.toString)

  def baseUrl(customerId: Int) =
    s"https://${suiteConfig.host}:${suiteConfig.port}${suiteConfig.suiteApiPath}/$customerId/"
}
