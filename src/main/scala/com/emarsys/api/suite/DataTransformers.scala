package com.emarsys.api.suite

import com.emarsys.api.suite.SuiteClient.SuiteRawResponse

object DataTransformers {
  type GetDataResultPart = List[Map[String, Option[String]]]

  case class GetDataRequest(keyId: String, keyValues: List[String], fields: Option[List[String]])
  case class GetDataResponse(data: GetDataResult)

  case class GetDataError(key: String, errorCode: Int, errorMsg: String)
  case class GetDataResult(result: GetDataResultPart, errors: List[GetDataError])
  case class GetDataRawResult(result: Either[Boolean, GetDataResultPart], errors: List[GetDataError])
  type GetDataRawResponseData = Either[String, GetDataRawResult]

  val getDataResultTransformer: (GetDataRawResult) => GetDataResult = {
    case GetDataRawResult(Right(r), e) => GetDataResult(r, e)
    case GetDataRawResult(Left(_), e)  => GetDataResult(Nil, e)
  }

  val getDataResponseTransformer: (SuiteRawResponse[GetDataRawResponseData]) => GetDataResponse = r => r.data match {
    case Right(d) => GetDataResponse(getDataResultTransformer(d))
    case Left(_)  => GetDataResponse(GetDataResult(Nil, Nil))
  }
}
