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
    generalPartnershipEnabled = true
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
          messages.radioSoleTrader shouldEqual "Sole trader"
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
          messages.radioLimitedCompany shouldEqual "Limited company"
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
          messages.radioGeneralPartnership shouldEqual "General partnership"
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

      "for the option 'Other'" should {

        "have the text 'Other'" in {
          messages.radioOther shouldEqual "Other"
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

