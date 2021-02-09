/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.config.VatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.EmailPasscodeForm
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.{EmailVerificationService, StoreEmailAddressService}
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_email_passcode
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureEmailPasscodeController @Inject()(storeEmailAddressService: StoreEmailAddressService,
                                               emailVerificationService: EmailVerificationService,
                                               view: capture_email_passcode)
                                              (implicit ec: ExecutionContext, vcc: VatControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate)(ec, vcc) {

  def show(): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.emailKey) match {
        case Some(email) =>
          Future.successful(Ok(view(
            passcodeForm = EmailPasscodeForm(),
            email = email,
            postAction = principalRoutes.CaptureEmailPasscodeController.submit()
          )))
        case _ =>
          Future.failed(throw new InternalServerException("[CaptureEmailPasscodeController][show] Couldn't retrieve email from session"))
      }
    }
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      (request.session.get(SessionKeys.vatNumberKey), request.session.get(SessionKeys.emailKey)) match {
        case (Some(vatNumber), Some(transactionEmail)) =>
          EmailPasscodeForm().bindFromRequest() fold(
            formWithErrors => {
              Future.successful(BadRequest(view(
                passcodeForm = formWithErrors,
                email = transactionEmail,
                postAction = principalRoutes.CaptureEmailPasscodeController.submit()
              )))
            },
            passcode =>
              emailVerificationService.verifyEmailPasscode(transactionEmail, passcode) flatMap {
                case PasscodeMismatch =>
                  val incorrectPasscodeForm = EmailPasscodeForm().fill(passcode).withError(
                    key = EmailPasscodeForm.code,
                    message = messagesApi.preferred(request)("capture-email-passcode.error.incorrect_passcode")
                  )
                  Future.successful(BadRequest(view(
                    passcodeForm = incorrectPasscodeForm,
                    email = transactionEmail,
                    postAction = principalRoutes.CaptureEmailPasscodeController.submit()
                  )))
                case PasscodeNotFound =>
                  Future.successful(Redirect(errorRoutes.PasscodeNotFoundController.show()))
                case MaxAttemptsExceeded =>
                  Future.successful(Redirect(errorRoutes.MaxEmailPasscodeAttemptsExceededController.show()))
                case EmailAlreadyVerified | EmailVerifiedSuccessfully =>
                  storeEmailAddressService.storeTransactionEmailVerified(vatNumber, transactionEmail) map {
                    case StoreEmailVerifiedSuccess =>
                      Redirect(routes.EmailVerifiedController.show())
                    case StoreEmailVerifiedFailed(status) =>
                      throw new InternalServerException(s"[CaptureEmailPasscodeController][Submit] Failed to store email with status $status")
                  }
              }

          )
        case (optVatNumber, optEmail) =>
          val missing = Seq(optVatNumber, optEmail).flatten.mkString(", ")
          throw new InternalServerException(s"[CaptureEmailPasscodeController][submit] Failed to retrieve: $missing from session")
      }
    }
  }

}