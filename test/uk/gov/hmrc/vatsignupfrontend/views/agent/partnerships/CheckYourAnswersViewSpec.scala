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
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._
import _root_.uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersPartnershipsIdConstants.CompanyNumberId


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
    val businessEntityAnswer = "business-entity-answer"
    val crnAnswer = "company-number-answer"
    val pobAnswer = "business-post-code-answer"
    lazy val expectedUrlUtr = partnershipRoutes.CapturePartnershipUtrController.show().url
    lazy val expectedUrlPostCode = partnershipRoutes.PartnershipPostCodeController.show().url
    lazy val expectedUrlCompanyNumber = partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url

    "the saUtr and the post code are given for a general partnership" should {
      val testPage = TestView(
        name = "Check your answers View",
        title = messages.title,
        heading = messages.heading,
        page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          entityType = GeneralPartnership,
          testBusinessPostcode,
          None,
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))
      )

      testPage.shouldHaveH2(messages.subHeading)

      testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)

      "render the page correctly" in {
        lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          entityType = GeneralPartnership,
          postCode = testBusinessPostcode,
          companyNumber = None,
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

        lazy val doc = Jsoup.parse(page.body)

        sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
        sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.postCode, Some(expectedUrlPostCode), doc)

        doc.getElementById(utrAnswer).text shouldBe testSaUtr
        doc.getElementById(businessEntityAnswer).text shouldBe messages.generalPartnership
        doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.postCode
      }
    }
    "the saUtr, company number and the post code are given for a limited partnership" should {
      "render the page correctly" in {
        lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
          utr = testSaUtr,
          entityType = GeneralPartnership,
          postCode = testBusinessPostcode,
          companyNumber = Some(testCompanyNumber),
          postAction = testCall
        )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

        lazy val doc = Jsoup.parse(page.body)

        sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
        sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.postCode, Some(expectedUrlPostCode), doc)
        sectionTest(CompanyNumberId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCompanyNumber), doc)

        doc.getElementById(utrAnswer).text shouldBe testSaUtr
        doc.getElementById(businessEntityAnswer).text shouldBe messages.generalPartnership
        doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.postCode
      }
    }
  }

}
