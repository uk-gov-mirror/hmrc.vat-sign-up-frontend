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

package uk.gov.hmrc.vatsignupfrontend.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, ReSignUpJourney}
import uk.gov.hmrc.vatsignupfrontend.connectors.{VatNumberEligibilityConnector, VatNumberEligibilityPreMigrationConnector}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityPreMigrationHttpParser.{IneligibleForMtdVatNumber, InvalidVatNumber, VatNumberEligible}
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService.StoreVatNumberOrchestrationServiceResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckVatNumberEligibilityService @Inject()(vatNumberEligibilityPreMigrationConnector: VatNumberEligibilityPreMigrationConnector,
                                                 vatNumberEligibilityConnector: VatNumberEligibilityConnector
                                                )(implicit ec: ExecutionContext) extends FeatureSwitching {

  def checkEligibility(vatNumber: String)(implicit headerCarrier: HeaderCarrier): Future[StoreVatNumberOrchestrationServiceResponse] = {

    if (isEnabled(ReSignUpJourney))
      vatNumberEligibilityConnector.checkVatNumberEligibility(vatNumber).map {
        case Right(MigrationInProgress) => StoreVatNumberOrchestrationService.MigrationInProgress
        case Right(AlreadySubscribed) => StoreVatNumberOrchestrationService.AlreadySubscribed
        case Right(Ineligible) => StoreVatNumberOrchestrationService.Ineligible
        case Right(Inhibited(inhibitedDates)) => StoreVatNumberOrchestrationService.Inhibited(inhibitedDates)
        case Right(Eligible(isOverseas, isMigrated)) => StoreVatNumberOrchestrationService.Eligible(isOverseas, isMigrated)
        case _ => throw new InternalServerException("Unexpected response from new VAT number eligibility backend")
      }
    else
      vatNumberEligibilityPreMigrationConnector.checkVatNumberEligibility(vatNumber).map {
        case Right(VatNumberEligible(isOverseas)) => StoreVatNumberOrchestrationService.Eligible(isOverseas, isMigrated = false)
        case Left(IneligibleForMtdVatNumber(dates)) if dates.isEmpty => StoreVatNumberOrchestrationService.Ineligible
        case Left(IneligibleForMtdVatNumber(dates)) => StoreVatNumberOrchestrationService.Inhibited(dates)
        case Left(InvalidVatNumber) => StoreVatNumberOrchestrationService.InvalidVatNumber
        case _ => throw new InternalServerException("Unexpected response from the old VAT number eligibility backend")
      }
  }

}


