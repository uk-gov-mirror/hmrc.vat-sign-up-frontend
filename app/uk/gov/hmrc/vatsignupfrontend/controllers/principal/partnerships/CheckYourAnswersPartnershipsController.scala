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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.StorePartnershipInformationService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships

import scala.concurrent.Future

@Singleton
class CheckYourAnswersPartnershipsController @Inject()(val controllerComponents: ControllerComponents,
                                                       val storePartnershipInformationService: StorePartnershipInformationService)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(GeneralPartnershipJourney, LimitedPartnershipJourney)) {

  override protected def featureEnabled[T](func: => T): T =
    if (featureSwitches exists isEnabled) func
    else throw new NotFoundException(featureSwitchError)


  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optBusinessEntityType = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)

      (optBusinessEntityType, optPartnershipUtr, optPartnershipPostCode) match {
        case (Some(entityType), Some(partnershipUtr), Some(partnershipPostCode)) =>
          (entityType, optPartnershipCrn, optPartnershipType) match {
            case (GeneralPartnership, _, _) =>
              Future.successful(
                Ok(check_your_answers_partnerships(
                  entityType = entityType,
                  companyUtr = partnershipUtr,
                  companyNumber = None,
                  postCode = partnershipPostCode,
                  postAction = routes.CheckYourAnswersPartnershipsController.submit()))
              )
            case (_: LimitedPartnershipBase, Some(_), Some(_: LimitedPartnershipEntityType)) =>
              Future.successful(
                Ok(check_your_answers_partnerships(
                  entityType = entityType,
                  companyUtr = partnershipUtr,
                  companyNumber = optPartnershipCrn,
                  postCode = partnershipPostCode,
                  postAction = routes.CheckYourAnswersPartnershipsController.submit()))
              )
            case _ =>
              Future.successful(
                Redirect(routes.CapturePartnershipCompanyNumberController.show())
              )
          }
        case (None, _, _) =>
          Future.successful(
            Redirect(principalRoutes.CaptureBusinessEntityController.show())
          )
        case (_, None, _) =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
        case (_, _, None) =>
          Future.successful(
            Redirect(routes.PrincipalPlacePostCodeController.show())
          )
      }
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)

      (optVatNumber, optPartnershipUtr, optPartnershipType, optPartnershipCrn, optPartnershipPostCode) match {
        case (Some(vatNumber), Some(partnershipUtr), Some(partnershipType: LimitedPartnershipEntityType), Some(companyNumber), Some(partnershipPostCode)) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = partnershipUtr,
            companyNumber = companyNumber,
            partnershipEntity = partnershipType,
            postCode = Some(partnershipPostCode)
          ) map {
            case Right(StorePartnershipInformationSuccess) =>
              Redirect(principalRoutes.DirectDebitResolverController.show())
            case Left(PartnershipUtrNotFound) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(StorePartnershipKnownFactsFailure) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(StorePartnershipInformationFailureResponse(status)) =>
              throw new InternalServerException("Store Partnership failed with status code: " + status)
          }
        case (Some(vatNumber), Some(partnershipUtr), None, None, Some(partnershipPostCode)) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = partnershipUtr,
            postCode = Some(partnershipPostCode)
          ) map {
            case Right(StorePartnershipInformationSuccess) =>
              Redirect(principalRoutes.DirectDebitResolverController.show())
            case Left(StorePartnershipKnownFactsFailure) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(PartnershipUtrNotFound) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(StorePartnershipInformationFailureResponse(status)) =>
              throw new InternalServerException("Store Partnership failed with status code: " + status)
          }
        case (None, _, _, _, _) =>
          Future.successful(
            Redirect(principalRoutes.ResolveVatNumberController.resolve())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
      }
    }
  }

}
