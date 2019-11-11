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
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.ClaimSubscriptionHttpParser
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService._
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService.{AlreadySubscribed, InvalidVatNumber, NoAgentClientRelationship, _}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockCheckVatNumberEligibilityService, MockClaimSubscriptionService, MockStoreMigratedVatNumberService, MockStoreVatNumberService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatNumberOrchestrationServiceSpec extends UnitSpec
  with MockCheckVatNumberEligibilityService
  with MockStoreMigratedVatNumberService
  with MockStoreVatNumberService
  with MockClaimSubscriptionService
  with BeforeAndAfterEach
  with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(ReSignUpJourney)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestService extends StoreVatNumberOrchestrationService(
    mockCheckVatNumberEligibilityService,
    mockStoreMigratedVatNumberService,
    mockStoreVatNumberService,
    mockClaimSubscriptionService
  )

  val testMigratableDates = MigratableDates(Some(testStartDate), Some(testEndDate))
  val testVatEnrolments: Enrolments = Enrolments(Set(testMtdVatEnrolment, testVatDecEnrolment))
  val testNoEnrolments: Enrolments = Enrolments(Set())

  "orchestrate" when {
    "enrolments are not provided" when {
      "the feature switch is enabled" when {
        "the vat number is eligible" should {
          "return Eligible" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Eligible(isOverseas = true, isMigrated = false)
          }
        }

        "the vat number is ineligible" should {
          "return Ineligible" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Ineligible))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Ineligible
          }
        }

        "the vat number is inhibited" should {
          "return Inhibited" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Inhibited(testMigratableDates)))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)
          }
        }

        "the vat number is being migrated" should {
          "return MigrationInProgress" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(MigrationInProgress))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.MigrationInProgress
          }
        }

        "the vat number is already subscribed" should {
          "return AlreadySubscribed" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
          }
        }

        "check vat number eligibility fails" should {
          "throw an internal server exception" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.failed(new InternalServerException("")))


            intercept[InternalServerException](await(TestService.orchestrate(testNoEnrolments, testVatNumber)))
          }
        }
      }
      "the feature switch is disabled" when {
        "the vat number is eligible and overseas" should {
          "return Eligible with overseas true" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Eligible(isOverseas = true, isMigrated = false)
          }
        }

        "the vat number is eligible and not overseas" should {
          "return Eligible with overseas false" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Eligible(isOverseas = false, isMigrated = false)
          }
        }

        "the vat number is ineligible" should {
          "return Ineligible" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Ineligible))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Ineligible
          }
        }

        "the vat number is ineligible due to inhibition" should {
          "return inhibited with dates" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Inhibited(testMigratableDates)))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Inhibited(migratableDates = testMigratableDates)
          }
        }

        "the vat number is invalid" should {
          "return invalid" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.InvalidVatNumber))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.InvalidVatNumber
          }
        }

        "the eligibility check fails" should {
          "throw internal server exception with a status" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.failed(new InternalServerException("")))

            val res = TestService.orchestrate(testNoEnrolments, testVatNumber)

            intercept[InternalServerException](await(res))
          }
        }
      }
    }

    "enrolments are provided" when {
      "the feature switch is enabled" when {
        "an eligible vat number has already been migrated to ETMP" should {
          "return VatNumberStored" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = true)))
            mockStoreMigratedVatNumber(testVatNumber, None, None)(
              Future.successful(StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true))
            )

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true)
          }
        }

        "the vat number has not been migrated to ETMP yet" should {
          "return VatNumberStored" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
            mockStoreVatNumber(testVatNumber, isFromBta = false)(Future.successful(Right(StoreVatNumberService.VatNumberStored(isDirectDebit = false))))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = false)
          }
        }

        "the vat number is ineligible" should {
          "return Ineligible" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Ineligible))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Ineligible
          }
        }

        "the vat number is inhibited" should {
          "return Inhibited" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Inhibited(testMigratableDates)))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)
          }
        }

        "the vat number is currently being migrated to ETMP" should {
          "return MigrationInProgress" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(MigrationInProgress))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.MigrationInProgress
          }
        }

        "the vat number is already subscribed" when {
          "subscription is claimed successfully" should {
            "return SubscriptionClaimed" in {
              enable(ReSignUpJourney)
              mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))
              mockClaimSubscription(testVatNumber, isFromBta = false)(Right(ClaimSubscriptionHttpParser.SubscriptionClaimed))

              val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

              await(res) shouldBe StoreVatNumberOrchestrationService.SubscriptionClaimed
            }
          }

          "the vat number is already enrolled on a different cred" should {
            "return AlreadyEnrolledOnDifferentCredential" in {
              enable(ReSignUpJourney)
              mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))
              mockClaimSubscription(testVatNumber, isFromBta = false)(Left(ClaimSubscriptionHttpParser.AlreadyEnrolledOnDifferentCredential))

              val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

              await(res) shouldBe StoreVatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
            }
          }

          "subscription claim fails" should {
            "throw and internal server exception" in {
              enable(ReSignUpJourney)
              mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))
              mockClaimSubscription(testVatNumber, isFromBta = false)(Left(ClaimSubscriptionHttpParser.ClaimSubscriptionFailureResponse(BAD_REQUEST)))

              val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

              intercept[InternalServerException](await(res))
            }
          }
        }

        "the vat number eligibility check fails" should {
          "throw internal server exception" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.failed(new InternalServerException("")))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            intercept[InternalServerException](await(res))
          }
        }

        "the user has MTD VAT enrolment and the vat number is already signed up" should {
          "return AlreadySubscribed" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))

            val res = TestService.orchestrate(Enrolments(Set(testMtdVatEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
          }
        }

        "the user has multiple enrolments and the vat number is already signed up" should {
          "return AlreadySubscribed" in {
            enable(ReSignUpJourney)
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment, testMtdVatEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
          }
        }
      }

      "the feature switch is disabled" when {
        "the vat number can be stored and is from overseas with direct debit" should {
          "return VatNumberStored with isOverseas and isDirectDebit" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))
            mockStoreVatNumber(testVatNumber, isFromBta = false)(Right(StoreVatNumberService.VatNumberStored(isOverseas = true, isDirectDebit = true)))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = true, isDirectDebit = true, isMigrated = false)
          }
        }


        "the vat number has a claimed subscription" should {
          "return SubscriptionClaimed" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))
            mockClaimSubscription(testVatNumber, isFromBta = false)(Right(ClaimSubscriptionHttpParser.SubscriptionClaimed))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.SubscriptionClaimed
          }
        }

        "the vat number is ineligible" should {
          "return Ineligible" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.Ineligible))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Ineligible
          }
        }

        "the vat number is ineligible due to inhibition" should {
          "return Inhibited with dates" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Inhibited(migratableDates = testMigratableDates)
          }
        }

        "the vat number has a migration in progress" should {
          "return MigrationInProgress" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))
            mockStoreVatNumber(testVatNumber, isFromBta = false)(Left(VatMigrationInProgress))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.MigrationInProgress
          }
        }

        "the vat number is already enrolled" should {
          "return AlreadyEnrolled" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))
            mockStoreVatNumber(testVatNumber, isFromBta = false)(Left(VatNumberAlreadyEnrolled))

            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadyEnrolledOnDifferentCredential
          }
        }

        "the user has multiple enrolments and the vat number is already signed up" should {
          "return AlreadySubscribed" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))
            val res = TestService.orchestrate(Enrolments(Set(testVatDecEnrolment, testMtdVatEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
          }
        }

        "the user has MTD-VAT enrolment and the vat number is already signed up" should {
          "return AlreadySubscribed" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))
            val res = TestService.orchestrate(Enrolments(Set(testMtdVatEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
          }
        }
      }
    }
  }
  "the user has an agent enrolment" should {
    "the ReSignUpJourney is enabled" when {
      "an eligible vat number has already been migrated to ETMP" should {
        "return VatNumberStored" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = true)))
          mockStoreMigratedVatNumber(testVatNumber, None, None)(
            Future.successful(StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true))
          )

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true)
        }
      }

      "the vat number has not been migrated to ETMP yet" should {
        "return VatNumberStored" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
          mockStoreVatNumberDelegated(testVatNumber)(Future.successful(Right(StoreVatNumberService.VatNumberStored(isDirectDebit = false))))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = false)
        }
      }

      "the vat number is ineligible" should {
        "return Ineligible" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Ineligible))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.Ineligible
        }
      }

      "the vat number is inhibited" should {
        "return Inhibited" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Inhibited(testMigratableDates)))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)
        }
      }

      "the vat number is currently being migrated to ETMP" should {
        "return MigrationInProgress" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(MigrationInProgress))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.MigrationInProgress
        }
      }

      "the vat number eligibility check fails" should {
        "throw internal server exception" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.failed(new InternalServerException("")))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          intercept[InternalServerException](await(res))
        }
      }

      "the vat number is already signed up" should {
        "return AlreadySubscribed" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.AlreadySubscribed))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
        }
      }

      "the vat number has No Agent Client Relationship" should {
        "return NoAgentClientRelationship" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.NoAgentClientRelationship))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.NoAgentClientRelationship
        }
      }

      "the vat number is Invalid" should {
        "return InvalidVatNumber" in {
          enable(ReSignUpJourney)
          mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.InvalidVatNumber))

          val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

          await(res) shouldBe StoreVatNumberOrchestrationService.InvalidVatNumber
        }
      }

      "the feature switch is disabled" when {
        "the vat number is overseas with direct debit" should {
          "return VatNumberStored with isOverseas and isDirectDebit" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = true, isMigrated = false)))
            mockStoreVatNumberDelegated(testVatNumber)(Right(StoreVatNumberService.VatNumberStored(isOverseas = true, isDirectDebit = true)))

            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = true, isDirectDebit = true, isMigrated = false)
          }
        }

        "the vat number is not overseas" should {
          "return VatNumberStored" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
            mockStoreVatNumberDelegated(testVatNumber)(Right(StoreVatNumberService.VatNumberStored(isOverseas = false, isDirectDebit = false)))

            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = false)
          }
        }

        "the vat number is ineligible" should {
          "return Ineligible" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.Ineligible))

            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Ineligible
          }
        }

        "the vat number is ineligible due to inhibition" should {
          "return Inhibited with dates" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(StoreVatNumberOrchestrationService.Inhibited(testMigratableDates)))

            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.Inhibited(migratableDates = testMigratableDates)
          }
        }

        "the vat number has no agent client relationship" should {
          "return no agent client relationship" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
            mockStoreVatNumberDelegated(testVatNumber)(Left(NoAgentClientRelationship))

            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.NoAgentClientRelationship
          }
        }

        "the vat number is already signed up" should {
          "return AlreadySubscribed" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
            mockStoreVatNumberDelegated(testVatNumber)(Left(AlreadySubscribed))
            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.AlreadySubscribed
          }
        }

        "the vat number is invalid" should {
          "return InvalidvatNumber" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
            mockStoreVatNumberDelegated(testVatNumber)(Left(InvalidVatNumber))
            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.InvalidVatNumber
          }
        }


        "the vat number is currently migrating" should {
          "return MigrationInProgress" in {
            mockCheckVatNumberEligibility(testVatNumber)(Future.successful(Eligible(isOverseas = false, isMigrated = false)))
            mockStoreVatNumberDelegated(testVatNumber)(Left(VatMigrationInProgress))
            val res = TestService.orchestrate(Enrolments(Set(testAgentEnrolment)), testVatNumber)

            await(res) shouldBe StoreVatNumberOrchestrationService.MigrationInProgress
          }
        }

      }
    }
  }
}
