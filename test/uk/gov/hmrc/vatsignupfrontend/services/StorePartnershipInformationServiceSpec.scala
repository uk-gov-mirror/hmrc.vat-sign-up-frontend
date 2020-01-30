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
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.connectors.StorePartnershipInformationConnector
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser.StorePartnershipInformationSuccess
import uk.gov.hmrc.vatsignupfrontend.models.{PartnershipEntityType, PostCode}
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.{GeneralPartnership, LimitedPartnership}

import scala.concurrent.Future


class StorePartnershipInformationServiceSpec extends UnitSpec with MockitoSugar {

  lazy val connector: StorePartnershipInformationConnector = mock[StorePartnershipInformationConnector]

  val successResult = Right(StorePartnershipInformationSuccess)

  implicit val hc = HeaderCarrier()

  def mockConnector(vatNumber: String,
                    sautr: Option[String],
                    partnershipType: PartnershipEntityType,
                    companyNumber: Option[String],
                    postCode:Option[PostCode]): Unit = {
    when(connector.storePartnershipInformation(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(sautr),
      ArgumentMatchers.eq(partnershipType),
      ArgumentMatchers.eq(companyNumber),
      ArgumentMatchers.eq(postCode)
    )(ArgumentMatchers.any()))
      .thenReturn(Future.successful(successResult))
  }

  object TestStorePartnershipInformationService extends StorePartnershipInformationService(connector)

  "storePartnershipInformation" when {
    "crn and postcode are not defined" should {
      "call store partnership connector with general partnership" in {
        mockConnector(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          partnershipType = GeneralPartnership,
          companyNumber = None,
          postCode = None
        )

        val res = TestStorePartnershipInformationService.storePartnershipInformation(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          postCode = None
        )

        await(res) shouldBe successResult
      }
    }

    "crn is defined but postcode is not" should {
      "call store partnership connector with limited partnership" in {
        mockConnector(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          partnershipType = LimitedPartnership,
          companyNumber = Some(testCompanyNumber),
          postCode = None
        )

        val res = TestStorePartnershipInformationService.storePartnershipInformation(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          partnershipEntity = LimitedPartnership,
          companyNumber = testCompanyNumber,
          postCode = None
        )

        await(res) shouldBe successResult
      }
    }

    "postcode is defined but crn is not" should {
      "call store partnership connector with general partnership" in {
        mockConnector(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          partnershipType = GeneralPartnership,
          companyNumber = None,
          postCode = Some(testBusinessPostcode)
        )

        val res = TestStorePartnershipInformationService.storePartnershipInformation(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          postCode = Some(testBusinessPostcode)
        )

        await(res) shouldBe successResult
      }
    }

    "crn and postcode are defined" should {
      "call store partnership connector with limited partnership" in {
        mockConnector(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          partnershipType = LimitedPartnership,
          companyNumber = Some(testCompanyNumber),
          postCode = Some(testBusinessPostcode)
        )

        val res = TestStorePartnershipInformationService.storePartnershipInformation(
          vatNumber = testVatNumber,
          sautr = Some(testSaUtr),
          partnershipEntity = LimitedPartnership,
          companyNumber = testCompanyNumber,
          postCode = Some(testBusinessPostcode)
        )

        await(res) shouldBe successResult
      }
    }
  }

}
