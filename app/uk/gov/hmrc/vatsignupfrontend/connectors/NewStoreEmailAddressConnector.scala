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

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.connectors.NewStoreEmailAddressConnector._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NewStoreEmailAddressConnector @Inject()(http: HttpClient)(implicit ec: ExecutionContext, appConfig: AppConfig) {

  object RequestKeys {
    val transactionEmailKey = "transactionEmail"
    val passcodeKey = "passCode"
  }

  val reasonKey = "reason"
  val passcodeNotFound = "PASSCODE_NOT_FOUND"
  val passcodeMismatch = "PASSCODE_MISMATCH"
  val maxAttemptsExceeded = "MAX_PASSCODE_ATTEMPTS_EXCEEDED"

  def storeTransactionEmailAddress(vatNumber: String,
                                   transactionEmail: String,
                                   passcode: String
                                  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[NewStoreEmailAddressResponse] = {

    def passcodeStatus(response: HttpResponse): String = (Json.parse(response.body) \ reasonKey).as[String]

    http.PUT(
      url = appConfig.storeTransactionEmailValidatedUrl(vatNumber),
      body = Json.obj(
        RequestKeys.transactionEmailKey -> transactionEmail,
        RequestKeys.passcodeKey -> passcode)
    ) map { response =>
      response.status match {
        case CREATED =>
          Right(NewStoreEmailAddressSuccess)
        case NOT_FOUND if passcodeStatus(response) == passcodeNotFound =>
          Left(PasscodeNotFound)
        case BAD_REQUEST if passcodeStatus(response) == passcodeMismatch =>
          Left(PasscodeMismatch)
        case BAD_REQUEST if passcodeStatus(response) == maxAttemptsExceeded =>
          Left(MaxAttemptsExceeded)
        case status =>
          Left(NewStoreEmailAddressFailureStatus(status))
      }
    }
  }

}

object NewStoreEmailAddressConnector {
  type NewStoreEmailAddressResponse = Either[NewStoreEmailAddressFailure,  NewStoreEmailAddressSuccess.type]

  case object NewStoreEmailAddressSuccess
  sealed trait NewStoreEmailAddressFailure

  case class NewStoreEmailAddressFailureStatus(status: Int) extends NewStoreEmailAddressFailure
  case object PasscodeMismatch extends NewStoreEmailAddressFailure
  case object MaxAttemptsExceeded extends NewStoreEmailAddressFailure
  case object PasscodeNotFound extends NewStoreEmailAddressFailure
}