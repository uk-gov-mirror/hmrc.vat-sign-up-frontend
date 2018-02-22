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

//$COVERAGE-OFF$Disabling scoverage

package uk.gov.hmrc.vatsubscriptionfrontend.testonly.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsubscriptionfrontend.testonly.models.{DeleteRecordFailure, DeleteRecordSuccess}


object DeleteRecordHttpParser {
  type DeleteRecordResponse = Either[DeleteRecordFailure, DeleteRecordSuccess.type]

  implicit object DeleteRecordHttpReads extends HttpReads[DeleteRecordResponse] {
    override def read(method: String, url: String, response: HttpResponse): DeleteRecordResponse = {
      response.status match {
        case NO_CONTENT => Right(DeleteRecordSuccess)
        case status => Left(DeleteRecordFailure(status))
      }
    }
  }

}

// $COVERAGE-ON$

