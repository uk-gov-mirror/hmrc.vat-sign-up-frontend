/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsignupfrontend.models.SubscriptionRequestSummary

object SubscriptionRequestSummaryHttpParser {
  type GetSubscriptionRequestSummaryResponse = Either[SubscriptionRequestSummaryRetrievalFailure, SubscriptionRequestSummary]

  implicit object GetSubscriptionRequestSummaryHttpReads extends HttpReads[GetSubscriptionRequestSummaryResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetSubscriptionRequestSummaryResponse = {
      response.status match {
        case OK => response.json.validate[SubscriptionRequestSummary].fold[GetSubscriptionRequestSummaryResponse](
          jsonErrors =>
            Left(SubscriptionRequestUnexpectedError(OK, s"JSON does not meet read requirements of SubscriptionRequestSummary")),
          parsedSubReqSumModel =>
            Right(parsedSubReqSumModel)
        )
        case BAD_REQUEST => Left(SubscriptionRequestExistsButNotComplete)
        case NOT_FOUND => Left(SubscriptionRequestDoesNotExist)
        case statusNotExpected => Left(SubscriptionRequestUnexpectedError(statusNotExpected, "Unexpected status from Backend"))
      }
    }
  }

  sealed trait SubscriptionRequestSummaryRetrievalFailure

  case object SubscriptionRequestExistsButNotComplete extends SubscriptionRequestSummaryRetrievalFailure

  case object SubscriptionRequestDoesNotExist extends SubscriptionRequestSummaryRetrievalFailure

  case class SubscriptionRequestUnexpectedError(status: Int, message: String) extends SubscriptionRequestSummaryRetrievalFailure {
    Logger.error(s"SubscriptionRequestUnexpectedError - $status - $message")
  }
}
