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

package uk.gov.hmrc.vatsignupfrontend.views.agent

import org.jsoup.Jsoup
import play.api.{Configuration, Environment}
import play.api.i18n.Messages.Implicits.applicationMessages
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.routes._
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.soletrader.{routes => soletraderRoutes}
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentCheckYourAnswersFinal => msg}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.utils.SummarySectionTesting
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.check_your_answers_final
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.BusinessEntityHelper.getBusinessEntityName

class CheckYourAnswersFinalSpec extends ViewSpec with SummarySectionTesting {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  def testView(vatNumber: String = testVatNumber,
               businessEntity: BusinessEntity,
               optCompanyNumber: Option[String] = None,
               optCompanyName: Option[String] = None,
               optNino: Option[String] = None,
               optPartnershipUtr: Option[String] = None,
               agentEmail: String = testAgentEmail,
               optClientEmail: Option[String] = Some(testEmail),
               skipCidCheck: Boolean = true,
               contactPreference: ContactPreference = Digital
              ): Html =
    check_your_answers_final(
      vatNumber = vatNumber,
      businessEntity = businessEntity,
      optCompanyNumber = optCompanyNumber,
      optCompanyName = optCompanyName,
      optNino = optNino,
      optPartnershipUtr = optPartnershipUtr,
      agentEmail = agentEmail,
      optClientEmail = optClientEmail,
      contactPreference = contactPreference,
      skipCidCheck = skipCidCheck,
      postAction = testCall
    )(viewTestRequest, applicationMessages, new AppConfig(configuration, env))

  "The final check your answers page" when {
    "The business is a Sole trader" when {
      "the SkipCidCheck feature switch is enabled" should {
        lazy val page = testView(businessEntity = SoleTrader, optNino = Some(testNino))
        lazy val doc = Jsoup.parse(page.body)

        val testPage = TestView(
          name = "Final check your answers - Sole trader",
          title = msg.title,
          heading = msg.heading,
          page = page
        )

        testPage.shouldHaveAcceptAndSendButton()

        "have rows for VRN, NINO, agent email, client email and contact preference" in {
          val page = testView(businessEntity = SoleTrader, optNino = Some(testNino))
          val doc = Jsoup.parse(page.body)

          doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
          doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(SoleTrader), Some(CaptureBusinessEntityController.show().url))
          doc.sectionTest(NinoId, msg.nino, testNino, Some(soletraderRoutes.CaptureNinoController.show().url))
          doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
          doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
          doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
        }
        "the SkipCidCheck feature switch is not enabled" should {
          lazy val page = testView(businessEntity = SoleTrader, optNino = Some(testNino), skipCidCheck = false)
          lazy val doc = Jsoup.parse(page.body)

          val testPage = TestView(
            name = "Final check your answers - Sole trader",
            title = msg.title,
            heading = msg.heading,
            page = page
          )

          testPage.shouldHaveAcceptAndSendButton()

          "have rows for VRN, NINO, agent email, client email and contact preference" in {
            val page = testView(businessEntity = SoleTrader, optNino = Some(testNino), skipCidCheck = false)
            val doc = Jsoup.parse(page.body)

            doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
            doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(SoleTrader), Some(CaptureBusinessEntityController.show().url))
            doc.sectionTest(NinoId, msg.nino, testNino, Some(CaptureClientDetailsController.show().url))
            doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
            doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
            doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
          }
        }
      }
    }
    "The business is a Limited company or overseas trader with UK establishment" should {
      lazy val page = testView(businessEntity = LimitedCompany, optCompanyNumber = Some(testCompanyNumber), optCompanyName = Some(testCompanyName))
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Limited company",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, CRN, Company name, agent email, client email and contact preference" in {
        val page = testView(businessEntity = LimitedCompany, optCompanyNumber = Some(testCompanyNumber), optCompanyName = Some(testCompanyName))
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(LimitedCompany), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(CrnId, msg.companyNumber, testCompanyNumber, Some(CaptureCompanyNumberController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show().url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show().url))
      }
    }
    "The business is a General Partnership" should {
      lazy val page = testView(businessEntity = GeneralPartnership, optPartnershipUtr = Some(testCompanyUtr))
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - General partnership",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, Partnership UTR, agent email, client email and contact preference" in {
        val page = testView(businessEntity = GeneralPartnership, optPartnershipUtr = Some(testCompanyUtr))
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(GeneralPartnership), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(PartnershipUtrId, msg.partnershipUtr, testCompanyUtr, Some(partnershipRoutes.CapturePartnershipUtrController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a Limited Partnership" should {
      lazy val page = testView(businessEntity = LimitedPartnership, optPartnershipUtr = Some(testCompanyUtr))
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - General partnership",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, CRN, Company name, Partnership UTR, agent email, client email and contact preference" in {
        val page = testView(
          businessEntity = LimitedPartnership,
          optCompanyNumber = Some(testCompanyNumber),
          optCompanyName = Some(testCompanyName),
          optPartnershipUtr = Some(testCompanyUtr)
        )
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(LimitedPartnership), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(CrnId, msg.partnershipCompanyNumber, testCompanyNumber, Some(partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url))
        doc.sectionTest(CompanyNameId, msg.partnershipCompanyName, testCompanyName, None)
        doc.sectionTest(PartnershipUtrId, msg.partnershipUtr, testCompanyUtr, Some(partnershipRoutes.CapturePartnershipUtrController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a Scottish Partnership" should {
      lazy val page = testView(
        businessEntity = ScottishLimitedPartnership,
        optCompanyNumber = Some(testCompanyNumber),
        optCompanyName = Some(testCompanyName),
        optPartnershipUtr = Some(testCompanyUtr)
      )
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Scottish partnership",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, CRN, Company name, Partnership UTR, agent email, client email and contact preference" in {
        val page = testView(
          businessEntity = ScottishLimitedPartnership,
          optCompanyNumber = Some(testCompanyNumber),
          optCompanyName = Some(testCompanyName),
          optPartnershipUtr = Some(testCompanyUtr)
        )
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(ScottishLimitedPartnership), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(CrnId, msg.partnershipCompanyNumber, testCompanyNumber, Some(partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url))
        doc.sectionTest(CompanyNameId, msg.partnershipCompanyName, testCompanyName, None)
        doc.sectionTest(PartnershipUtrId, msg.partnershipUtr, testCompanyUtr, Some(partnershipRoutes.CapturePartnershipUtrController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a Limited Liability Partnership" should {
      lazy val page = testView(
        businessEntity = LimitedLiabilityPartnership,
        optCompanyNumber = Some(testCompanyNumber),
        optCompanyName = Some(testCompanyName),
        optPartnershipUtr = Some(testCompanyUtr)
      )
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Limited liability partnership",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, CRN, Company name, Partnership UTR, agent email, client email and contact preference" in {
        val page = testView(
          businessEntity = LimitedLiabilityPartnership,
          optCompanyNumber = Some(testCompanyNumber),
          optCompanyName = Some(testCompanyName),
          optPartnershipUtr = Some(testCompanyUtr)
        )
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(LimitedLiabilityPartnership), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(CrnId, msg.partnershipCompanyNumber, testCompanyNumber, Some(partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url))
        doc.sectionTest(CompanyNameId, msg.partnershipCompanyName, testCompanyName, None)
        doc.sectionTest(PartnershipUtrId, msg.partnershipUtr, testCompanyUtr, Some(partnershipRoutes.CapturePartnershipUtrController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a VAT group" should {
      lazy val page = testView(businessEntity = VatGroup)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - VAT group",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = VatGroup)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(VatGroup), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a Charity" should {
      lazy val page = testView(businessEntity = Charity)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Charity",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = Charity)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(Charity), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a Division" should {
      lazy val page = testView(businessEntity = Division)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Division",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = Division)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(Division), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is an Unincorporated Association" should {
      lazy val page = testView(businessEntity = UnincorporatedAssociation)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Unincorporated Association",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = UnincorporatedAssociation)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(UnincorporatedAssociation), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is an Trust" should {
      lazy val page = testView(businessEntity = Trust)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Trust",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = Trust)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(Trust), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is a Registered Society" should {
      lazy val page = testView(businessEntity = RegisteredSociety, optCompanyNumber = Some(testCompanyNumber), optCompanyName = Some(testCompanyName))
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Registered Society",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, CRN, Company name, agent email, client email and contact preference" in {
        val page = testView(businessEntity = RegisteredSociety, optCompanyNumber = Some(testCompanyNumber), optCompanyName = Some(testCompanyName))
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(RegisteredSociety), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(CrnId, msg.companyNumber, testCompanyNumber, Some(CaptureRegisteredSocietyCompanyNumberController.show().url))
        doc.sectionTest(CompanyNameId, msg.registeredSocietyCompanyName, testCompanyName, None)
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show().url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show().url))
      }
    }
    "The business is an Government Organisation" should {
      lazy val page = testView(businessEntity = GovernmentOrganisation)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Government Organisation",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = GovernmentOrganisation)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(BusinessEntityId, msg.businessEntity, getBusinessEntityName(GovernmentOrganisation), Some(CaptureBusinessEntityController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show.url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show.url))
      }
    }
    "The business is overseas without a UK establishment" should {
      lazy val page = testView(businessEntity = Overseas)
      lazy val doc = Jsoup.parse(page.body)

      val testPage = TestView(
        name = "Final check your answers - Overseas without a UK establishment",
        title = msg.title,
        heading = msg.heading,
        page = page
      )

      testPage.shouldHaveAcceptAndSendButton()

      "NOT have business entity and have rows for VRN, agent email, client email and contact preference" in {
        val page = testView(businessEntity = Overseas)
        val doc = Jsoup.parse(page.body)

        doc.sectionTest(VatNumberId, msg.vrn, testVatNumber, Some(CaptureVatNumberController.show().url))
        doc.sectionTest(AgentEmailId, msg.agentEmail, testAgentEmail, Some(CaptureAgentEmailController.show().url))
        doc.sectionTest(ContactPreferenceId, msg.contactPreference, Digital.toString, Some(ContactPreferenceController.show().url))
        doc.sectionTest(ClientEmailId, msg.clientEmail, testEmail, Some(CaptureClientEmailController.show().url))
      }
    }
  }

}
