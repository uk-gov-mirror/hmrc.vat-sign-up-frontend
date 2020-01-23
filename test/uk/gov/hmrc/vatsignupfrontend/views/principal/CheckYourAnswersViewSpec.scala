/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalCheckYourAnswers => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._

class CheckYourAnswersViewSpec extends ViewSpec {

  val testRegistrationDate: DateModel = DateModel.dateConvert(LocalDate.now())
  val testEntity: BusinessEntity = SoleTrader

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def page(optBox5Figure: Option[String] = None,
           optLastReturnMonthPeriod: Option[String] = None,
           optPreviousVatReturn: Option[String] = None,
           optPostCode: Option[PostCode] = Some(testBusinessPostcode)
          ): Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.check_your_answers(
    vatNumber = testVatNumber,
    registrationDate = testRegistrationDate,
    optPostCode = optPostCode,
    optPreviousVatReturn = optPreviousVatReturn,
    optBox5Figure = optBox5Figure,
    optLastReturnMonthPeriod = optLastReturnMonthPeriod,
    postAction = testCall
  )(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  lazy val pageDefault: Html = page()

  lazy val pageWithAdditionalKnownFacts: Html = page(Some(testBox5Figure), Some(testLastReturnMonthPeriod), Some(Yes.stringValue))

  lazy val pageWithoutPostCode: Html = page(Some(testBox5Figure), Some(testLastReturnMonthPeriod), Some(Yes.stringValue), None)

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

    "display the correct answer for Previous VAT return" in {
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.PreviousVatReturnController.show().url

      sectionTest(
        page = pageWithAdditionalKnownFacts,
        sectionId = PreviousVatReturnId,
        expectedQuestion = messages.previousVatReturn,
        expectedAnswer = Yes.stringValue,
        expectedEditLink = Some(expectedEditLink)
      )
    }

    "display the correct answer for Box5Value" in {
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBox5FigureController.show().url

      sectionTest(
        page = pageWithAdditionalKnownFacts,
        sectionId = VatBox5FigureId,
        expectedQuestion = messages.box5Figure,
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

  "Check your answers view without overseas postcode" should {

    val testPage = TestView(
      name = "Check your answers View",
      title = messages.title,
      heading = messages.heading,
      page = pageWithoutPostCode
    )

    testPage.shouldHaveH2(messages.subHeading)

    testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)

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

    "display the correct answer for Box5Value" in {
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBox5FigureController.show().url

      sectionTest(
        page = pageWithAdditionalKnownFacts,
        sectionId = VatBox5FigureId,
        expectedQuestion = messages.box5Figure,
        expectedAnswer = s"£99,999,999,999.99",
        expectedEditLink = Some(expectedEditLink)
      )
    }

    "display the correct answer for Previous VAT return" in {
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.PreviousVatReturnController.show().url

      sectionTest(
        page = pageWithAdditionalKnownFacts,
        sectionId = PreviousVatReturnId,
        expectedQuestion = messages.previousVatReturn,
        expectedAnswer = Yes.stringValue,
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
