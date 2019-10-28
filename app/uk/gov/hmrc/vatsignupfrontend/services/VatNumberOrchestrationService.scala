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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, ReSignUpJourney}
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreMigratedVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser.{StoreMigratedVatNumberFailure, StoreMigratedVatNumberSuccess}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser.VatNumberEligibilityFailure
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityPreMigrationHttpParser.{IneligibleForMtdVatNumber, VatNumberEligibilityFailureResponse}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.{ClaimSubscriptionHttpParser, VatNumberEligibilityHttpParser, VatNumberEligibilityPreMigrationHttpParser}
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._
import uk.gov.hmrc.vatsignupfrontend.services.VatNumberOrchestrationService._
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatNumberOrchestrationService @Inject()(storeMigratedVatNumberConnector: StoreMigratedVatNumberConnector,
                                              migratedVatNumberEligibilityService: VatNumberEligibilityService,
                                              vatNumberEligibilityService: VatNumberEligibilityPreMigrationService,
                                              storeVatNumberService: StoreVatNumberService,
                                              claimSubscriptionService: ClaimSubscriptionService
                                             )(implicit ec: ExecutionContext) extends FeatureSwitching {

  def orchestrate(enrolments: Enrolments,
                  optVatNumber: Option[String],
                  isFromBta: Boolean
                 )(implicit hc: HeaderCarrier): Future[VatNumberOrchestrationServiceSuccess] =

    if (isEnabled(ReSignUpJourney))
      enrolments.getAnyVatNumber match {
        case None =>
          checkMigratedVatNumberEligibility(optVatNumber.get)
        case Some(vatNumber) =>
          checkMigratedVatNumberEligibility(vatNumber).flatMap {
            case Eligible(_, isMigrated) if isMigrated =>
              storeMigratedVatNumber(vatNumber, isFromBta)
            case Eligible(_, _) =>
              storePreMigrationVatNumber(vatNumber, isFromBta)
            case VatNumberOrchestrationService.AlreadySubscribed =>
              enrolments.mtdVatNumber match {
                case Some(_) =>
                  Future.successful(VatNumberOrchestrationService.AlreadySubscribed)
                case None =>
                  claimSubscription(vatNumber, isFromBta)
              }
            case response =>
              Future.successful(response)
          }
      }
    else
      enrolments.vatNumber match {
        case None =>
          checkPreMigrationVatNumberEligibility(optVatNumber.get)
        case Some(vatNumber) =>
          enrolments.mtdVatNumber match {
            case Some(_) =>
              Future.successful(VatNumberOrchestrationService.AlreadySubscribed)
            case None =>
              storePreMigrationVatNumber(vatNumber, isFromBta)
          }
      }

  private def checkMigratedVatNumberEligibility(vatNumber: String)(implicit hc: HeaderCarrier): Future[VatNumberOrchestrationServiceSuccess] =
    migratedVatNumberEligibilityService.checkVatNumberEligibility(vatNumber).map {
      case Right(VatNumberEligibilityHttpParser.Eligible(isOverseas, isMigrated)) =>
        VatNumberOrchestrationService.Eligible(isOverseas, isMigrated)
      case Right(VatNumberEligibilityHttpParser.Ineligible) =>
        VatNumberOrchestrationService.Ineligible
      case Right(VatNumberEligibilityHttpParser.Inhibited(dates)) =>
        VatNumberOrchestrationService.Inhibited(dates)
      case Right(VatNumberEligibilityHttpParser.MigrationInProgress) =>
        VatNumberOrchestrationService.MigrationInProgress
      case Right(VatNumberEligibilityHttpParser.AlreadySubscribed) =>
        VatNumberOrchestrationService.AlreadySubscribed
      case Left(VatNumberEligibilityFailure(status)) =>
        throw new InternalServerException(s"Failed to get vat number eligibility with status: $status")
    }

  private def checkPreMigrationVatNumberEligibility(vatNumber: String)(implicit hc: HeaderCarrier): Future[VatNumberOrchestrationServiceSuccess] =
    vatNumberEligibilityService.checkVatNumberEligibility(vatNumber).map {
      case Right(success) =>
        VatNumberOrchestrationService.Eligible(isOverseas = success.isOverseas, isMigrated = false)
      case Left(IneligibleForMtdVatNumber(MigratableDates(None, None))) =>
        VatNumberOrchestrationService.Ineligible
      case Left(IneligibleForMtdVatNumber(migratableDates)) =>
        VatNumberOrchestrationService.Inhibited(migratableDates)
      case Left(VatNumberEligibilityPreMigrationHttpParser.InvalidVatNumber) =>
        VatNumberOrchestrationService.InvalidVatNumber
      case Left(VatNumberEligibilityFailureResponse(status)) =>
        throw new InternalServerException(s"Failure retrieving eligibility of vat number: status=$status")
    }

  private def storeMigratedVatNumber(vatNumber: String, isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[VatNumberOrchestrationServiceSuccess] =
    storeMigratedVatNumberConnector.storeVatNumber(vatNumber).map {
      case Right(StoreMigratedVatNumberSuccess) =>
        VatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true)
      case Left(StoreMigratedVatNumberFailure(status)) =>
        throw new InternalServerException(s"Failed to store migrated vat number with status: $status")
    }

  private def storePreMigrationVatNumber(vatNumber: String, isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[VatNumberOrchestrationServiceSuccess] =
    storeVatNumberService.storeVatNumber(vatNumber, isFromBta).map {
      case Right(StoreVatNumberService.VatNumberStored(isOverseas, isDirectDebit)) =>
        VatNumberOrchestrationService.VatNumberStored(isOverseas, isDirectDebit, isMigrated = false)
      case Right(SubscriptionClaimed) =>
        VatNumberOrchestrationService.ClaimedSubscription
      case Left(IneligibleVatNumber(MigratableDates(None, None))) =>
        VatNumberOrchestrationService.Ineligible
      case Left(IneligibleVatNumber(migratableDates)) =>
        VatNumberOrchestrationService.Inhibited(migratableDates)
      case Left(VatMigrationInProgress) =>
        VatNumberOrchestrationService.MigrationInProgress
      case Left(VatNumberAlreadyEnrolled) =>
        VatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
      case _ =>
        throw new InternalServerException("Failed to store non migrated vat number")
    }

  private def claimSubscription(vatNumber: String, isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[VatNumberOrchestrationServiceSuccess] =
    claimSubscriptionService.claimSubscription(vatNumber, isFromBta).map {
      case Right(ClaimSubscriptionHttpParser.SubscriptionClaimed) =>
        VatNumberOrchestrationService.ClaimedSubscription
      case Left(ClaimSubscriptionHttpParser.AlreadyEnrolledOnDifferentCredential) =>
        VatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
      case Left(unexpectedError) =>
        throw new InternalServerException(s"Unexpected error in claim subscription for user with enrolment - $unexpectedError")
    }
}

object VatNumberOrchestrationService {

  sealed trait VatNumberOrchestrationServiceSuccess

  case class VatNumberStored(isOverseas: Boolean, isDirectDebit: Boolean, isMigrated: Boolean) extends VatNumberOrchestrationServiceSuccess

  case object Ineligible extends VatNumberOrchestrationServiceSuccess

  case object ClaimedSubscription extends VatNumberOrchestrationServiceSuccess

  case object MigrationInProgress extends VatNumberOrchestrationServiceSuccess

  case object AlreadyEnrolledOnDifferentCredential extends VatNumberOrchestrationServiceSuccess

  case object InvalidVatNumber extends VatNumberOrchestrationServiceSuccess

  case class Inhibited(migratableDates: MigratableDates) extends VatNumberOrchestrationServiceSuccess

  case class Eligible(isOverseas: Boolean, isMigrated: Boolean) extends VatNumberOrchestrationServiceSuccess

  case object AlreadySubscribed extends VatNumberOrchestrationServiceSuccess

}
