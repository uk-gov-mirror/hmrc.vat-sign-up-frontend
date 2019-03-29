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

package uk.gov.hmrc.vatsignupfrontend.views.principal.partnerships


import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CaptureBusinessEntity, PartnershipsCYA => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership, No, Yes}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersPartnershipsIdConstants._

class CheckYourAnswersPartnershipsViewSpec extends ViewSpec {

  lazy val appConfig = app.injector.instanceOf[AppConfig]

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

  def sectionTest(doc: Document, sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String]): Unit = {
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

  "Limited Partnership Check Your Answers View" should {

    lazy val limitedPartnershipCyaPage: Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships(
      entityType = LimitedPartnership,
      companyUtr = Some(testCompanyUtr),
      companyNumber = Some(testCompanyNumber),
      postCode = Some(testBusinessPostcode),
      postAction = testCall,
      hasOptionalSautr = None
    )(FakeRequest(), applicationMessages, appConfig)

    lazy val limitedPartnershipCyaDoc = Jsoup.parse(limitedPartnershipCyaPage.body)

    val testPage = TestView(
      name = "Limited Partnership Check Your Answers View",
      title = messages.title,
      heading = messages.heading,
      page = limitedPartnershipCyaPage
    )

    testPage.shouldHaveForm("Check Your Answers Form")(actionCall = testCall)

    "display the correct info for business entity" in {
      val sectionId = BusinessEntityId
      val expectedQuestion = messages.businessEntity
      val expectedAnswer = CaptureBusinessEntity.radioLimitedPartnership
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBusinessEntityController.show().url

      sectionTest(
        doc = limitedPartnershipCyaDoc,
        sectionId = sectionId,
        expectedQuestion = expectedQuestion,
        expectedAnswer = expectedAnswer,
        expectedEditLink = Some(expectedEditLink)
      )
    }

    "display the correct info for company number" in {
      val sectionId = CompanyNumberId
      val expectedQuestion = messages.companyNumber
      val expectedAnswer = testCompanyNumber
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.CapturePartnershipCompanyNumberController.show().url

      sectionTest(
        doc = limitedPartnershipCyaDoc,
        sectionId = sectionId,
        expectedQuestion = expectedQuestion,
        expectedAnswer = expectedAnswer,
        expectedEditLink = Some(expectedEditLink)
      )
    }

    "display the correct info for company utr" in {
      val sectionId = CompanyUtrId
      val expectedQuestion = messages.companyUtr
      val expectedAnswer = testCompanyUtr
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.CapturePartnershipUtrController.show().url

      sectionTest(
        doc = limitedPartnershipCyaDoc,
        sectionId = sectionId,
        expectedQuestion = expectedQuestion,
        expectedAnswer = expectedAnswer,
        expectedEditLink = Some(expectedEditLink)
      )
    }

    "display the correct info for post code" in {
      val sectionId = PartnershipPostCodeId
      val expectedQuestion = messages.postCode
      val expectedAnswer = testBusinessPostcode.checkYourAnswersFormat
      val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.PrincipalPlacePostCodeController.show().url

      sectionTest(
        doc = limitedPartnershipCyaDoc,
        sectionId = sectionId,
        expectedQuestion = expectedQuestion,
        expectedAnswer = expectedAnswer,
        expectedEditLink = Some(expectedEditLink)
      )
    }

  }

  "General Partnership Check Your Answers View" when {

    "the user does not have an Sautr" should {

      lazy val generalPartnershipCyaPage: Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships(
        entityType = GeneralPartnership,
        companyUtr = Some(testCompanyUtr),
        companyNumber = None,
        postCode = Some(testBusinessPostcode),
        postAction = testCall,
        hasOptionalSautr = Some(false)
      )(FakeRequest(), applicationMessages, appConfig)

      lazy val generalPartnershipCyaDoc = Jsoup.parse(generalPartnershipCyaPage.body)

      val testPage = TestView(
        name = "General Partnership without Sautr Check Your Answers",
        title = messages.title,
        heading = messages.heading,
        page = generalPartnershipCyaPage
      )

      testPage.shouldHaveForm("Check Your Answers Form")(actionCall = testCall)


      "display the correct info for business entity" in {
        val sectionId = BusinessEntityId
        val expectedQuestion = messages.businessEntity
        val expectedAnswer = CaptureBusinessEntity.radioGeneralPartnership
        val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBusinessEntityController.show().url

        sectionTest(
          doc = generalPartnershipCyaDoc,
          sectionId = sectionId,
          expectedQuestion = expectedQuestion,
          expectedAnswer = expectedAnswer,
          expectedEditLink = Some(expectedEditLink)
        )
      }

      "display the correct info for company utr" in {
        val sectionId = CompanyUtrId
        val expectedQuestion = messages.companyUtr
        val expectedAnswer = testCompanyUtr
        val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.CapturePartnershipUtrController.show().url

        sectionTest(
          doc = generalPartnershipCyaDoc,
          sectionId = sectionId,
          expectedQuestion = expectedQuestion,
          expectedAnswer = expectedAnswer,
          expectedEditLink = Some(expectedEditLink)
        )
      }

      "display the correct info for post code" in {
        val sectionId = PartnershipPostCodeId
        val expectedQuestion = messages.postCode
        val expectedAnswer = testBusinessPostcode.checkYourAnswersFormat
        val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.PrincipalPlacePostCodeController.show().url

        sectionTest(
          doc = generalPartnershipCyaDoc,
          sectionId = sectionId,
          expectedQuestion = expectedQuestion,
          expectedAnswer = expectedAnswer,
          expectedEditLink = Some(expectedEditLink)
        )
      }

      "display the correct info for Do You Have A Utr" in {
        val sectionId = HasOptionalSautrId
        val expectedQuestion = messages.hasOptionalSautr
        val expectedAnswer = MessageLookup.Base.no
        val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.DoYouHaveAUtrController.show().url

        sectionTest(
          doc = generalPartnershipCyaDoc,
          sectionId = sectionId,
          expectedQuestion = expectedQuestion,
          expectedAnswer = expectedAnswer,
          expectedEditLink = Some(expectedEditLink)
        )
      }
    }

    "the user has an Sautr" should {

      lazy val generalPartnershipCyaPage: Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships(
        entityType = GeneralPartnership,
        companyUtr = None,
        companyNumber = None,
        postCode = None,
        postAction = testCall,
        hasOptionalSautr = Some(true)
      )(FakeRequest(), applicationMessages, appConfig)

      lazy val generalPartnershipCyaDoc = Jsoup.parse(generalPartnershipCyaPage.body)

      val testPage = TestView(
        name = "General Partnership with Sautr Check Your Answers View",
        title = messages.title,
        heading = messages.heading,
        page = generalPartnershipCyaPage
      )

      testPage.shouldHaveForm("Check Your Answers Form")(actionCall = testCall)


      "display the correct info for business entity" in {
        val sectionId = BusinessEntityId
        val expectedQuestion = messages.businessEntity
        val expectedAnswer = CaptureBusinessEntity.radioGeneralPartnership
        val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBusinessEntityController.show().url

        sectionTest(
          doc = generalPartnershipCyaDoc,
          sectionId = sectionId,
          expectedQuestion = expectedQuestion,
          expectedAnswer = expectedAnswer,
          expectedEditLink = Some(expectedEditLink)
        )
      }

      "display the correct info for Do You Have A Sautr" in {
        val sectionId = HasOptionalSautrId
        val expectedQuestion = messages.hasOptionalSautr
        val expectedAnswer = MessageLookup.Base.yes
        val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes.DoYouHaveAUtrController.show().url

        sectionTest(
          doc = generalPartnershipCyaDoc,
          sectionId = sectionId,
          expectedQuestion = expectedQuestion,
          expectedAnswer = expectedAnswer,
          expectedEditLink = Some(expectedEditLink)
        )
      }
    }
  }
}
