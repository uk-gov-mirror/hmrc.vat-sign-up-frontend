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

package uk.gov.hmrc.vatsignupfrontend.helpers

import java.time.LocalDate
import java.util.UUID
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.vatsignupfrontend.Constants.Enrolments._
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, PostCode, UserDetailsModel}

import scala.util.Random

object TestConstants {
  val testVatNumber: String = TestConstantsGenerator.randomVatNumber
  val testInvalidVatNumber: String = "999999999"
  val testName: String = UUID.randomUUID().toString
  val testBusinessPostcode: PostCode = PostCode(TestConstantsGenerator.randomPostCode)
  val testCompanyNumber: String = TestConstantsGenerator.randomCrnNumeric
  val testCompanyName: String = Random.alphanumeric.take(10).mkString
  val testEmail: String = TestConstantsGenerator.randomEmail
  val testNino: String = new Generator().nextNino.nino
  val testSaUtr: String = new Generator().nextAtedUtr.utr
  val testUri: String = "/test/uri"

  val testAgentEnrolment: Enrolment = Enrolment(agentEnrolmentKey)
  val testVatDecEnrolment: Enrolment = Enrolment(VatDecEnrolmentKey) withIdentifier(VatReferenceKey, testVatNumber)
  val testIRSAEnrolment: Enrolment = Enrolment(IRSAEnrolmentKey) withIdentifier(IRSAReferenceKey, testSaUtr)

  val testUserDetails: UserDetailsModel =
    UserDetailsModel(
      firstName = UUID.randomUUID().toString,
      lastName = UUID.randomUUID().toString,
      nino = testNino,
      dateOfBirth = DateModel.dateConvert(LocalDate.now())
    )
  val testUserDetailsJson: String = Json.toJson(testUserDetails).toString()
}
