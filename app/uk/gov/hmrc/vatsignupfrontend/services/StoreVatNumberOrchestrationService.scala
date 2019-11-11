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
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.httpparsers.ClaimSubscriptionHttpParser
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService._
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreVatNumberOrchestrationService @Inject()(checkVatNumberEligibilityService: CheckVatNumberEligibilityService,
                                                   storeMigratedVatNumberService: StoreMigratedVatNumberService,
                                                   storeVatNumberService: StoreVatNumberService,
                                                   claimSubscriptionService: ClaimSubscriptionService
                                                  )(implicit ec: ExecutionContext) extends FeatureSwitching {

  def orchestrate(enrolments: Enrolments, vatNumber: String)(implicit headerCarrier: HeaderCarrier): Future[StoreVatNumberOrchestrationServiceResponse] =
    enrolments.getAnyVatNumber match {
      case Some(enrolmentVrn) =>
        checkVatNumberEligibilityService.checkEligibility(vatNumber).flatMap {
          case Eligible(_, isMigrated) if isMigrated =>
            storeMigratedVatNumberService.storeVatNumber(vatNumber, None, None)
          case Eligible(_, _) if enrolments.mtdVatNumber.isEmpty =>
            storeVatNumber(vatNumber)
          case Eligible(_, _) =>
            Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed)
          case StoreVatNumberOrchestrationService.AlreadySubscribed if enrolments.mtdVatNumber.isEmpty =>
            claimSubscription(vatNumber)
          case eligibilityResponse =>
            Future.successful(eligibilityResponse)
        }
      case None if enrolments.agent.isDefined =>
        checkVatNumberEligibilityService.checkEligibility(vatNumber).flatMap {
          case Eligible(_, isMigrated) if isMigrated =>
            storeMigratedVatNumberService.storeVatNumber(vatNumber, None, None)
          case Eligible(_, _) =>
            storeVatNumberDelegated(vatNumber)
          case eligibilityResponse =>
            Future.successful(eligibilityResponse)
        }
      case None =>
        checkVatNumberEligibilityService.checkEligibility(vatNumber)
    }


  private def storeVatNumberDelegated(vatNumber: String)(implicit hc: HeaderCarrier): Future[StoreVatNumberOrchestrationServiceResponse] = {
    storeVatNumberService.storeVatNumberDelegated(vatNumber).map {
      case Right(StoreVatNumberService.VatNumberStored(isOverseas, isDirectDebit)) =>
        StoreVatNumberOrchestrationService.VatNumberStored(isOverseas, isDirectDebit, isMigrated = false)
      case Left(StoreVatNumberService.NoAgentClientRelationship) =>
        StoreVatNumberOrchestrationService.NoAgentClientRelationship
      case Left(VatMigrationInProgress) =>
        MigrationInProgress
      case Left(StoreVatNumberService.InvalidVatNumber) =>
        StoreVatNumberOrchestrationService.InvalidVatNumber
      case Left(StoreVatNumberService.IneligibleVatNumber(dates)) if dates.isEmpty =>
        Ineligible
      case Left(StoreVatNumberService.AlreadySubscribed) =>
        StoreVatNumberOrchestrationService.AlreadySubscribed
      case Left(StoreVatNumberService.IneligibleVatNumber(dates)) =>
        Inhibited(dates)
      case _ =>
        throw new InternalServerException("Failed to store non migrated vat number for a delegated user")
    }
  }

  private def storeVatNumber(vatNumber: String)(implicit hc: HeaderCarrier): Future[StoreVatNumberOrchestrationServiceResponse] = {
    storeVatNumberService.storeVatNumber(vatNumber, isFromBta = false).map {
      case Right(StoreVatNumberService.VatNumberStored(isOverseas, isDirectDebit)) =>
        StoreVatNumberOrchestrationService.VatNumberStored(isOverseas, isDirectDebit, isMigrated = false)
      case Right(StoreVatNumberService.SubscriptionClaimed) =>
        StoreVatNumberOrchestrationService.SubscriptionClaimed
      case Left(IneligibleVatNumber(migratableDates)) if migratableDates.isEmpty =>
        StoreVatNumberOrchestrationService.Ineligible
      case Left(IneligibleVatNumber(migratableDates)) =>
        StoreVatNumberOrchestrationService.Inhibited(migratableDates)
      case Left(VatMigrationInProgress) =>
        StoreVatNumberOrchestrationService.MigrationInProgress
      case Left(VatNumberAlreadyEnrolled) =>
        StoreVatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
      case _ =>
        throw new InternalServerException("Failed to store non migrated vat number")
    }
  }

  private def claimSubscription(vatNumber: String)(implicit hc: HeaderCarrier): Future[StoreVatNumberOrchestrationServiceResponse] =
    claimSubscriptionService.claimSubscription(vatNumber, isFromBta = false).map {
      case Right(ClaimSubscriptionHttpParser.SubscriptionClaimed) =>
        StoreVatNumberOrchestrationService.SubscriptionClaimed
      case Left(ClaimSubscriptionHttpParser.AlreadyEnrolledOnDifferentCredential) =>
        StoreVatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
      case Left(unexpectedError) =>
        throw new InternalServerException(s"Unexpected error in claim subscription for user with enrolment - $unexpectedError")
    }
}

object StoreVatNumberOrchestrationService {

  sealed trait StoreVatNumberOrchestrationServiceResponse

  case class VatNumberStored(isOverseas: Boolean, isDirectDebit: Boolean, isMigrated: Boolean) extends StoreVatNumberOrchestrationServiceResponse

  case object Ineligible extends StoreVatNumberOrchestrationServiceResponse

  case object SubscriptionClaimed extends StoreVatNumberOrchestrationServiceResponse

  case object MigrationInProgress extends StoreVatNumberOrchestrationServiceResponse

  case object KnownFactsMismatch extends StoreVatNumberOrchestrationServiceResponse

  case object NoAgentClientRelationship extends StoreVatNumberOrchestrationServiceResponse

  case object AlreadyEnrolledOnDifferentCredential extends StoreVatNumberOrchestrationServiceResponse

  case object InvalidVatNumber extends StoreVatNumberOrchestrationServiceResponse

  case class Inhibited(migratableDates: MigratableDates) extends StoreVatNumberOrchestrationServiceResponse

  case class Eligible(isOverseas: Boolean, isMigrated: Boolean) extends StoreVatNumberOrchestrationServiceResponse

  case object AlreadySubscribed extends StoreVatNumberOrchestrationServiceResponse

}

