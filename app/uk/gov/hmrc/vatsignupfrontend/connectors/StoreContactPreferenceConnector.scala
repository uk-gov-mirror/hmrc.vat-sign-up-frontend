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

package uk.gov.hmrc.vatsignupfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreContactPreferenceHttpParser.StoreContactPreferenceResponse
import uk.gov.hmrc.vatsignupfrontend.models.ContactPreference

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreContactPreferenceConnector @Inject()(val http: HttpClient,
                                                val applicationConfig: AppConfig)(implicit ec: ExecutionContext) {
  val contactPreferenceKey = "contactPreference"

  def storeContactPreference(vatNumber: String, contactPreference: ContactPreference)(implicit hc: HeaderCarrier): Future[StoreContactPreferenceResponse] = {
    val jsonBody = Json.obj(contactPreferenceKey -> contactPreference.toString)

    http.POST[JsObject, StoreContactPreferenceResponse](applicationConfig.storeContactPreferenceUrl(vatNumber), jsonBody)
  }
}
