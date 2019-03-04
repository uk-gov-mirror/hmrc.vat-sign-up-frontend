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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import java.time.LocalDate

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.http.NotImplementedHttpRequestHandler
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalCheckYourAnswers => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, SoleTrader}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._

class CheckYourAnswersViewSpec extends ViewSpec {


  val testRegistrationDate = DateModel.dateConvert(LocalDate.now())
  val testEntity = SoleTrader

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  def page(optBox5Value: Option[String] = None,
           optLastReturnMonthPeriod: Option[String] = None
          ): Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.check_your_answers(
    vatNumber = testVatNumber,
    registrationDate = testRegistrationDate,
    postCode = testBusinessPostcode,
    optBox5Value = optBox5Value,
    optLastReturnMonthPeriod = optLastReturnMonthPeriod,
    postAction = testCall
  )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

  lazy val pageDefault = page()

  lazy val pageWithAdditionalKnownFacts = page(Some(testBox5Figure), Some(testLastReturnMonthPeriod))

  val questionId: String => String = (sectionId: String) => s"$sectionId-question"
  val answerId: String => String = (sectionId: String) => s"$sectionId-answer"
  val editLinkId: String => String = (sectionId: String) => s"$sectionId-edit"

  def questionStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__heading tabular-data__heading--label"
  }

  def answerStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__data-1"
  }

  def editLinkStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__data-2"
  }

  "Check your answers view" should {

    val testPage = TestView(
      name = "Check your answers View",
      title = messages.title,
      heading = messages.heading,
      page = pageDefault
    )

    testPage.shouldHaveH2(messages.subHeading)

    testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)
  }

  def sectionTest(page: Html, sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String]): Unit = {
    lazy val doc = Jsoup.parse(page.body)
    val accountingPeriod = doc.getElementById(sectionId)
    val question = doc.getElementById(questionId(sectionId))
    val answer = doc.getElementById(answerId(sectionId))
    val editLink = doc.getElementById(editLinkId(sectionId))

    questionStyleCorrectness(question)
    answerStyleCorrectness(answer)
    if (expectedEditLink.nonEmpty) editLinkStyleCorrectness(editLink)

    question.text() shouldBe expectedQuestion
    answer.text() shouldBe expectedAnswer
    if (expectedEditLink.nonEmpty) {
      editLink.attr("href") shouldBe expectedEditLink.get
      editLink.select("span").text() shouldBe expectedQuestion
      editLink.select("span").hasClass("visuallyhidden") shouldBe true
    }
  }

  "display the correct info for VatNumber" in {
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureVatNumberController.show().url

    sectionTest(
      page = pageDefault,
      sectionId = VatNumberId,
      expectedQuestion = messages.yourVatNumber,
      expectedAnswer = testVatNumber,
      expectedEditLink = Some(expectedEditLink)
    )
  }

  "display the correct info for VatRegistrationDate" in {
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureVatRegistrationDateController.show().url

    sectionTest(
      page = pageDefault,
      sectionId = VatRegistrationDateId,
      expectedQuestion = messages.vatRegistrationDate,
      expectedAnswer = testRegistrationDate.toCheckYourAnswersDateFormat,
      expectedEditLink = Some(expectedEditLink)
    )
  }

  "display the correct info for BusinessPostCode" in {
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.BusinessPostCodeController.show().url

    sectionTest(
      page = pageDefault,
      sectionId = BusinessPostCodeId,
      expectedQuestion = messages.businessPostCode,
      expectedAnswer = testBusinessPostcode.checkYourAnswersFormat,
      expectedEditLink = Some(expectedEditLink)
    )
  }

  "Check your answers view with additional known facts" should {

    val testPage = TestView(
      name = "Check your answers View",
      title = messages.title,
      heading = messages.heading,
      page = pageWithAdditionalKnownFacts
    )

    testPage.shouldHaveH2(messages.subHeading)

    testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)

    "display the correct answer for Box5Value" in {
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBox5FigureController.show().url

      sectionTest(
        page = pageWithAdditionalKnownFacts,
        sectionId = VatBox5ValueId,
        expectedQuestion = messages.box5Value,
        expectedAnswer = s"£99,999,999,999.99",
        expectedEditLink = Some(expectedEditLink)
      )
    }

    "display the correct answer for LastReturnMonth" in {
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureLastReturnMonthPeriodController.show().url

      sectionTest(
        page = pageWithAdditionalKnownFacts,
        sectionId = VatLastReturnMonthId,
        expectedQuestion = messages.lastReturnMonth,
        expectedAnswer = testLastReturnMonthPeriod,
        expectedEditLink = Some(expectedEditLink)
      )
    }
  }

}
