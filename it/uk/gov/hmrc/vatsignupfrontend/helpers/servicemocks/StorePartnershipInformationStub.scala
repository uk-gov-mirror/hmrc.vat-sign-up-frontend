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

package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType._

object StorePartnershipInformationStub extends WireMockMethods with FeatureSwitching {

  private def toJson(sautr: String,
                     partnershipType: PartnershipEntityType,
                     companyNumber: Option[String]
                    ) = Json.obj(
    "partnershipType" -> Json.toJson(partnershipType),
    "sautr" -> sautr
  ).++(
    companyNumber match {
      case Some(crn) => Json.obj("crn" -> crn)
      case _ => Json.obj()
    }
  )

  def stubStorePartnershipInformation(vatNumber: String,
                                      sautr: String,
                                      partnershipEntityType: PartnershipEntityType,
                                      companyNumber: Option[String]
                                     )(responseStatus: Int): Unit = {
    when(method = POST, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/partnership-information",
      body = toJson(sautr, partnershipEntityType, companyNumber))
      .thenReturn(status = responseStatus)
  }
}
