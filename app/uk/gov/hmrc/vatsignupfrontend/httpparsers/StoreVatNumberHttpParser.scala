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
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates


object StoreVatNumberHttpParser {
  type StoreVatNumberResponse = Either[StoreVatNumberFailure, StoreVatNumberSuccess]

  val CodeKey = "CODE"
  val NoRelationshipCode = "RELATIONSHIP_NOT_FOUND"
  val KnownFactsMismatchCode = "KNOWN_FACTS_MISMATCH"
  val SubscriptionClaimedCode = "SUBSCRIPTION_CLAIMED"

  implicit object StoreVatNumberHttpReads extends HttpReads[StoreVatNumberResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreVatNumberResponse = {

      def responseCode: Option[String] = (response.json \ CodeKey).asOpt[String]

      response.status match {
        case CREATED  => Right(VatNumberStored)
        case FORBIDDEN if responseCode contains NoRelationshipCode => Left(NoAgentClientRelationship)
        case FORBIDDEN if responseCode contains KnownFactsMismatchCode => Left(KnownFactsMismatch)
        case PRECONDITION_FAILED => Left(InvalidVatNumber)
        case UNPROCESSABLE_ENTITY => response.json.validate[MigratableDates] match {
          case JsSuccess(dates, _) => Left(IneligibleVatNumber(dates))
        }
        case CONFLICT => Left(AlreadySubscribed)
        case status => Left(StoreVatNumberFailureResponse(status))
      }
    }
  }

  sealed trait StoreVatNumberSuccess

  case object VatNumberStored extends StoreVatNumberSuccess

  sealed trait StoreVatNumberFailure

  case object NoAgentClientRelationship extends StoreVatNumberFailure

  case object KnownFactsMismatch extends StoreVatNumberFailure

  case object AlreadySubscribed extends StoreVatNumberFailure

  case object InvalidVatNumber extends StoreVatNumberFailure

  case class IneligibleVatNumber(migratableDates: MigratableDates) extends StoreVatNumberFailure

  case class StoreVatNumberFailureResponse(status: Int) extends StoreVatNumberFailure

}
