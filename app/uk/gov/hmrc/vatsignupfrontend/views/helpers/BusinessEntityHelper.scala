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

package uk.gov.hmrc.vatsignupfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.vatsignupfrontend.models._

object BusinessEntityHelper {
  def getBusinessEntityName(businessEntity: BusinessEntity)(implicit messages: Messages) = {
    businessEntity match {
      case SoleTrader => Messages("core.capture_entity_type.soleTrader")
      case GeneralPartnership => Messages("core.capture_entity_type.generalPartnership")
      case _: LimitedPartnershipBase => Messages("core.capture_entity_type.limitedPartnership")
      case LimitedCompany | Overseas => Messages("core.capture_entity_type.limitedCompany")
      case VatGroup => Messages("core.capture_entity_type.vatGroup")
      case Division => Messages("core.capture_entity_type.division")
      case Trust => Messages("core.capture_entity_type.trust")
      case UnincorporatedAssociation => Messages("core.capture_entity_type.unincorporatedAssociation")
      case RegisteredSociety => Messages("core.capture_entity_type.registeredSociety")
      case Charity => Messages("core.capture_entity_type.charity")
      case GovernmentOrganisation => Messages("core.capture_entity_type.governmentOrganisation")
      case Other => Messages("core.capture_entity_type.other")
    }
  }
}
