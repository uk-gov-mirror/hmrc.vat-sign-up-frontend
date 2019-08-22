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

package uk.gov.hmrc.vatsignupfrontend.views.agent.partnerships

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentCheckYourAnswers => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._
import _root_.uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersPartnershipsIdConstants.{CompanyNumberId, HasOptionalSautrId}


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

  def sectionTest(sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String], doc: Document): Unit = {
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
    lazy val expectedSaUtrUrl = partnershipRoutes.DoesYourClientHaveAUtrController.show().url

    "the saUtr and the post code are given for a general partnership" should {
      "render the page correctly" when {
        val testPageGeneralPartnershipFSEnabled = TestView(
          name = "Check your answers View - General parnership fs enabled",
          title = messages.title,
          heading = messages.heading,
          page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = GeneralPartnership,
            Some(testBusinessPostcode),
            companyNumber = None,
            hasOptionalSautr = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))
        )

        val testPageGeneralPartnershipFSDisabled = TestView(
          name = "Check your answers View - General parnership fs disabled",
          title = messages.title,
          heading = messages.heading,
          page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = GeneralPartnership,
            Some(testBusinessPostcode),
            companyNumber = None,
            hasOptionalSautr = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))
        )

        testPageGeneralPartnershipFSEnabled.shouldHaveForm("Check your answers Form")(actionCall = testCall)

        testPageGeneralPartnershipFSDisabled.shouldHaveForm("Check your answers Form")(actionCall = testCall)

        "the GeneralPartnershipNoSAUTR feature switch is enabled" in {
          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), testPageGeneralPartnershipFSEnabled.document)
          sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.checkYourAnswersFormat, Some(expectedUrlPostCode), testPageGeneralPartnershipFSEnabled.document)

          testPageGeneralPartnershipFSEnabled.document.getElementById(utrAnswer).text shouldBe testSaUtr
          testPageGeneralPartnershipFSEnabled.document.getElementById(businessEntityAnswer).text shouldBe messages.generalPartnership
          testPageGeneralPartnershipFSEnabled.document.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
        "the GeneralPartnershipNoSAUTR feature switch is disabled" in {
          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), testPageGeneralPartnershipFSDisabled.document)
          sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.checkYourAnswersFormat, Some(expectedUrlPostCode), testPageGeneralPartnershipFSDisabled.document)

          testPageGeneralPartnershipFSDisabled.document.getElementById(utrAnswer).text shouldBe testSaUtr
          testPageGeneralPartnershipFSDisabled.document.getElementById(businessEntityAnswer).text shouldBe messages.generalPartnership
          testPageGeneralPartnershipFSDisabled.document.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
      }
    }

    "the General Partnership has an optional SA UTR" should {
      "render the page correctly" when {
        "the GeneralPartnershipNoSAUTR feature switch is disabled and hasOptionalSautr is true" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = GeneralPartnership,
            postCode = None,
            companyNumber = None,
            hasOptionalSautr = Some(true),
            generalPartnershipNoSAUTR = false,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

          lazy val doc = Jsoup.parse(page.body)

          Option(doc.getElementById(utrAnswer)).map(_.text) shouldBe Some(testSaUtr)
          sectionTest(
            sectionId = HasOptionalSautrId,
            expectedQuestion = messages.hasOptionalSautr,
            expectedAnswer = MessageLookup.Base.yes,
            expectedEditLink = Some(expectedSaUtrUrl),
            doc = doc
          )
        }
        "the GeneralPartnershipNoSAUTR feature switch is disabled and hasOptionalSautr is false" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = None,
            entityType = GeneralPartnership,
            postCode = None,
            companyNumber = None,
            hasOptionalSautr = Some(false),
            generalPartnershipNoSAUTR = false,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

          lazy val doc = Jsoup.parse(page.body)

          Option(doc.getElementById(utrAnswer)).isDefined shouldBe false
          sectionTest(
            sectionId = HasOptionalSautrId,
            expectedQuestion = messages.hasOptionalSautr,
            expectedAnswer = MessageLookup.Base.no,
            expectedEditLink = Some(expectedSaUtrUrl),
            doc = doc
          )
        }
        "the GeneralPartnershipNoSAUTR feature switch is enabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = None,
            entityType = GeneralPartnership,
            postCode = None,
            companyNumber = None,
            hasOptionalSautr = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

          lazy val doc = Jsoup.parse(page.body)

          Option(doc.getElementById(s"$HasOptionalSautrId-row")).isDefined shouldBe false
          sectionTest(
            sectionId = UtrId,
            expectedQuestion = messages.yourUtr,
            expectedAnswer = messages.noSAUTR,
            expectedEditLink = Some(expectedUrlUtr),
            doc = doc
          )
        }
      }
    }

    "the saUtr, company number and the post code are given for a limited partnership" should {
      "render the page correctly" when {
        "the GeneralPartnershipNoSAUTR feature switch is enabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = LimitedPartnership,
            Some(testBusinessPostcode),
            companyNumber = Some(testCompanyNumber),
            hasOptionalSautr = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

          lazy val doc = Jsoup.parse(page.body)

          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
          sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.checkYourAnswersFormat, Some(expectedUrlPostCode), doc)
          sectionTest(CompanyNumberId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCompanyNumber), doc)

          doc.getElementById(utrAnswer).text shouldBe testSaUtr
          doc.getElementById(businessEntityAnswer).text shouldBe messages.limitedPartnership
          doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
        "the GeneralPartnershipNoSAUTR feature switch is disabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = LimitedPartnership,
            Some(testBusinessPostcode),
            companyNumber = Some(testCompanyNumber),
            hasOptionalSautr = None,
            generalPartnershipNoSAUTR = false,
            postAction = testCall
          )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

          lazy val doc = Jsoup.parse(page.body)

          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
          sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.checkYourAnswersFormat, Some(expectedUrlPostCode), doc)
          sectionTest(CompanyNumberId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCompanyNumber), doc)

          doc.getElementById(utrAnswer).text shouldBe testSaUtr
          doc.getElementById(businessEntityAnswer).text shouldBe messages.limitedPartnership
          doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
      }
    }
  }

}
