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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, RequestHeader, Result}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.AdditionalKnownFacts
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._
import uk.gov.hmrc.vatsignupfrontend.services.{StoreMigratedVatNumberService, StoreVatNumberService}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.check_your_answers

import scala.concurrent.Future

@Singleton
class CheckYourAnswersController @Inject()(val controllerComponents: ControllerComponents,
                                           val storeVatNumberService: StoreVatNumberService,
                                           val storeMigratedVatNumberService: StoreMigratedVatNumberService
                                          ) extends AuthenticatedController(AdministratorRolePredicate) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)
      val optBusinessPostCode = request.session.getModel[PostCode](SessionKeys.businessPostCodeKey)
      val optBox5Figure = request.session.get(SessionKeys.box5FigureKey).filter(_.nonEmpty)
      val optLastReturnMonth = request.session.get(SessionKeys.lastReturnMonthPeriodKey).filter(_.nonEmpty)
      val optPreviousVatReturn = request.session.get(SessionKeys.previousVatReturnKey).filter(_.nonEmpty)
      val optBusinessEntity = request.session.get(SessionKeys.businessEntityKey).filter(_.nonEmpty)
      val isMigrated = request.session.get(SessionKeys.isMigratedKey).contains("true")
      val isOverseas = request.session.get(SessionKeys.businessEntityKey).contains(Overseas.toString)

      (optVatNumber, optVatRegistrationDate, optBusinessPostCode, optPreviousVatReturn, optBox5Figure, optLastReturnMonth) match {
        case (Some(vatNumber), Some(vatRegistrationDate), _, _, _, _) if isMigrated && isOverseas =>
          Future.successful(
            Ok(check_your_answers(
              vatNumber = vatNumber,
              registrationDate = vatRegistrationDate,
              optPostCode = None,
              optPreviousVatReturn = None,
              optBox5Figure = None,
              optLastReturnMonthPeriod = None,
              postAction = routes.CheckYourAnswersController.submit()))
          )
        case (Some(vatNumber), Some(vatRegistrationDate), Some(businessPostCode), _, _, _) if isMigrated =>
          Future.successful(
            Ok(check_your_answers(
              vatNumber = vatNumber,
              registrationDate = vatRegistrationDate,
              optPostCode = Some(businessPostCode),
              optPreviousVatReturn = None,
              optBox5Figure = None,
              optLastReturnMonthPeriod = None,
              postAction = routes.CheckYourAnswersController.submit()))
          )
        case (None, _, _, _, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case (_, None, _, _, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatRegistrationDateController.show())
          )
        case (_, _, None, _, _, _) if optBusinessEntity.isEmpty =>
          Future.successful(
            Redirect(routes.BusinessPostCodeController.show())
          )
        case (_, _, _, None, _, _) if isEnabled(AdditionalKnownFacts) =>
          Future.successful(
            Redirect(routes.PreviousVatReturnController.show())
          )
        case (_, _, _, _, None, _) if isEnabled(AdditionalKnownFacts) && (optPreviousVatReturn contains Yes.stringValue) =>
          Future.successful(
            Redirect(routes.CaptureBox5FigureController.show())
          )
        case (_, _, _, _, _, None) if isEnabled(AdditionalKnownFacts) && (optPreviousVatReturn contains Yes.stringValue) =>
          Future.successful(
            Redirect(routes.CaptureLastReturnMonthPeriodController.show())
          )
        case (Some(vatNumber), Some(vatRegistrationDate), _, _, _, _) =>
          Future.successful(
            Ok(check_your_answers(
              vatNumber = vatNumber,
              registrationDate = vatRegistrationDate,
              optPostCode = if (optBusinessEntity contains Overseas.toString) None else optBusinessPostCode,
              optPreviousVatReturn = optPreviousVatReturn,
              optBox5Figure = optBox5Figure,
              optLastReturnMonthPeriod = optLastReturnMonth,
              postAction = routes.CheckYourAnswersController.submit()))
          )
      }
    }
  }

  private def storeVatNumber(vatNumber: String,
                             optPostCode: Option[PostCode],
                             vatRegistrationDate: DateModel,
                             optBox5Figure: Option[String],
                             optLastReturnMonth: Option[String],
                             isFromBta: Boolean
                            )(implicit hc: HeaderCarrier, request: RequestHeader): Future[Result] =
    storeVatNumberService.storeVatNumber(
      vatNumber = vatNumber,
      optPostCode = optPostCode,
      registrationDate = vatRegistrationDate,
      optBox5Figure = optBox5Figure,
      optLastReturnMonth = optLastReturnMonth,
      isFromBta = isFromBta
    ) map {
      case Right(VatNumberStored(isOverseas, isDirectDebit)) if isOverseas =>
        Redirect(routes.CaptureBusinessEntityController.show())
          .addingToSession(SessionKeys.hasDirectDebitKey, isDirectDebit)
          .addingToSession(SessionKeys.businessEntityKey, Overseas.asInstanceOf[BusinessEntity])
      case Right(VatNumberStored(_, isDirectDebit)) =>
        Redirect(routes.CaptureBusinessEntityController.show())
          .addingToSession(SessionKeys.hasDirectDebitKey, isDirectDebit)
      case Right(SubscriptionClaimed) =>
        Redirect(routes.SignUpCompleteClientController.show())
      case Left(KnownFactsMismatch) =>
        Redirect(routes.VatCouldNotConfirmBusinessController.show())
      case Left(InvalidVatNumber) =>
        Redirect(routes.InvalidVatNumberController.show())
      case Left(IneligibleVatNumber(migratableDates)) =>
        Redirect(routes.CannotUseServiceController.show())
      case Left(VatNumberAlreadyEnrolled) =>
        Redirect(bta.routes.BusinessAlreadySignedUpController.show())
      case Left(VatMigrationInProgress) =>
        Redirect(routes.MigrationInProgressErrorController.show())
      case err =>
        throw new InternalServerException("unexpected response on store vat number " + err)
    }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)
      val optBusinessPostCode = request.session.getModel[PostCode](SessionKeys.businessPostCodeKey)
      val optBox5Figure = request.session.get(SessionKeys.box5FigureKey).filter(_.nonEmpty)
      val optlastReturnMonth = request.session.get(SessionKeys.lastReturnMonthPeriodKey).filter(_.nonEmpty)
      val optPreviousVatReturn = request.session.get(SessionKeys.previousVatReturnKey).filter(_.nonEmpty)
      val optLastReturnMonth = request.session.get(SessionKeys.lastReturnMonthPeriodKey).filter(_.nonEmpty)
      val optBusinessEntity = request.session.get(SessionKeys.businessEntityKey).filter(_.nonEmpty)
      val isMigrated = request.session.get(SessionKeys.isMigratedKey).contains("true")
      val isOverseas = request.session.get(SessionKeys.businessEntityKey).contains(Overseas.toString)

      (optVatNumber, optVatRegistrationDate, optBusinessPostCode, optPreviousVatReturn, optBox5Figure, optlastReturnMonth) match {
        case (None, _, _, _, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case (_, None, _, _, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatRegistrationDateController.show())
          )
        case (_, _, None, _, _, _) if optBusinessEntity.isEmpty =>
          Future.successful(
            Redirect(routes.BusinessPostCodeController.show())
          )
        case (Some(vatNumber), Some(vatRegistrationDate), optBusinessPostcode, _, _, _) if isMigrated =>
          storeMigratedVatNumberService.storeVatNumber(vatNumber, Some(vatRegistrationDate.toDesDateFormat), optBusinessPostcode).map {
            case Right(StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberSuccess) =>
              Redirect(routes.CaptureBusinessEntityController.show())
            case Left(StoreMigratedVatNumberHttpParser.KnownFactsMismatch) =>
              throw new InternalServerException(s"[CheckYourAnswersController][storeMigratedUnenrolledVatNumber] Failed to store vat number for unenrolled known facts mismatch")
          }
        case (_, _, _, None, _, _) if isEnabled(AdditionalKnownFacts) =>
          Future.successful(
            Redirect(routes.PreviousVatReturnController.show())
          )
        case (_, _, _, _, None, _) if isEnabled(AdditionalKnownFacts) && (optPreviousVatReturn contains Yes.stringValue) =>
          Future.successful(
            Redirect(routes.CaptureBox5FigureController.show())
          )
        case (_, _, _, _, _, None) if isEnabled(AdditionalKnownFacts) && (optPreviousVatReturn contains Yes.stringValue) =>
          Future.successful(
            Redirect(routes.CaptureLastReturnMonthPeriodController.show())
          )
        case (Some(vatNumber), Some(vatRegistrationDate), _, _, _, _) =>
          storeVatNumber(
            vatNumber = vatNumber,
            optPostCode = if (optBusinessEntity contains Overseas.toString) None else optBusinessPostCode,
            vatRegistrationDate = vatRegistrationDate,
            optBox5Figure = optBox5Figure,
            optLastReturnMonth = optLastReturnMonth,
            isFromBta = false
          )
      }
    }
  }

}
