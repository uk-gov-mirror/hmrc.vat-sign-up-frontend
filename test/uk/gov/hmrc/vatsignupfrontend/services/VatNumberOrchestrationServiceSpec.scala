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

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status._
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, ReSignUpJourney}
import uk.gov.hmrc.vatsignupfrontend.connectors.mocks.MockStoreMigratedVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberSuccess
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser.{AlreadySubscribed, Eligible, Ineligible, Inhibited, MigrationInProgress, VatNumberEligibilityFailure}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityPreMigrationHttpParser.{IneligibleForMtdVatNumber, VatNumberEligible}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.{ClaimSubscriptionHttpParser, VatNumberEligibilityHttpParser, VatNumberEligibilityPreMigrationHttpParser}
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockClaimSubscriptionService, MockStoreVatNumberService, MockVatNumberEligibilityPreMigrationService, MockVatNumberEligibilityService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatNumberOrchestrationServiceSpec extends UnitSpec
  with MockVatNumberEligibilityService
  with MockVatNumberEligibilityPreMigrationService
  with MockStoreMigratedVatNumberConnector
  with MockStoreVatNumberService
  with MockClaimSubscriptionService
  with BeforeAndAfterEach
  with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(ReSignUpJourney)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestService extends VatNumberOrchestrationService(
    mockStoreMigratedVatNumberConnector,
    mockVatNumberEligibilityService,
    mockVatNumberEligibilityPreMigrationService,
    mockStoreVatNumberService,
    mockClaimSubscriptionService
  )

  val testMigratableDates = MigratableDates(Some(testStartDate), Some(testEndDate))
  val testVatEnrolments: Enrolments = Enrolments(Set(testMtdVatEnrolment, testVatDecEnrolment))
  val testNoEnrolments: Enrolments = Enrolments(Set())

  "checkVatNumberEligibility" when {
    "the feature switch is enabled" when {
      "the vat number is eligible" should {
        "return Eligible" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Right(Eligible(isOverseas = true, isMigrated = false))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Eligible(isOverseas = true, isMigrated = false)
        }
      }

      "the vat number is ineligible" should {
        "return Ineligible" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Right(Ineligible)
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Ineligible
        }
      }

      "the vat number is inhibited" should {
        "return Inhibited" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Right(Inhibited(testMigratableDates))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Inhibited(testMigratableDates)
        }
      }

      "the vat number is being migrated" should {
        "return MigrationInProgress" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Right(MigrationInProgress)
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.MigrationInProgress
        }
      }

      "the vat number is already subscribed" should {
        "return AlreadySubscribed" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Right(AlreadySubscribed)
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.AlreadySubscribed
        }
      }

      "check vat number eligibility fails" should {
        "throw and internal server exception" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Left(VatNumberEligibilityFailure(BAD_REQUEST))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          intercept[InternalServerException](await(res))
        }
      }
    }

    "the feature switch is disabled" when {
      "the vat number is eligible and overseas" should {
        "return Eligible with overseas true" in {
          mockVatNumberEligibilityPreMigration(testVatNumber)(Future.successful(
            Right(VatNumberEligible(isOverseas = true))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Eligible(isOverseas = true, isMigrated = false)
        }
      }

      "the vat number is eligible and not overseas" should {
        "return Eligible with overseas false" in {
          mockVatNumberEligibilityPreMigration(testVatNumber)(Future.successful(
            Right(VatNumberEligible())
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Eligible(isOverseas = false, isMigrated = false)
        }
      }

      "the vat number is ineligible" should {
        "return Ineligible" in {
          mockVatNumberEligibilityPreMigration(testVatNumber)(Future.successful(
            Left(IneligibleForMtdVatNumber(MigratableDates(None, None)))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Ineligible
        }
      }

      "the vat number is ineligible due to inhibition" should {
        "return inhibited with dates" in {
          mockVatNumberEligibilityPreMigration(testVatNumber)(Future.successful(
            Left(IneligibleForMtdVatNumber(testMigratableDates))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.Inhibited(migratableDates = testMigratableDates)
        }
      }

      "the vat number is invalid" should {
        "return invalid" in {
          mockVatNumberEligibilityPreMigration(testVatNumber)(Future.successful(
            Left(VatNumberEligibilityPreMigrationHttpParser.InvalidVatNumber)
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          await(res) shouldBe VatNumberOrchestrationService.InvalidVatNumber
        }
      }

      "the eligibility check fails" should {
        "throw internal server exception with a status" in {
          mockVatNumberEligibilityPreMigration(testVatNumber)(Future.successful(
            Left(VatNumberEligibilityPreMigrationHttpParser.VatNumberEligibilityFailureResponse(BAD_REQUEST))
          ))

          val res = TestService.checkVatNumberEligibility(testVatNumber)

          intercept[InternalServerException](await(res))
        }
      }
    }
  }

  "storeVatNumber" when {
    "the feature switch is enabled" when {
      "an eligible vat number has already been migrated to ETMP" should {
        "return MigratedVatNumberStored" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(Eligible(isOverseas = false, isMigrated = true))))
          mockStoreMigratedVatNumber(testVatNumber)(Future.successful(Right(StoreMigratedVatNumberSuccess)))

          val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

          await(res) shouldBe VatNumberOrchestrationService.MigratedVatNumberStored
        }
      }

      "the vat number has not been migrated to ETMP yet" should {
        "return NonMigratedVatNumberStored" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(VatNumberEligibilityHttpParser.Eligible(isOverseas = false, isMigrated = false))))
          mockStoreVatNumber(testVatNumber, isFromBta = false)(Future.successful(Right(VatNumberStored(isDirectDebit = false))))

          val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

          await(res) shouldBe VatNumberOrchestrationService.NonMigratedVatNumberStored(isOverseas = false, isDirectDebit = false)
        }
      }

      "the vat number is ineligible" should {
        "return Ineligible" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(Ineligible)))

          val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

          await(res) shouldBe VatNumberOrchestrationService.Ineligible
        }
      }

      "the vat number is inhibited" should {
        "return Inhibited" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(Inhibited(testMigratableDates))))

          val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

          await(res) shouldBe VatNumberOrchestrationService.Inhibited(testMigratableDates)
        }
      }

      "the vat number is currently being migrated to ETMP" should {
        "return MigrationInProgress" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(VatNumberEligibilityHttpParser.MigrationInProgress)))

          val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

          await(res) shouldBe VatNumberOrchestrationService.MigrationInProgress
        }
      }

      "the vat number is already subscribed" when {
        "subscription is claimed successfully" should {
          "return ClaimedSubscription" in {
            enable(ReSignUpJourney)
            mockVatNumberEligibility(testVatNumber)(Future.successful(
              Right(AlreadySubscribed)
            ))
            mockClaimSubscription(testVatNumber, isFromBta = false)(Right(ClaimSubscriptionHttpParser.SubscriptionClaimed))

            val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

            await(res) shouldBe VatNumberOrchestrationService.ClaimedSubscription
          }
        }

        "the vat number is already enrolled on a different cred" should {
          "return AlreadyEnrolledOnDifferentCredential" in {
            enable(ReSignUpJourney)
            mockVatNumberEligibility(testVatNumber)(Future.successful(
              Right(AlreadySubscribed)
            ))
            mockClaimSubscription(testVatNumber, isFromBta = false)(Left(ClaimSubscriptionHttpParser.AlreadyEnrolledOnDifferentCredential))

            val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

            await(res) shouldBe VatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
          }
        }

        "subscription claim fails" should {
          "throw and internal server exception" in {
            enable(ReSignUpJourney)
            mockVatNumberEligibility(testVatNumber)(Future.successful(
              Right(AlreadySubscribed)
            ))
            mockClaimSubscription(testVatNumber, isFromBta = false)(Left(ClaimSubscriptionHttpParser.ClaimSubscriptionFailureResponse(BAD_REQUEST)))

            val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

            intercept[InternalServerException](await(res))
          }
        }
      }

      "the vat number eligibility check fails" should {
        "throw internal server exception" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(
            Left(VatNumberEligibilityHttpParser.VatNumberEligibilityFailure(BAD_REQUEST))
          ))

          val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

          intercept[InternalServerException](await(res))
        }
      }
    }

    "the feature switch is disabled" when {
      "return NonMigratedVatNumberStored with isOverseas and isDirectDebit" in {
        mockStoreVatNumber(testVatNumber, isFromBta = false)(Right(VatNumberStored(isOverseas = true, isDirectDebit = true)))

        val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

        await(res) shouldBe VatNumberOrchestrationService.NonMigratedVatNumberStored(isOverseas = true, isDirectDebit = true)
      }
    }

    "the vat number has a claimed subscription" should {
      "return ClaimedSubscription" in {
        mockStoreVatNumber(testVatNumber, isFromBta = false)(Right(SubscriptionClaimed))

        val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

        await(res) shouldBe VatNumberOrchestrationService.ClaimedSubscription
      }
    }

    "the vat number is ineligible" should {
      "return Ineligible" in {
        mockStoreVatNumber(testVatNumber, isFromBta = false)(Left(IneligibleVatNumber(MigratableDates(None, None))))

        val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

        await(res) shouldBe VatNumberOrchestrationService.Ineligible
      }
    }

    "the vat number is ineligible due to inhibition" should {
      "return Inhibited with dates" in {
        mockStoreVatNumber(testVatNumber, isFromBta = false)(Left(IneligibleVatNumber(testMigratableDates)))

        val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

        await(res) shouldBe VatNumberOrchestrationService.Inhibited(migratableDates = testMigratableDates)
      }
    }

    "the vat number has a migration in progress" should {
      "return MigrationInProgress" in {
        mockStoreVatNumber(testVatNumber, isFromBta = false)(Left(VatMigrationInProgress))

        val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

        await(res) shouldBe VatNumberOrchestrationService.MigrationInProgress
      }
    }

    "the vat number is already enrolled" should {
      "return AlreadyEnrolled" in {
        mockStoreVatNumber(testVatNumber, isFromBta = false)(Left(VatNumberAlreadyEnrolled))

        val res = TestService.storeVatNumber(testVatNumber, isFromBta = false)

        await(res) shouldBe VatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
      }
    }
  }
}
