/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.LocalDate

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser.VatNumberStored
import uk.gov.hmrc.vatsignupfrontend.models.DateModel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StoreVatNumberServiceSpec extends UnitSpec with MockitoSugar {

  val mockConnector: StoreVatNumberConnector = mock[StoreVatNumberConnector]

  object TestStoreVatNumberService extends StoreVatNumberService(mockConnector)

  val testDate = DateModel.dateConvert(LocalDate.now())

  implicit val hc = HeaderCarrier()

  "storeVatNumber with known facts" should {
    "convert the known facts into the expected strings" in {
      when(mockConnector.storeVatNumber(
        ArgumentMatchers.eq(testVatNumber),
        ArgumentMatchers.eq(testBusinessPostcode.postCode),
        ArgumentMatchers.eq(testDate.toLocalDate.toString)
      )(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right(VatNumberStored)))

      val r = TestStoreVatNumberService.storeVatNumber(testVatNumber, testBusinessPostcode, testDate)

      // null pointer exception would have been thrown if the arguments weren't converted to the expected string format
      await(r) shouldBe Right(VatNumberStored)
    }
  }

}
