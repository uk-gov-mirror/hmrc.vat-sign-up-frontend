/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.BTAClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessPostCodeForm._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.ClaimSubscriptionHttpParser.{AlreadyEnrolledOnDifferentCredential, KnownFactsMismatch, SubscriptionClaimed}
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, PostCode}
import uk.gov.hmrc.vatsignupfrontend.services.ClaimSubscriptionService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.principal_place_of_business

import scala.concurrent.Future

@Singleton
class BtaBusinessPostCodeController @Inject()(val controllerComponents: ControllerComponents,
                                              claimSubscriptionService: ClaimSubscriptionService)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(BTAClaimSubscription)) {

  def show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(
          Ok(principal_place_of_business(businessPostCodeForm.form, routes.BtaBusinessPostCodeController.submit()))
        )
      }
  }

  private def claimSubscription(vatNumber: String,
                                optPostCode: Option[PostCode],
                                vatRegistrationDate: DateModel)(implicit hc: HeaderCarrier): Future[Result] =
    claimSubscriptionService.claimSubscription(vatNumber, optPostCode, vatRegistrationDate, isFromBta = true) map {
      case Right(SubscriptionClaimed) => Redirect(principalRoutes.SignUpCompleteClientController.show())
      case Left(KnownFactsMismatch) => Redirect(errorRoutes.CouldNotConfirmBusinessController.show())
      case Left(AlreadyEnrolledOnDifferentCredential) => Redirect(errorRoutes.BusinessAlreadySignedUpController.show())
      case err@_ => throw new InternalServerException("unexpected response on store vat number " + err)
    }

  def submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
        val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)

        (optVatNumber, optVatRegistrationDate) match {
          case (Some(vatNumber), Some(vatRegistrationDate)) =>
            businessPostCodeForm.bindFromRequest.fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(principal_place_of_business(formWithErrors, routes.BtaBusinessPostCodeController.submit()))
                ),
              businessPostCode =>
                claimSubscription(vatNumber, Some(businessPostCode), vatRegistrationDate)
            )
          case (None, _) => Future.failed(new InternalServerException("Entered BTA claim subscription flow without VRN"))
          case (_, None) => Future.successful(Redirect(routes.CaptureBtaVatRegistrationDateController.show().url))

        }
      }
  }

}
