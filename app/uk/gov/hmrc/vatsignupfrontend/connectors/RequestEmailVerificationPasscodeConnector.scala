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

package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.http.Status.{CONFLICT, CREATED}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.models.{AlreadyVerifiedEmailAddress, RequestEmailPasscodeResult, RequestEmailPasscodeSuccessful}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RequestEmailVerificationPasscodeConnector @Inject()(val http: HttpClient,
                                                          val applicationConfig: AppConfig
                                                         )(implicit ec: ExecutionContext) {

  def requestEmailVerificationPasscode(email: String, language: String)(implicit hc: HeaderCarrier): Future[RequestEmailPasscodeResult] = {

    val url = applicationConfig.requestEmailVerificationPasscodeUrl()

    val jsonBody = Json.obj("email" -> email, "serviceName" -> "VAT Signup", "lang" -> language)

    http.POST(url, jsonBody).map {
      _.status match {
        case CREATED => RequestEmailPasscodeSuccessful
        case CONFLICT => AlreadyVerifiedEmailAddress
        case status => throw new InternalServerException(s"requestEmailVerificationPasscode failed: email-verification returned $status")
      }
    }
  }

}
