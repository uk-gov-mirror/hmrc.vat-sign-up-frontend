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

package uk.gov.hmrc.vatsignupfrontend.services

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.connectors.GetCompanyNameConnector
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.GetCompanyNameHttpParser.CompanyDetails
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.NonPartnershipEntity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GetCompanyNameServiceSpec extends UnitSpec with MockitoSugar {

  val mockConnector = mock[GetCompanyNameConnector]

  object TestStoreVatNumberService extends GetCompanyNameService(mockConnector)

  implicit val hc = HeaderCarrier()

  val testShortCompanyNumber = "1234567"
  val testShortPaddedCompanyNumber = "01234567"
  val testPrefixedCompanyNumber = "SC12"
  val testPrefixedPaddedCompanyNumber = "SC000012"
  val testSuffixedCompanyNumber = "1234567R"
  val testPrefixSuffixCompanyNumber = "IP1234RS"
  val testInvalidCompanyNumber = "123456 7"
  val testInvalidCompanyNumber2 = "1234-567"

  val result = Right(CompanyDetails(testCompanyName, NonPartnershipEntity))

  "getCompanyName" when {
    "the company number is 8 characters long" should {
      "return the result of the connector" in {
        when(mockConnector.getCompanyName(ArgumentMatchers.eq(testCompanyNumber))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(result))

        val r = TestStoreVatNumberService.getCompanyName(testCompanyNumber)

        // null pointer exception would have been thrown if the arguments weren't converted to the expected string format
        await(r) shouldBe result
      }
    }
    "the company number is shorter than 8 characters" when {
      "there CRN has no prefix" should {
        "return the result of the connector" in {
          when(mockConnector.getCompanyName(ArgumentMatchers.eq(testShortPaddedCompanyNumber))(ArgumentMatchers.any()))
            .thenReturn(Future.successful(result))

          val r = TestStoreVatNumberService.getCompanyName(testShortCompanyNumber)

          // null pointer exception would have been thrown if the arguments weren't converted to the expected string format
          await(r) shouldBe result
        }
      }
      "the CRN has a prefix" should {
        "return the result of the connector" in {
          when(mockConnector.getCompanyName(ArgumentMatchers.eq(testPrefixedPaddedCompanyNumber))(ArgumentMatchers.any()))
            .thenReturn(Future.successful(result))

          val r = TestStoreVatNumberService.getCompanyName(testPrefixedCompanyNumber)

          // null pointer exception would have been thrown if the arguments weren't converted to the expected string format
          await(r) shouldBe result
        }
      }
    }
    "the company number has a suffix" should {
      "return the result of the connector" in {
        when(mockConnector.getCompanyName(ArgumentMatchers.eq(testSuffixedCompanyNumber))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(result))

        val r = TestStoreVatNumberService.getCompanyName(testSuffixedCompanyNumber)

        // null pointer exception would have been thrown if the arguments weren't converted to the expected string format
        await(r) shouldBe result
      }
    }
    "the company number has a prefix and a suffix" should {
      "return the result of the connector" in {
        when(mockConnector.getCompanyName(ArgumentMatchers.eq(testPrefixSuffixCompanyNumber))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(result))

        val r = TestStoreVatNumberService.getCompanyName(testPrefixSuffixCompanyNumber)

        // null pointer exception would have been thrown if the arguments weren't converted to the expected string format
        await(r) shouldBe result
      }
    }
    "the company number is invalid" should {
      "throw an illegal argument exception if any spaces were not stripped by the frontend" in {
        when(mockConnector.getCompanyName(ArgumentMatchers.eq(testInvalidCompanyNumber))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(result))

        intercept[IllegalArgumentException] {
          await(TestStoreVatNumberService.getCompanyName(testInvalidCompanyNumber))
        }
      }
      "throw an illegal argument exception if the company number contains any illegal characters" in {
        when(mockConnector.getCompanyName(ArgumentMatchers.eq(testInvalidCompanyNumber2))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(result))

        intercept[IllegalArgumentException] {
          await(TestStoreVatNumberService.getCompanyName(testInvalidCompanyNumber2))
        }
      }
    }
  }

}
