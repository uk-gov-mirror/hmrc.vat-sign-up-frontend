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

package uk.gov.hmrc.vatsignupfrontend.views.agent.partnerships

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentCheckYourAnswers => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._

class CheckYourAnswersViewSpec extends ViewSpec {


  val env = Environment.simple()
  val configuration = Configuration.load(env)

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

  def sectionTest(sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String], doc: Document) = {
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

  "the Check Your Answers View" when {
    val utrAnswer = "utr-answer"
    val crnAnswer = "company-number-answer"
    val pobAnswer = "business-post-code-answer"
    val expectedUrlUtr = partnershipRoutes.CapturePartnershipUtrController.show().url
    val expectedUrlCrn = partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url
    val expectedUrlPostCode = partnershipRoutes.PartnershipPostCodeController.show().url

    "the saUtr, the crn and the post code are given" should {
      val testPage = TestView(
        name = "Check your answers View",
        title = messages.title,
        heading = messages.heading,
        page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          crn = Some(testCompanyNumber),
          postCode = Some(testBusinessPostcode),
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))
      )

      testPage.shouldHaveH2(messages.subHeading)

      testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)

      "render the page correctly" in {
        def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          crn = Some(testCompanyNumber),
          postCode = Some(testBusinessPostcode),
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

        lazy val doc = Jsoup.parse(page.body)

        sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
        sectionTest(CrnId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCrn), doc)
        sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.postCode, Some(expectedUrlPostCode), doc)

        doc.getElementById(utrAnswer).text shouldBe testSaUtr
        doc.getElementById(crnAnswer).text shouldBe testCompanyNumber
        doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.postCode
      }
    }

    "the saUtr and the crn are given but the post code isn't" should {
      "render the page correctly" in {
        def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          crn = Some(testCompanyNumber),
          postCode = None,
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

        lazy val doc = Jsoup.parse(page.body)

        sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
        sectionTest(CrnId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCrn), doc)

        doc.getElementById(utrAnswer).text shouldBe testSaUtr
        doc.getElementById(crnAnswer).text shouldBe testCompanyNumber
        doc.getElementById(pobAnswer) shouldBe null
      }
    }

    "the saUtr and the post code are given but the crn isn't" should {
      "render the page correctly" in {
        def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          crn = None,
          postCode = Some(testBusinessPostcode),
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

        lazy val doc = Jsoup.parse(page.body)

        sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
        sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.postCode, Some(expectedUrlPostCode), doc)

        doc.getElementById(utrAnswer).text shouldBe testSaUtr
        doc.getElementById(crnAnswer) shouldBe null
        doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.postCode
      }
    }

    "the saUtr is given but the crn and the post code aren't" should {
      "render the page correctly" in {
        def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          crn = None,
          postCode = None,
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

        lazy val doc = Jsoup.parse(page.body)

        sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)

        doc.getElementById(utrAnswer).text shouldBe testSaUtr
        doc.getElementById(crnAnswer) shouldBe null
        doc.getElementById(pobAnswer) shouldBe null
      }
    }
  }

}
