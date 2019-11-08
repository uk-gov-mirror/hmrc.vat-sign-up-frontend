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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, ReSignUpJourney}
import uk.gov.hmrc.vatsignupfrontend.connectors.mocks.{MockVatNumberEligibilityConnector, MockVatNumberEligibilityPreMigrationConnector}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.{testEndDate, testStartDate, testVatNumber}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser.{AlreadySubscribed, Eligible, Ineligible, Inhibited, MigrationInProgress}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityPreMigrationHttpParser.{IneligibleForMtdVatNumber, InvalidVatNumber, VatNumberEligible}
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckVatNumberEligibilityServiceSpec extends UnitSpec
  with MockVatNumberEligibilityPreMigrationConnector
  with MockVatNumberEligibilityConnector
  with FeatureSwitching
  with BeforeAndAfterEach{

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(ReSignUpJourney)
  }

  object TestService extends CheckVatNumberEligibilityService(
    mockVatNumberEligibilityPreMigrationConnector,
    mockVatNumberEligibilityConnector
  )

  implicit val hc = HeaderCarrier()

  val testMigratableDates = MigratableDates(Some(testStartDate), Some(testEndDate))

  "checkVatNumberEligibility" when {
    "the ReSignUpJourney feature switch is enabled" when {
      "the connector returns Eligible" should {
        "return Eligible and isMigrated" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(Eligible(isOverseas = false, isMigrated = true))))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.Eligible(isOverseas = false, isMigrated = true)
        }
      }

      "the connector returns MigrationInProgress" should {
        "return MigrationInProgress" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(MigrationInProgress)))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.MigrationInProgress
        }
      }

      "the connector returns AlreadySubscribed" should {
        "return AlreadySubscribed" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(AlreadySubscribed)))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
        }
      }

      "the connector returns Ineligible" should {
        "return Ineligible" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(Ineligible)))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.Ineligible
        }
      }

      "the connector returns Inhibited with dates" should {
        "return Inhibited" in {
          enable(ReSignUpJourney)
          mockVatNumberEligibility(testVatNumber)(Future.successful(Right(Inhibited(testMigratableDates))))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)
        }
      }
    }

    "the ReSignUpJourney feature switch is disabled" when {
      "the pre migration connector returns VatNumberEligible" should {
        "return Eligible" in {
          mockVatNumberPreMigrationEligibility(testVatNumber)(Future.successful(Right(VatNumberEligible(isOverseas = false))))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.Eligible(isOverseas = false, isMigrated = false)
        }
      }

      "the pre migration connector returns IneligibleForMtdVatNumber with dates" should {
        "return Inhibited with dates" in {
          mockVatNumberPreMigrationEligibility(testVatNumber)(Future.successful(Left(IneligibleForMtdVatNumber(testMigratableDates))))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)
        }
      }

      "the pre migration connector returns IneligibleForMtdVatNumber without dates" should {
        "return Ineligible" in {
          mockVatNumberPreMigrationEligibility(testVatNumber)(Future.successful(Left(IneligibleForMtdVatNumber(MigratableDates()))))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.Ineligible
        }
      }

      "the pre migration connector returns InvalidVatNumber with dates" should {
        "return InvalidVatNumber" in {
          mockVatNumberPreMigrationEligibility(testVatNumber)(Future.successful(Left(InvalidVatNumber)))

          val result = await(TestService.checkEligibility(testVatNumber))
          result shouldBe StoreVatNumberOrchestrationService.InvalidVatNumber
        }
      }
    }
  }


}
