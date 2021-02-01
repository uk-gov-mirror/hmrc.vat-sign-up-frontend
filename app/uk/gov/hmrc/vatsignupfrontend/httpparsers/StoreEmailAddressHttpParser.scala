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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StoreEmailAddressHttpParser {

  type StoreEmailAddressResponse = Either[StoreEmailAddressFailure, StoreEmailAddressSuccess]

  val emailVerifiedKey = "emailVerified"
  val reasonKey = "reason"

  val storedSuccessfully = "OK"
  val passcodeNotFound = "PASSCODE_NOT_FOUND"
  val passcodeMismatch = "PASSCODE_MISMATCH"
  val maxAttemptsExceeded = "MAX_PASSCODE_ATTEMPTS_EXCEEDED"

  implicit object StoreEmailAddressHttpReads extends HttpReads[StoreEmailAddressResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreEmailAddressResponse = {
      def emailVerified: Option[Boolean] = (response.json \ emailVerifiedKey).asOpt[Boolean]

      def passcodeStatus: Option[String] = (response.json \ reasonKey).asOpt[String]

      (response.status, emailVerified, passcodeStatus) match {
        case (BAD_REQUEST, _, Some(`maxAttemptsExceeded`)) =>
          Left(MaxAttemptsExceeded)
        case (BAD_REQUEST, _, Some(`passcodeNotFound`)) =>
          Left(PasscodeNotFound)
        case (BAD_REQUEST, _, Some(`passcodeMismatch`)) =>
          Left(PasscodeMismatch)
        case (OK, _, Some(`storedSuccessfully`)) =>
          Right(StoreEmailAddressSuccess(emailVerified = true))
        case (OK, Some(verified), _) =>
          Right(StoreEmailAddressSuccess(verified))
        case (status, _, _) =>
          Left(StoreEmailAddressFailureStatus(status))
      }
    }
  }

  case class StoreEmailAddressSuccess(emailVerified: Boolean)

  sealed trait StoreEmailAddressFailure

  case class StoreEmailAddressFailureStatus(status: Int) extends StoreEmailAddressFailure

  case object PasscodeMismatch extends StoreEmailAddressFailure

  case object MaxAttemptsExceeded extends StoreEmailAddressFailure

  case object PasscodeNotFound extends StoreEmailAddressFailure

}

