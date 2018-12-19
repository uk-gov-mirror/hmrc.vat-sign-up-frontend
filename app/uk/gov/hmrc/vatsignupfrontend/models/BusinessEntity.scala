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

package uk.gov.hmrc.vatsignupfrontend.models

import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.SessionFormatter

sealed trait BusinessEntity {

  import BusinessEntity._

  override def toString: String = this match {
    case LimitedCompany => LimitedCompanyKey
    case SoleTrader => SoleTraderKey
    case GeneralPartnership => GeneralPartnershipKey
    case LimitedPartnership => LimitedPartnershipKey
    case VatGroup => VatGroupKey
    case Division => DivisionKey
    case UnincorporatedAssociation => UnincorporatedAssociationKey
    case Trust => TrustKey
    case RegisteredSociety => RegisteredSocietyKey
    case Other => OtherKey
  }
}

object GeneralPartnership extends BusinessEntity

trait LimitedPartnershipBase extends BusinessEntity

object LimitedPartnership extends LimitedPartnershipBase

object LimitedLiabilityPartnership extends LimitedPartnershipBase

object ScottishLimitedPartnership extends LimitedPartnershipBase

object LimitedCompany extends BusinessEntity

object SoleTrader extends BusinessEntity

object VatGroup extends BusinessEntity

object Division extends BusinessEntity

object UnincorporatedAssociation extends BusinessEntity

object Trust extends BusinessEntity

object RegisteredSociety extends BusinessEntity

object Other extends BusinessEntity

object BusinessEntity {
  val LimitedCompanyKey = "limited-company"
  val SoleTraderKey = "sole-trader"
  val GeneralPartnershipKey = "general-partnership"
  val LimitedPartnershipKey = "limited-partnership"
  val LimitedLiabilityPartnershipKey = "llp"
  val ScottishLimitedPartnershipKey = "scottish-partnership"
  val VatGroupKey = "vat-group"
  val DivisionKey = "division"
  val UnincorporatedAssociationKey = "unincorporated-association"
  val TrustKey = "trust"
  val RegisteredSocietyKey = "registered-society"
  val OtherKey = "other"

  implicit object BusinessEntitySessionFormatter extends SessionFormatter[BusinessEntity] {
    override def fromString(string: String): Option[BusinessEntity] = string match {
      case LimitedCompanyKey => Some(LimitedCompany)
      case SoleTraderKey => Some(SoleTrader)
      case GeneralPartnershipKey => Some(GeneralPartnership)
      case LimitedPartnershipKey => Some(LimitedPartnership)
      case LimitedLiabilityPartnershipKey => Some(LimitedLiabilityPartnership)
      case ScottishLimitedPartnershipKey => Some(ScottishLimitedPartnership)
      case VatGroupKey => Some(VatGroup)
      case DivisionKey => Some(Division)
      case UnincorporatedAssociationKey => Some(UnincorporatedAssociation)
      case TrustKey => Some(Trust)
      case RegisteredSocietyKey => Some(RegisteredSociety)
      case OtherKey => Some(Other)
      case _ => None
    }

    override def toString(entity: BusinessEntity): String = entity match {
      case LimitedCompany => LimitedCompanyKey
      case SoleTrader => SoleTraderKey
      case GeneralPartnership => GeneralPartnershipKey
      case LimitedPartnership => LimitedPartnershipKey
      case LimitedLiabilityPartnership => LimitedLiabilityPartnershipKey
      case ScottishLimitedPartnership => ScottishLimitedPartnershipKey
      case VatGroup => VatGroupKey
      case Division => DivisionKey
      case UnincorporatedAssociation => UnincorporatedAssociationKey
      case Trust => TrustKey
      case RegisteredSociety => RegisteredSocietyKey
      case Other => OtherKey
    }
  }

}
