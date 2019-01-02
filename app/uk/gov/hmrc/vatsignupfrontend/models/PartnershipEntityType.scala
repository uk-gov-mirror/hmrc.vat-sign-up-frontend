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

package uk.gov.hmrc.vatsignupfrontend.models

import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.SessionFormatter

sealed trait PartnershipEntityType {
  val StringValue: String

  override def toString: String = StringValue
}

sealed trait LimitedPartnershipEntityType extends PartnershipEntityType

object PartnershipEntityType {

  case object GeneralPartnership extends PartnershipEntityType {
    val StringValue = "generalPartnership"
  }

  case object LimitedPartnership extends LimitedPartnershipEntityType {
    val StringValue = "limitedPartnership"
  }

  case object LimitedLiabilityPartnership extends LimitedPartnershipEntityType {
    val StringValue = "limitedLiabilityPartnership"
  }

  case object ScottishLimitedPartnership extends LimitedPartnershipEntityType {
    val StringValue = "scottishLimitedPartnership"
  }

  val partnershipEntityTypeFrontEndKey = "entityType"

  import play.api.libs.json._

  implicit val writer: Writes[PartnershipEntityType] = new Writes[PartnershipEntityType] {
    def writes(partnershipEntityType: PartnershipEntityType): JsValue =
      JsString(partnershipEntityType.toString)
  }

  implicit object CompanyTypeSessionFormatter extends SessionFormatter[PartnershipEntityType] {

    val LimitedPartnershipKey: String = LimitedPartnership.toString
    val LimitedLiabilityPartnershipKey: String = LimitedLiabilityPartnership.toString
    val ScottishLimitedPartnershipKey: String = ScottishLimitedPartnership.toString

    override def toString(entity: PartnershipEntityType): String = entity.toString

    override def fromString(string: String): Option[PartnershipEntityType] = string match {
      case LimitedPartnershipKey => Some(LimitedPartnership)
      case LimitedLiabilityPartnershipKey => Some(LimitedLiabilityPartnership)
      case ScottishLimitedPartnershipKey => Some(ScottishLimitedPartnership)
    }
  }

}
