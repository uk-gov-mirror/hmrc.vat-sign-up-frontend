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

package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub.stubStoreCompanyNumber
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreCompanyNumberHttpParser.StoreCompanyNumberSuccess

import scala.concurrent.ExecutionContext.Implicits.global

class StoreCompanyNumberConnectorISpec extends ComponentSpecBase {

  lazy val connector: StoreCompanyNumberConnector = app.injector.instanceOf[StoreCompanyNumberConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "storeCRN" when {
    "CTUTR not provided" when {
      "Backend returns a NO_CONTENT response" should {
        "return StoreCompanyNumberSuccess" in {
          stubStoreCompanyNumber(testVatNumber, testCompanyNumber, None)(NO_CONTENT)

          val res = connector.storeCompanyNumber(testVatNumber, testCompanyNumber, None)

          await(res) shouldBe Right(StoreCompanyNumberSuccess)
        }
      }
    }
    "CTUTR provided" when {
      "Backend returns a NO_CONTENT response" should {
        "return StoreCompanyNumberSuccess" in {
          stubStoreCompanyNumber(testVatNumber, testCompanyNumber, Some(testCtUtr))(NO_CONTENT)

          val res = connector.storeCompanyNumber(testVatNumber, testCompanyNumber, Some(testCtUtr))

          await(res) shouldBe Right(StoreCompanyNumberSuccess)
        }
      }
    }
  }
}