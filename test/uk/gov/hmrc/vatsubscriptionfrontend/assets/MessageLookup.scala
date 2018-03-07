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

package uk.gov.hmrc.vatsubscriptionfrontend.assets

object MessageLookup {

  object Base {
    val continue = "Continue"
    val continueToSignUp = "Continue to sign up"
    val confirmAndContinue = "Confirm and continue"
    val acceptAndContinue = "Accept and continue"
    val agreeAndContinue = "Agree and continue"
    val submit = "Submit"
    val update = "Update"
    val signOut = "Sign out"
    val signUp = "Sign up"
    val goBack = "Go back"
    val signUpAnotherClient = "Sign up another client"
    val day = "Day"
    val month = "Month"
    val year = "Year"
    val change = "Change"
  }

  object ErrorMessage {
    val invalidVatNumber = "Please enter a valid vat registration number"
    val invalidCompanyNumber = "Please enter a valid company registration number"
  }

  object NotEnrolledToAS {
    val title = "You can't use this service yet"
    val heading: String = title
    val line1 = "To use this service, you need to set up an agent services account."
  }

  object NoAgentClientRelationship {
    val title = "You can't sign up this client yet"
    val heading: String = title
    val line1 = "To use this service, your client needs to authorise you as their agent."
  }

  object CaptureVatNumber {
    val title = "What is your client's VAT number?"
    val heading: String = title
    val description = "This is the 9-digit number they received when they registered for VAT."
    val hint = "For example, 123456789"
  }

  object ConfirmVatNumber {
    val title = "Confirm your client's VAT number"
    val heading: String = title
    val vatNumberHeading = "VAT number"
    val link = "Change VAT number"
  }

  object CaptureCompanyNumber {
    val title = "What is your client's company registration number?"
    val heading: String = title
  }

  object CaptureBusinessEntity {
    val title = "What type of business is your client registered as?"
    val heading: String = title
    val radioSoleTrader: String = "Sole trader"
    val radioLimitedCompany: String = "Limited company"
  }

  object ClientDetails {
    val title = "Enter your client's details"
    val heading = "Enter your client's details"
    val line1 = "We will attempt to match these details against information we currently hold."
    val field1 = "First name"
    val field2 = "Last name"
    val field3 = "National Insurance number"
    val field4 = "Date of birth"
    val formhint1_line1 = "For example, 'QQ 12 34 56 C'."
    val formhint2 = "For example, 10 12 1990"
  }

  object ConfirmCompanyNumber {
    val title = "Confirm your client's company registration number"
    val heading: String = title
    val companyNumberHeading = "Company registration number"
    val link = "Change company registration number"
  }

  object AgreeCaptureEmail {
    val title = "Agree to get emails instead of letters"
    val heading: String = title
    val line1 = "When your client has a new message about VAT in their HMRC account, we'll send an email to let them know."
    val line2 = "They'll need to sign in to their account account to read the message."
  }

  object CaptureEmail {
    val title = "What is your client’s email address?"
    val heading: String = title
    val hint = "For example, me@me.com"
  }

  object ConfirmEmail {
    val title = "Check your client's email address"
    val heading: String = title
    val emailHeading = "Email address"
    val link = "Change email address"
  }

  object VerifyEmail {
    val title = "We've sent your client an email"
    val heading: String = title

    def line1(email: String) = s"We've sent an email to $email. Your client needs to click on the link in the email to verify their email address."

    val line2 = "They need to verify their email address to get VAT emails from HMRC."
  }

  object ConfirmClient {
    val title = "Confirm your client"
    val heading = "Check your answers"
    val subHeading = "You've told us"
    val firstName = "First name"
    val lastName = "Last name"
    val nino = "National Insurance number"
    val dob = "Date of birth"
  }


  object Terms {
      val title = "Terms of participation"
      val heading: String = title
      val line1 = "By taking part in this trial, you agree that either you or your client will:"
      val bullet1 = "use accounting software that supports Making Tax Digital to record your client’s sales and purchases, then to submit their VAT Returns"
      val bullet2 = "submit each VAT Return within one calendar month and 7 days from the end of your accounting period"
      val bullet3 = "tell HMRC if your client stops trading and then submit their final VAT Return"
      val bullet4 = "tell HMRC if your client wants to leave this trial"
      val line2 = "These terms aren't contractual and your client can leave the trial at any time."
    }

    object Confirmation {
      val title = "We've received your client's information"
      val heading: String = title

      object Section1 {
        val heading = "What happens next"
        val line1 = "We'll contact your client within 24 hours to tell them if they can use software to submit their VAT Returns."
      }

      object Section2 {
        val heading = "When your client's information is approved"
        val line1 = "Either you or your client need to complete the steps below. It's important for your client to choose a software package that can interact with yours."
        val bullet1 = "Choose accounting software if you haven't already."
        val bullet2 = "Sign in to the software with your Government Gateway details and authorise it to interact with HMRC."
        val bullet3 = "Add any purchases and sales that your client has already received or paid out."
        val bullet4 = "Record your client's future purchases and sales using the software, then to submit their VAT Returns."
        val line2 = "Your client can view their VAT Return deadlines in their accounting software or business tax account."
      }

    }

    object YourVatNumber {
      val title = "Confirm you want to use this VAT number to sign up"
      val heading: String = title
      val vatNumberHeading = "VAT number"
      val link = "I want to use a different VAT number"
  }

  object YourCaptureEmail {
    val title = "What is your email address?"
    val heading: String = title
    val hint = "For example, me@me.com"
  }

  object PrincipalConfirmEmail {
    val title = "Check your email address"
    val heading: String = title
    val emailHeading = "Email address"
    val link = "Change email address"
  }


}