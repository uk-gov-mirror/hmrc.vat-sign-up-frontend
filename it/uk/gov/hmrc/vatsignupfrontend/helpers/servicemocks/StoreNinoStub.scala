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

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.models.NinoSource.ninoSourceFrontEndKey
import uk.gov.hmrc.vatsignupfrontend.models.{NinoSource, UserDetailsModel}

object StoreNinoStub extends WireMockMethods with FeatureSwitching {

  private def toJson(userDetailsModel: UserDetailsModel, ninoSource: NinoSource) = Json.obj(
    "firstName" -> userDetailsModel.firstName,
    "lastName" -> userDetailsModel.lastName,
    "nino" -> userDetailsModel.nino,
    "dateOfBirth" -> userDetailsModel.dateOfBirth.toLocalDate,
    ninoSourceFrontEndKey -> ninoSource
  )

  def stubStoreNino(vatNumber: String, nino: String, ninoSource: NinoSource)(responseStatus: Int): Unit = {
    when(method = PUT, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/national-insurance-number",
      body = Json.obj("nino" -> nino, ninoSourceFrontEndKey -> ninoSource))
      .thenReturn(status = responseStatus)
  }

  def stubStoreNinoSuccess(vatNumber: String, nino: String, ninoSource: NinoSource): Unit =
    stubStoreNino(vatNumber, nino, ninoSource)(NO_CONTENT)

  def stubStoreNinoNoMatch(vatNumber: String, nino: String, ninoSource: NinoSource): Unit =
    stubStoreNino(vatNumber, nino, ninoSource)(FORBIDDEN)

  def stubStoreNinoNotFound(vatNumber: String, nino: String, ninoSource: NinoSource): Unit =
    stubStoreNino(vatNumber, nino, ninoSource)(NOT_FOUND)

}
