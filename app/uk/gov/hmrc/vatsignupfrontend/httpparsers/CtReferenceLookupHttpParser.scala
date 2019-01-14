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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object CtReferenceLookupHttpParser {
  type CtReferenceLookupResponse = Either[CtReferenceLookupFailure, CtReferenceIsFound.type]

  implicit object CtReferenceLookupHttpReads extends HttpReads[CtReferenceLookupResponse] {
    override def read(method: String, url: String, response: HttpResponse): CtReferenceLookupResponse = {
      response.status match {
        case OK => Right(CtReferenceIsFound)
        case NOT_FOUND => Left(CtReferenceNotFound)
        case status => Left(CtReferenceLookupFailureResponse(status))
      }
    }
  }

  sealed trait CtReferenceLookupFailure

  case object CtReferenceIsFound

  case object CtReferenceNotFound extends CtReferenceLookupFailure

  case class CtReferenceLookupFailureResponse(status: Int) extends CtReferenceLookupFailure

}


