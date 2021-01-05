/*
 * Copyright 2021 HM Revenue & Customs
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

//$COVERAGE-OFF$Disabling scoverage

package uk.gov.hmrc.vatsignupfrontend.testonly.connectors

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.testonly.httpparsers.TriggerBulkMigrationHttpParser._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TriggerBulkMigrationConnector @Inject()(val http: HttpClient,
                                              val applicationConfig: AppConfig
                                             )(implicit ec: ExecutionContext) {

  private def triggerBulkMigrationUrl(vatNumber: String) = s"${applicationConfig.protectedMicroServiceUrl}/migration-notification/vat-number/$vatNumber"

  private val auth: (String, String) = "AUTHORIZATION" -> s"Basic ${Base64.getEncoder.encodeToString("username:password".getBytes(UTF_8))}"

  def triggerBulkMigration(vatNumber: String)(implicit hc: HeaderCarrier): Future[TriggerBulkMigrationResponse] = {

    val headerCarrier: HeaderCarrier = hc.withExtraHeaders(auth)


    http.POST[JsObject, TriggerBulkMigrationResponse](
      url = triggerBulkMigrationUrl(vatNumber),
      body = Json.obj()
    )(
      implicitly[Writes[JsObject]],
      implicitly[HttpReads[TriggerBulkMigrationResponse]],
      headerCarrier,
      implicitly[ExecutionContext]
    )
  }

}

// $COVERAGE-ON$
