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

import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalCaptureBusinessEntity => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class CaptureBusinessEntitySpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_business_entity(
    businessEntityForm(isAgent = false),
    postAction = testCall,
    generalPartnershipEnabled = true,
    limitedPartnershipEnabled = true,
    vatGroupEnabled = true,
    divisionEnabled = true,
    trustEnabled = true,
    unincorporatedAssociationEnabled = true,
    registeredSocietyEnabled = true
  )(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Principal Business Entity view" should {

    val testPage = TestView(
      name = "Principal Capture Business Entity View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveForm("Business Entity Form")(actionCall = testCall)

    soleTrader -> messages.radioSoleTrader
    limitedCompany -> messages.radioLimitedCompany
    generalPartnership -> messages.radioGeneralPartnership
    other -> messages.radioOther

    "have a set of radio inputs" which {
      lazy val doc = Jsoup.parse(page.body)

      "for the option 'Sole Trader'" should {

        "have the text 'Sole trader'" in {
          doc.select("label[for=sole-trader]").text() shouldEqual  messages.radioSoleTrader
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#sole-trader")

          "have the id 'sole-trader'" in {
            optionLabel.attr("id") shouldEqual "sole-trader"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'Limited company'" should {

        "have the text 'Limited company'" in {
          doc.select("label[for=limited-company]").text() shouldEqual messages.radioLimitedCompany
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#limited-company")

          "have the id 'limited-company'" in {
            optionLabel.attr("id") shouldEqual "limited-company"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'General partnership'" should {

        "have the text 'General partnership'" in {
          doc.select("label[for=general-partnership]").text() shouldEqual  messages.radioGeneralPartnership
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#general-partnership")

          "have the id 'limited-company'" in {
            optionLabel.attr("id") shouldEqual "general-partnership"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'Limited partnership'" should {

        "have the text 'Limited partnership'" in {
          doc.select("label[for=limited-partnership]").text() shouldEqual messages.radioLimitedPartnership
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#limited-partnership")

          "have the id 'limited-company'" in {
            optionLabel.attr("id") shouldEqual "limited-partnership"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
      "for the option 'VAT group'" should {

        "have the text 'VAT group'" in {
          doc.select("label[for=vat-group]").text() shouldEqual messages.radioVatGroup
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#vat-group")

          "have the id 'vat-group'" in {
            optionLabel.attr("id") shouldEqual "vat-group"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
      "for the option 'Division'" should {

        "have the text 'Administrative division'" in {
          doc.select("label[for=division]").text() shouldEqual messages.radioDivision
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#division")

          "have the id 'division'" in {
            optionLabel.attr("id") shouldEqual "division"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
      "for the option 'Unincorporated Association'" should {

        "have the text 'Unincorporated Association'" in {
          doc.select("label[for=unincorporated-association]").text() shouldEqual messages.radioUnincorporatedAssociation
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#unincorporated-association")

          "have the id 'unincorporated-association'" in {
            optionLabel.attr("id") shouldEqual "unincorporated-association"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'Trust'" should {

        "have the text 'Trust'" in {
          doc.select("label[for=trust]").text() shouldEqual messages.radioTrust
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#trust")

          "have the id 'trust'" in {
            optionLabel.attr("id") shouldEqual "trust"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'Registered Society'" should {

        "have the text 'Registered society (including community benefit societies and co-operative societies)'" in {
          doc.select("label[for=registered-society]").text() shouldEqual messages.radioRegisteredSociety
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#registered-society")

          "have the id 'registered-society'" in {
            optionLabel.attr("id") shouldEqual "registered-society"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'Other'" should {

        "have the text 'Other'" in {
          doc.select("label[for=other]").text() shouldEqual messages.radioOther
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#other")

          "have the id 'other'" in {
            optionLabel.attr("id") shouldEqual "other"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }


    testPage.shouldHaveContinueButton()
  }
}
