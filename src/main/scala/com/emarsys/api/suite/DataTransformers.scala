package com.emarsys.api.suite

object DataTransformers {
  type GetDataResultPart = List[Map[String, Option[String]]]

  case class GetDataRequest(keyId: String, keyValues: List[String], fields: Option[List[String]])
  case class GetDataResponse(replyCode: Int, replyText: String, data: GetDataResult)

  case class GetDataError(key: String, errorCode: Int, errorMsg: String)
  case class GetDataResult(result: GetDataResultPart, errors: List[GetDataError])
  case class GetDataRawResult(result: Either[Boolean, GetDataResultPart], errors: List[GetDataError])
  case class GetDataRawResponse(replyCode: Int, replyText: String, data: Either[String, GetDataRawResult])

  val getDataResultTransformer: (GetDataRawResult) => GetDataResult = {
    case GetDataRawResult(Right(r), e) => GetDataResult(r, e)
    case GetDataRawResult(Left(_), e)  => GetDataResult(Nil, e)
  }

  val getDataResponseTransformer: (GetDataRawResponse) => GetDataResponse = {
    case GetDataRawResponse(c, t, Right(d)) => GetDataResponse(c, t, getDataResultTransformer(d))
    case GetDataRawResponse(c, t, Left(_))  => GetDataResponse(c, t, GetDataResult(Nil, Nil))
  }
}
