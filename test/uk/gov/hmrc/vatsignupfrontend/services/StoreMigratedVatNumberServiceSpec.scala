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

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.connectors.mocks.MockStoreMigratedVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser.{KnownFactsMismatch, NoAgentClientRelationship, StoreMigratedVatNumberFailureStatus, StoreMigratedVatNumberSuccess}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StoreMigratedVatNumberServiceSpec extends UnitSpec
  with MockStoreMigratedVatNumberConnector {

  object TestService extends StoreMigratedVatNumberService(mockStoreMigratedVatNumberConnector)

  implicit val hc = HeaderCarrier()

  private val testRegistrationDate = "01-Dec-2017" // Todo: This may be in the wrong format... Check.

  "storeVatNumber" when {
    "known facts are provided" when {
      "the connector returns StoreMigratedVatNumberSuccess" should {
        "return StoreMigratedVatNumberSuccess" in {
          mockStoreMigratedVatNumber(testVatNumber, testRegistrationDate, Some(testBusinessPostcode))(
            Future.successful(Right(StoreMigratedVatNumberSuccess))
          )

          val res = await(TestService.storeVatNumber(
            vatNumber = testVatNumber,
            optRegistrationDate = Some(testRegistrationDate),
            optBusinessPostCode = Some(testBusinessPostcode))
          )

          res shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true)
        }
      }
      "the connector returns KnownFactsMismatch" should {
        "return KnownFactsMismatch" in {
          mockStoreMigratedVatNumber(testVatNumber, testRegistrationDate, Some(testBusinessPostcode))(
            Future.successful(Left(KnownFactsMismatch))
          )

          val res = await(TestService.storeVatNumber(
            vatNumber = testVatNumber,
            optRegistrationDate = Some(testRegistrationDate),
            optBusinessPostCode = Some(testBusinessPostcode))
          )

          res shouldBe StoreVatNumberOrchestrationService.KnownFactsMismatch
        }
      }
      "the connector returns NoAgentClientRelationship" should {
        "return NoAgentClientRelationship" in {
          mockStoreMigratedVatNumber(testVatNumber)(
            Future.successful(Left(NoAgentClientRelationship))
          )

          val res = await(TestService.storeVatNumber(
            vatNumber = testVatNumber,
            optRegistrationDate = None,
            optBusinessPostCode = None)
          )

          res shouldBe StoreVatNumberOrchestrationService.NoAgentClientRelationship
        }
      }
      "the connector returns StoreMigratedVatNumberFailureStatus" should {
        "return InternalServerException" in {
          mockStoreMigratedVatNumber(testVatNumber, testRegistrationDate, Some(testBusinessPostcode))(
            Future.successful(Left(StoreMigratedVatNumberFailureStatus(BAD_REQUEST)))
          )

          intercept[InternalServerException] {
            await(TestService.storeVatNumber(testVatNumber, Some(testRegistrationDate), Some(testBusinessPostcode)))
          }
        }
      }
    }
    "known facts are not provided" when {
      "the connector returns StoreMigratedVatNumberSuccess" should {
        "return StoreMigratedVatNumberSuccess" in {
          mockStoreMigratedVatNumber(testVatNumber)(Future.successful(Right(StoreMigratedVatNumberSuccess)))

          val res = await(TestService.storeVatNumber(testVatNumber, optRegistrationDate = None, optBusinessPostCode = None))
          res shouldBe StoreVatNumberOrchestrationService.VatNumberStored(isOverseas = false, isDirectDebit = false, isMigrated = true)
        }
      }
      "the connector returns NoAgentClientRelationship" should {
        "return NoAgentClientRelationship" in {
          mockStoreMigratedVatNumber(testVatNumber)(Future.successful(Left(NoAgentClientRelationship)))

          val res = await(TestService.storeVatNumber(testVatNumber, optRegistrationDate = None, optBusinessPostCode = None))
          res shouldBe StoreVatNumberOrchestrationService.NoAgentClientRelationship
        }
      }
      "the connector returns StoreMigratedVatNumberFailureStatus" should {
        "return StoreMigratedVatNumberFailureStatus" in {
          mockStoreMigratedVatNumber(testVatNumber)(Future.successful(Left(StoreMigratedVatNumberFailureStatus(BAD_REQUEST))))

          intercept[InternalServerException] {
            await(TestService.storeVatNumber(testVatNumber, optRegistrationDate = None, optBusinessPostCode = None))
          }
        }
      }
    }
  }

}
