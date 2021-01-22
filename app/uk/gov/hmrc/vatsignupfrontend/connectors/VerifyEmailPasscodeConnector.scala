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

import play.api.http.Status.{CREATED, FORBIDDEN, NOT_FOUND, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.models.{VerifyEmailPasscodeResult, _}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailPasscodeConnector @Inject()(httpClient: HttpClient, config: AppConfig)
                                            (implicit ec: ExecutionContext) {

  val CodeKey = "code"
  val PasscodeMismatchKey = "PASSCODE_MISMATCH"
  val PasscodeNotFoundKey = "PASSCODE_NOT_FOUND"
  val MaxAttemptsExceededKey = "MAX_EMAILS_EXCEEDED"

  def verifyEmailVerificationPasscode(email: String, passcode: String)(implicit hc: HeaderCarrier): Future[VerifyEmailPasscodeResult] = {
    val jsonBody = Json.obj(
      "email" -> email,
      "passcode" -> passcode
    )

    httpClient.POST(config.verifyEmailVerificationPasscodeUrl(), jsonBody) map { response =>
      def errorCode: Option[String] = (response.json \ CodeKey).asOpt[String]

      response.status match {
        case CREATED => EmailVerifiedSuccessfully
        case NO_CONTENT => EmailAlreadyVerified
        case NOT_FOUND if errorCode contains PasscodeMismatchKey => PasscodeMismatch
        case NOT_FOUND if errorCode contains PasscodeNotFoundKey => PasscodeNotFound
        case FORBIDDEN if errorCode contains MaxAttemptsExceededKey => MaxAttemptsExceeded
        case status =>
          throw new InternalServerException(s"Unexpected response returned from VerifyEmailPasscode endpoint - Status: $status, response: ${response.body}")
      }
    }
  }
}
