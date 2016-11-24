package com.emarsys.formats

import com.emarsys.api.suite.DataTransformers.GetDataRawResponseData
import com.emarsys.api.suite.SegmentApi.ContactCriteria
import com.emarsys.api.suite.SuiteClient.SuiteRawResponse
import fommil.sjs.FamilyFormats
import spray.json.JsonFormat
import spray.json._
import shapeless._

object SuiteSdkFormats extends DefaultJsonProtocol with FamilyFormats  {

    implicit override def eitherFormat[A, B](implicit a: JsonFormat[A], b: JsonFormat[B])  = super.eitherFormat[A, B]

    implicit val getDataRawResponseF: JsonFormat[SuiteRawResponse[GetDataRawResponseData]] = cachedImplicit
    implicit val contactCriteriaF   : JsonFormat[ContactCriteria]                          = cachedImplicit

    override implicit def coproductHint[T: Typeable]: CoproductHint[T] = new FlatCoproductHint[T]("coproductType")

}