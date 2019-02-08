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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.check_your_answers

import scala.concurrent.Future

@Singleton
class CheckYourAnswersController @Inject()(val controllerComponents: ControllerComponents,
                                           val storeVatNumberService: StoreVatNumberService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)
      val optBusinessPostCode = request.session.getModel[PostCode](SessionKeys.businessPostCodeKey)

      (optVatNumber, optVatRegistrationDate, optBusinessPostCode) match {
        case (Some(vat_number), Some(vatRegistrationDate), Some(postCode)) =>
          Future.successful(
            Ok(check_your_answers(
              vat_number,
              vatRegistrationDate,
              postCode,
              routes.CheckYourAnswersController.submit()))
          )
        case (None, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case (_, None, _) =>
          Future.successful(
            Redirect(routes.CaptureVatRegistrationDateController.show())
          )
        case (_, _, None) =>
          Future.successful(
            Redirect(routes.BusinessPostCodeController.show())
          )
      }
    }
  }

  private def storeVatNumber(vatNumber: String,
                             postCode: PostCode,
                             vatRegistrationDate: DateModel,
                             isFromBta: Boolean
                            )(implicit hc: HeaderCarrier) =
    storeVatNumberService.storeVatNumber(vatNumber, postCode, vatRegistrationDate, isFromBta) map {
      case Right(VatNumberStored(isOverseas)) if isOverseas => Redirect(routes.OverseasResolverController.resolve())
      case Right(VatNumberStored(_)) => Redirect(routes.CaptureBusinessEntityController.show())
      case Right(SubscriptionClaimed) => Redirect(routes.SignUpCompleteClientController.show())
      case Left(KnownFactsMismatch) => Redirect(routes.CouldNotConfirmBusinessController.show())
      case Left(InvalidVatNumber) => Redirect(routes.InvalidVatNumberController.show())
      case Left(IneligibleVatNumber(migratableDates)) => Redirect(routes.CannotUseServiceController.show())
      case Left(VatNumberAlreadyEnrolled) => Redirect(bta.routes.BusinessAlreadySignedUpController.show())
      case Left(VatMigrationInProgress) => Redirect(routes.MigrationInProgressErrorController.show())
      case err@_ => throw new InternalServerException("unexpected response on store vat number " + err)
    }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)
      val optBusinessPostCode = request.session.getModel[PostCode](SessionKeys.businessPostCodeKey)

      (optVatNumber, optVatRegistrationDate, optBusinessPostCode) match {
        case (Some(vatNumber), Some(vatRegistrationDate), Some(postCode)) =>
          storeVatNumber(vatNumber, postCode, vatRegistrationDate, isFromBta = false)
        case (None, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case (_, None, _) =>
          Future.successful(
            Redirect(routes.CaptureVatRegistrationDateController.show())
          )
        case (_, _, None) =>
          Future.successful(
            Redirect(routes.BusinessPostCodeController.show())
          )
      }
    }
  }

}
