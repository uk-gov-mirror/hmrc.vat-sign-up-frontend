/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.models.companieshouse

import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.SessionFormatter

sealed trait CompanyType

case object LimitedPartnership extends CompanyType

case object LimitedLiabilityPartnership extends CompanyType

case object ScottishPartnership extends CompanyType

case object NonPartnershipEntity extends CompanyType

object CompanyType {

  implicit object CompanyTypeSessionFormatter extends SessionFormatter[CompanyType] {

    val LimitedPartnershipKey: String = LimitedPartnership.toString
    val LimitedLiabilityPartnershipKey: String = LimitedLiabilityPartnership.toString
    val ScottishPartnershipKey: String = ScottishPartnership.toString
    val NonPartnershipEntityKey: String = NonPartnershipEntity.toString

    override def toString(entity: CompanyType): String = entity.toString

    override def fromString(string: String): Option[CompanyType] = string match {
      case LimitedPartnershipKey => Some(LimitedPartnership)
      case LimitedLiabilityPartnershipKey => Some(LimitedLiabilityPartnership)
      case ScottishPartnershipKey => Some(ScottishPartnership)
      case _ => Some(NonPartnershipEntity)
    }
  }

}
