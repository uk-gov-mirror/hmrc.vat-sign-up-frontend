/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.utils

import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.Constants.Enrolments._

object EnrolmentUtils {

  implicit class EnrolmentUtils(enrolments: Enrolments) {

    def agentReferenceNumber: Option[String] =
      enrolments getEnrolment agentEnrolmentKey flatMap {
        agentEnrolment =>
          agentEnrolment getIdentifier agentReferenceKey map (_.value)
      }

    def vatNumber: Option[String] =
      enrolments getEnrolment VatDecEnrolmentKey flatMap {
        vatDecEnrolment =>
          vatDecEnrolment getIdentifier VatReferenceKey map (_.value)
      }

    def selfAssessmentUniqueTaxReferenceNumber: Option[String] =
      enrolments getEnrolment IRSAEnrolmentKey flatMap {
        vatDecEnrolment =>
          vatDecEnrolment getIdentifier IRSAReferenceKey map (_.value)

      }

    def companyUtr: Option[String] = {
      enrolments getEnrolment IRCTEnrolmentKey flatMap {
        IRCTEnrolment =>
          IRCTEnrolment getIdentifier IRCTReferenceKey map {
            _.value
          }
      }
    }

    def partnershipUtr: Option[String] =
      enrolments getEnrolment PartnershipEnrolmentKey flatMap {
        vatDecEnrolment =>
          vatDecEnrolment getIdentifier PartnershipReferenceKey map (_.value)

      }

    def mtdVatNumber: Option[String] =
      enrolments getEnrolment MtdVatEnrolmentKey flatMap {
        mtdVatEnrolment =>
          mtdVatEnrolment getIdentifier MtdVatReferenceKey map (_.value)
      }

    def getAnyVatNumber: Option[String] = {
      (vatNumber, mtdVatNumber) match {
        case (Some(vrn), Some(mtdVrn)) if vrn == mtdVrn => Some(vrn)
        case (Some(vrn), Some(mtdVrn)) => throw new InternalServerException(s"Found multiple different vat numbers in enrolments: $vrn and $mtdVrn")
        case (None, Some(mtdVrn)) => Some(mtdVrn)
        case (Some(vrn), None) => Some(vrn)
        case _ => None
      }
    }

  }

}
