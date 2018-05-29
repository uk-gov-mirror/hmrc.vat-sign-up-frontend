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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import java.time.LocalDate

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalConfirmYourDetails => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.ConfirmClientIdConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testNino


class ConfirmYourDetailsViewSpec extends ViewSpec {

  val testFirstName = "Test"
  val testLastName = "User"
  val testDob = DateModel.dateConvert(LocalDate.now())
  val testClientDetails = UserDetailsModel(
    testFirstName,
    testLastName,
    testNino,
    testDob)

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.confirm_your_details(
    userDetailsModel = testClientDetails,
    postAction = testCall
  )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

  lazy val doc = Jsoup.parse(page.body)

  val questionId: String => String = (sectionId: String) => s"$sectionId-question"
  val answerId: String => String = (sectionId: String) => s"$sectionId-answer"

  def questionStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__heading tabular-data__heading--label"
  }

  def answerStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__data-1"
  }


  "Confirm Your Details page view" should {

    val testPage = TestView(
      name = "Confirm Your Details View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveForm("Confirm Your Details Form")(actionCall = testCall)

//    TODO: Update change link once controller created
//    testPage.shouldHaveALink(
//      id = "changeLink",
//      text = messages.link,
//      href = routes.CaptureEmailController.show().url
//    )

  }

  def sectionTest(sectionId: String, expectedQuestion: String, expectedAnswer: String) = {
    val accountingPeriod = doc.getElementById(sectionId)
    val question = doc.getElementById(questionId(sectionId))
    val answer = doc.getElementById(answerId(sectionId))

    questionStyleCorrectness(question)
    answerStyleCorrectness(answer)
    question.text() shouldBe expectedQuestion
    answer.text() shouldBe expectedAnswer
  }

  "display the correct info for firstName" in {
    val sectionId = FirstNameId
    val expectedQuestion = messages.firstName
    val expectedAnswer = testFirstName

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer
    )
  }

  "display the correct info for lastName" in {
    val sectionId = LastNameId
    val expectedQuestion = messages.lastName
    val expectedAnswer = testLastName

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer
    )
  }

  "display the correct info for dob" in {
    val sectionId = DobId
    val expectedQuestion = messages.dob
    val expectedAnswer = testDob.toCheckYourAnswersDateFormat

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer
    )
  }

  "display the correct info for nino" in {
    val sectionId = NinoId
    val expectedQuestion = messages.nino
    val expectedAnswer = testNino

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer

    )
  }


}
