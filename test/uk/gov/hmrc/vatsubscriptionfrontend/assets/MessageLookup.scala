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
    val tryAgain = "Try again"
  }

  object ErrorMessage {
    val invalidVatNumber = "Please enter a valid vat registration number"
    val invalidCompanyNumber = "Please enter a valid company number"
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

  object CannotUseServiceYet {
    val title = "Your client can't use this service yet"
    val heading: String = title
    val line1 = "This service is currently only available to certain sole traders and limited companies."
    val line2 = "Your client must come back and sign up in April 2019, when this service is available to them."
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
    val title = "What is your client's company number?"
    val heading: String = title
  }

  object CaptureBusinessEntity {
    val title = "What type of business is your client registered as?"
    val heading: String = title
    val radioSoleTrader: String = "Sole trader"
    val radioLimitedCompany: String = "Limited company"
    val radioOther: String = "Other"
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
    val title = "Confirm your client's company number"
    val heading: String = title
    val companyNumberHeading = "Company number"
    val link = "Change company number"
  }

  object AgreeCaptureEmail {
    val title = "Agree to get emails instead of letters"
    val heading: String = title
    val line1 = "When your client has a new message about VAT in their HMRC account, we'll send an email to let them know."
    val line2 = "They'll need to sign in to their account account to read the message."
  }

  object CaptureEmail {
    val title = "What is your client's email address?"
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

  object ClientVerifiedEmail {
    val title = "You've verified your email address"
    val heading: String = title
    val line1 = "HM Revenue and Customs will contact you by email instead of sending letters."
    val link = "Return to GOV.UK"
  }


  object ConfirmClient {
    val title = "Confirm your client's details"
    val heading = "Confirm your client's details"
    val subHeading = "You've told us"
    val firstName = "First name"
    val lastName = "Last name"
    val nino = "National Insurance number"
    val dob = "Date of birth"
  }

  object FailedClientMatching {
    val title = "There's a problem"
    val heading: String = title
    val description = "The details you've entered are not on our system."
  }


  object Terms {
    val title = "Terms of participation"
    val heading: String = title
    val line1 = "By taking part in this trial, you agree that either you or your client will:"
    val bullet1 = "use relevant software to record your client's sales and purchases, then to submit their VAT Returns"
    val bullet2 = "submit each VAT Return within one calendar month and 7 days from the end of their accounting period"
    val bullet3 = "tell HMRC if your client stops trading and then submit their final VAT Return"
    val bullet4 = "tell HMRC if your client wants to leave this trial"
    val line2 = "These terms are not contractual and your client can stop taking part in this trial at any time."
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

  object SignInWithDifferentDetails {
    val title = "You need to sign in with different details"
    val heading: String = title
    val line1 = "You need to sign in with the Government Gateway details for the business you want to sign up."
  }

  object PrincipalGuidance {
    val title = "Use software to submit your VAT Returns"
    val heading: String = title
    val line1 = "HM Revenue and Customs (HMRC) are changing the way you submit your VAT Returns."
    val line2 = "From April 2019, VAT registered businesses with a turnover above £85,000 must use relevant third party software to submit their VAT Returns."
    val line3 = "If you're a sole trader or a director of a limited company you can try this new way of reporting now, instead of waiting until April 2019."
    val line4 = "You'll need to sign up for this new service, even if you already use software to submit your VAT Returns."

    object Section1 {
      val heading = "How it works"
      val line1 = "To sign up for this service, you need to use software to record your purchases and sales. If you don't already record in this way, submit your next VAT Return on GOV.UK or by post before you sign up. This will give you more time to complete your first return using software."
      val number1 = "You need to choose relevant third party software (opens in a new window). If you already use software, check with your supplier to see if you can use it with this service."
      val number2 = "Sign up for this service and agree to get emails instead of letters."
      val number3 = "Allow your software to submit VAT Returns to HMRC. You might need to sign in with your Government Gateway details."
      val number4 = "Use your software to record your purchases and sales, then to submit your VAT Returns. We'll email you to let you know when to send a return."
      val line2 = "You can also choose to:"
      val bullet1 = "pay throughout the year, if it helps you manage your income"
      val bullet2 = "get your accountant to send your VAT Returns"
      val bullet3 = "view your VAT Return deadlines in your business tax account"
    }

    object Section2 {
      val heading = "Sign up"
      val line1 = "To sign up you'll need the Government Gateway details you got when you registered for VAT."
      val line2 = "If you haven't submitted a VAT Return online before, then you'll also need your VAT registration certificate."
      val link = "Sign up to use software to submit your VAT Returns."
    }
  }

  object PrincipalCaptureBusinessEntity {
    val title = "What type of business are you registered as?"
    val heading: String = title
    val radioSoleTrader: String = "Sole trader"
    val radioLimitedCompany: String = "Limited company"
    val radioOther: String = "Other"
  }

  object PrincipalCannotUseServiceYet {
    val title = "You can't use this service yet"
    val heading: String = title
    val line1 = "This service is only available to some limited companies and sole traders."
    val line2 = "You'll be able to sign up for this service by April 2019."
  }

  object PrincipalCaptureEmail {
    val title = "What is your email address?"
    val heading: String = title
    val hint = "For example, me@me.com"
  }

  object PrincipalAgreeCaptureEmail {
    val title = "Agree to get emails instead of letters"
    val heading: String = title
    val line1 = "When you have a new message about VAT in your HMRC account, we'll send you an email to let you know."
    val line2 = "You'll need to sign in to your account to read the message."
  }

  object PrincipalConfirmEmail {
    val title = "Check your email address"
    val heading: String = title
    val emailHeading = "Email address"
    val link = "Change email address"
  }

  object PrincipalVerifyEmail {
    val title = "Verify your email address"
    val heading: String = title

    def line1(email: String) = s"We've sent an email to $email. Click on the link in the email to verify your email address."
  }

  object PrincipalEmailVerified {
    val title = "You've verified your email address"
    val heading: String = title
    val line1 = "You'll now receive messages and email notifications from HMRC."
  }

  object YourDetails {
    val title = "Enter your details"
    val heading: String = title
    val line1 = "We will attempt to match these details against information we currently hold."
    val field1 = "First name"
    val field2 = "Last name"
    val field3 = "National Insurance number"
    val field4 = "Date of birth"
    val formhint1_line1 = "For example, 'QQ 12 34 56 C'."
    val formhint2 = "For example, 10 12 1990"
  }

  object ConfirmDetails {
    val title = "Confirm your details"
    val heading = "Confirm your details"
    val subHeading = "You've told us"
    val firstName = "First name"
    val lastName = "Last name"
    val nino = "National Insurance number"
    val dob = "Date of birth"
  }

  object FailedIdentityVerification {
    val title = "We couldn't confirm your details"
    val heading: String = title
    val line1 = "The information you provided doesn't match the details on our system."
    val tryAgain = "Try again"
  }

  object IdentityVerificationSuccess {
    val title = "We've confirmed your identity"
    val heading: String = title
    val line1 = "You can now sign up for this service."
  }

  object FailedMatching {
    val title = "We couldn't confirm your details"
    val heading: String = title
    val line1 = "The information you provided doesn't match the details on our system."
    val tryAgain = "Try again"
  }

  object PrincipalCaptureCompanyNumber {
    val title = "What is your company number?"
    val heading: String = title
    val line1 = "You received this from Companies House when you set up your company. It's 8 digits and sometimes starts with 2 letters."
  }

  object PrincipalConfirmCompanyNumber {
    val title = "Confirm your company number"
    val heading: String = title
    val companyNumberHeading = "Company number"
    val link = "Change company number"
  }

  object PrincipalTerms {
    val title = "Terms of participation"
    val heading: String = title
    val line1 = "By taking part in this trial, you agree to:"
    val bullet1 = "use relevant software to record your sales and purchases, then to submit your VAT returns"
    val bullet2 = "submit each VAT Return within one calendar month and 7 days from the end of your accounting period"
    val bullet3 = "authorise any third party you use (such as your accountant) and have responsibility for any information they give to HMRC on your behalf"
    val bullet4 = "tell HMRC if you stop trading and then submit your final VAT return"
    val bullet5 = "tell HMRC if you want to leave this trial"
    val line2 = "These terms are not contractual and you can stop taking part in the trial at any time."
  }

  object PrincipalInformationReceived {
    val title = "We've received your information"
    val heading: String = title

    object Section1 {
      val heading = "What happens next"
      val line1 = "We'll let you know whether you can use software to submit your VAT Returns, usually within 24 hours."
    }

    object Section2 {
      val heading = "After your application is approved"
      val bullet1 = "Choose accounting software that supports this service if you haven't already."
      val bullet2 = "Sign in to the software with your Government Gateway details and authorise it to interact with HMRC."
      val bullet3 = "Add any sales and purchases that you've already received or paid out."
      val bullet4 = "Record your future sales and purchases using the software."
      val bullet5 = "Submit your VAT Returns before your deadlines."
      val line1 = "You can view your VAT Return deadlines in your accounting software or business tax account."
    }

  }

}
