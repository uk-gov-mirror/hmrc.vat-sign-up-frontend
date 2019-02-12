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
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.{ClaimSubscriptionHttpParser, StoreVatNumberHttpParser}
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, MigratableDates, PostCode}
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreVatNumberService @Inject()(storeVatNumberConnector: StoreVatNumberConnector,
                                      claimSubscriptionService: ClaimSubscriptionService
                                     )(implicit ec: ExecutionContext) {

  def storeVatNumberDelegated(vatNumber: String)(implicit hc: HeaderCarrier): Future[DelegatedStoreVatNumberResponse] =
    storeVatNumberConnector.storeVatNumber(vatNumber, isFromBta = false) map {
      case Right(StoreVatNumberHttpParser.VatNumberStored(isOverseas)) => Right(VatNumberStored(isOverseas))
      case Left(StoreVatNumberHttpParser.AlreadySubscribed) => Left(AlreadySubscribed)
      case Left(StoreVatNumberHttpParser.NoAgentClientRelationship) => Left(NoAgentClientRelationship)
      case Left(StoreVatNumberHttpParser.InvalidVatNumber) => Left(InvalidVatNumber)
      case Left(StoreVatNumberHttpParser.IneligibleVatNumber(migratableDates)) => Left(IneligibleVatNumber(migratableDates))
      case Left(StoreVatNumberHttpParser.VatMigrationInProgress) => Left(VatMigrationInProgress)
      case Left(unexpectedError) =>
        throw new InternalServerException(s"Unexpected error in store VAT number for delegated user - $unexpectedError")
    }

  def storeVatNumber(vatNumber: String, isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[StoreVatNumberWithEnrolmentResponse] =
    storeVatNumberConnector.storeVatNumber(vatNumber, isFromBta) flatMap {
      case Right(StoreVatNumberHttpParser.VatNumberStored(isOverseas)) =>
        Future.successful(Right(VatNumberStored(isOverseas)))
      case Left(StoreVatNumberHttpParser.AlreadySubscribed) =>
        claimSubscriptionService.claimSubscription(vatNumber, isFromBta) map {
          case Right(ClaimSubscriptionHttpParser.SubscriptionClaimed) =>
            Right(SubscriptionClaimed)
          case Left(ClaimSubscriptionHttpParser.AlreadyEnrolledOnDifferentCredential) =>
            Left(VatNumberAlreadyEnrolled)
          case Left(unexpectedError) =>
            throw new InternalServerException(s"Unexpected error in claim subscription for user with enrolment - $unexpectedError")
        }
      case Left(StoreVatNumberHttpParser.IneligibleVatNumber(migratableDates)) =>
        Future.successful(Left(IneligibleVatNumber(migratableDates)))
      case Left(StoreVatNumberHttpParser.VatMigrationInProgress) =>
        Future.successful(Left(VatMigrationInProgress))
      case Left(unexpectedError) =>
        throw new InternalServerException(s"Unexpected error in store VAT number for user with enrolment - $unexpectedError")
    }

  def storeVatNumber(vatNumber: String,
                     postCode: PostCode,
                     registrationDate: DateModel,
                     isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[StoreVatNumberWithKnownFactsResponse] =
    storeVatNumberConnector.storeVatNumber(vatNumber, postCode.postCode, registrationDate.toLocalDate.toString, isFromBta) flatMap {
      case Right(StoreVatNumberHttpParser.VatNumberStored(isOverseas)) =>
        Future.successful(Right(VatNumberStored(isOverseas)))
      case Right(StoreVatNumberHttpParser.VatNumberStored(_)) =>
        Future.successful(Right(VatNumberStored(isOverseas = false)))
      case Left(StoreVatNumberHttpParser.AlreadySubscribed) =>
        claimSubscriptionService.claimSubscription(vatNumber, postCode, registrationDate, isFromBta) map {
          case Right(ClaimSubscriptionHttpParser.SubscriptionClaimed) =>
            Right(SubscriptionClaimed)
          case Left(ClaimSubscriptionHttpParser.AlreadyEnrolledOnDifferentCredential) =>
            Left(VatNumberAlreadyEnrolled)
          case Left(unexpectedError) =>
            throw new InternalServerException(s"Unexpected error in claim subscription with supplied known facts - $unexpectedError")
        }
      case Left(StoreVatNumberHttpParser.KnownFactsMismatch) =>
        Future.successful(Left(KnownFactsMismatch))
      case Left(StoreVatNumberHttpParser.InvalidVatNumber) =>
        Future.successful(Left(InvalidVatNumber))
      case Left(StoreVatNumberHttpParser.IneligibleVatNumber(migratableDates)) =>
        Future.successful(Left(IneligibleVatNumber(migratableDates)))
      case Left(StoreVatNumberHttpParser.VatMigrationInProgress) =>
        Future.successful(Left(VatMigrationInProgress))
      case Left(unexpectedError) =>
        throw new InternalServerException(s"Unexpected error in store VAT number with supplied known facts - $unexpectedError")
    }

}

object StoreVatNumberService {
  type StoreVatNumberWithKnownFactsResponse = Either[StoreVatNumberWithKnownFactsFailure, StoreVatNumberSuccess]
  type StoreVatNumberWithEnrolmentResponse = Either[StoreVatNumberWithEnrolmentFailure, StoreVatNumberSuccess]
  type DelegatedStoreVatNumberResponse = Either[DelegatedStoreVatNumberFailure, StoreVatNumberSuccess]

  sealed trait StoreVatNumberSuccess

  case class VatNumberStored(isOverseas: Boolean = false) extends StoreVatNumberSuccess

  case object SubscriptionClaimed extends StoreVatNumberSuccess

  sealed trait StoreVatNumberWithKnownFactsFailure

  sealed trait DelegatedStoreVatNumberFailure

  sealed trait StoreVatNumberWithEnrolmentFailure

  case object NoAgentClientRelationship extends DelegatedStoreVatNumberFailure

  case object AlreadySubscribed extends DelegatedStoreVatNumberFailure

  case object KnownFactsMismatch extends StoreVatNumberWithKnownFactsFailure

  case object InvalidVatNumber extends StoreVatNumberWithKnownFactsFailure with DelegatedStoreVatNumberFailure

  case class IneligibleVatNumber(migratableDates: MigratableDates) extends StoreVatNumberWithKnownFactsFailure
    with DelegatedStoreVatNumberFailure with StoreVatNumberWithEnrolmentFailure

  case class StoreVatNumberFailureResponse(status: Int) extends StoreVatNumberWithKnownFactsFailure with DelegatedStoreVatNumberFailure

  case class ClaimSubscriptionFailureResponse(status: Int) extends StoreVatNumberWithKnownFactsFailure

  case object VatMigrationInProgress extends StoreVatNumberWithEnrolmentFailure with DelegatedStoreVatNumberFailure
    with StoreVatNumberWithKnownFactsFailure

  case object VatNumberAlreadyEnrolled extends StoreVatNumberWithEnrolmentFailure with DelegatedStoreVatNumberFailure
    with StoreVatNumberWithKnownFactsFailure

}
