/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.http.Status._
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsignupfrontend.models.{MigratableDates, OverseasTrader}

object VatNumberEligibilityHttpParser {
  type VatNumberEligibilityResponse = Either[VatNumberIneligibleResponse, VatNumberEligibleResponse]

  implicit object VatNumberEligibilityHttpReads extends HttpReads[VatNumberEligibilityResponse] {
    override def read(method: String, url: String, response: HttpResponse): VatNumberEligibilityResponse = {
      response.status match {
        case OK => (response.json \ OverseasTrader.key).asOpt[Boolean] match {
          case Some(overseas) if overseas => Right(OverseasVatNumberEligible)
          case _ => Right(VatNumberEligible)
        }
        case NO_CONTENT => Right(VatNumberEligible)
        case BAD_REQUEST =>
          response.json.validate[MigratableDates] match {
            case JsSuccess(dates, _) => Left(IneligibleForMtdVatNumber(dates))
          }
        case NOT_FOUND => Left(InvalidVatNumber)
        case status => Left(VatNumberEligibilityFailureResponse(status))
      }
    }
  }

  sealed trait VatNumberEligibleResponse

  case object VatNumberEligible extends VatNumberEligibleResponse

  case object OverseasVatNumberEligible extends VatNumberEligibleResponse

  sealed trait VatNumberIneligibleResponse

  case class IneligibleForMtdVatNumber(migratableDates: MigratableDates) extends VatNumberIneligibleResponse

  case object InvalidVatNumber extends VatNumberIneligibleResponse

  case class VatNumberEligibilityFailureResponse(status: Int) extends VatNumberIneligibleResponse

}
