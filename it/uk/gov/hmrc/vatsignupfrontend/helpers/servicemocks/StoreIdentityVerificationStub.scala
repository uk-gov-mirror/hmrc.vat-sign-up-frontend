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

package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.Constants.StoreIdentityVerificationUrlKey


object StoreIdentityVerificationStub extends WireMockMethods {
  def stubStoreIdentityVerification(vatNumber: String, continueUrl: String)(responseStatus: Int): Unit = {
    when(method = POST, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/identity-verification",
      body = Json.obj(StoreIdentityVerificationUrlKey -> continueUrl))
      .thenReturn(status = responseStatus)
  }
}
