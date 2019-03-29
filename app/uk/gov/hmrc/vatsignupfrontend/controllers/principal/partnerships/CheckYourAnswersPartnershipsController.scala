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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreJointVentureInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.{StoreJointVentureInformationService, StorePartnershipInformationService}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships

import scala.concurrent.Future

@Singleton
class CheckYourAnswersPartnershipsController @Inject()(val controllerComponents: ControllerComponents,
                                                       val storePartnershipInformationService: StorePartnershipInformationService,
                                                       val storeJointVentureInformationService: StoreJointVentureInformationService)
  extends AuthenticatedController(
    retrievalPredicate = AdministratorRolePredicate){

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {

      val optBusinessEntityType = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)
      val optJointVentureProperty = request.session.get(SessionKeys.jointVentureOrPropertyKey).map(_.toBoolean)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)

      (optJointVentureProperty, optBusinessEntityType, optPartnershipCrn, optPartnershipUtr, optPartnershipPostCode) match {
        case (Some(true), Some(GeneralPartnership), _, _, _) =>
          Future.successful(Ok(check_your_answers_partnerships(
            entityType = GeneralPartnership,
            companyUtr = None,
            companyNumber = None,
            postCode = None,
            jointVentureProperty = optJointVentureProperty,
            postAction = routes.CheckYourAnswersPartnershipsController.submit()
          )))
        case (_, Some(GeneralPartnership), _, Some(_), Some(_)) =>
          Future.successful(Ok(check_your_answers_partnerships(
            entityType = GeneralPartnership,
            companyUtr = optPartnershipUtr,
            companyNumber = None,
            postCode = optPartnershipPostCode,
            jointVentureProperty = optJointVentureProperty,
            postAction = routes.CheckYourAnswersPartnershipsController.submit()
          )))
        case (_, Some(entity: LimitedPartnershipBase), Some(_), Some(_), Some(_)) =>
          Future.successful(Ok(check_your_answers_partnerships(
            entityType = entity,
            companyUtr = optPartnershipUtr,
            companyNumber = optPartnershipCrn,
            postCode = optPartnershipPostCode,
            jointVentureProperty = None,
            postAction = routes.CheckYourAnswersPartnershipsController.submit()
          )))
        case _ =>
          Future.successful(Redirect(principalRoutes.CaptureBusinessEntityController.show()))
      }
    }
  }


  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {

      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optBusinessEntityType = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)
      val optJointVentureProperty = request.session.getModel[Boolean](SessionKeys.jointVentureOrPropertyKey)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)

      (optVatNumber, optJointVentureProperty, optBusinessEntityType) match {
        case (Some(vrn), Some(true), Some(GeneralPartnership)) =>
          storeJointVentureInformationService.storeJointVentureInformation(vrn) map {
            case Right(StoreJointVentureInformationSuccess) => Redirect(principalRoutes.DirectDebitResolverController.show())
            case Left(StoreJointVentureInformationFailureResponse(status)) =>
              throw new InternalServerException("Store Joint Venture Partnership Information failed with status code: " + status)
          }
        case (Some(vrn), _, Some(GeneralPartnership)) =>
          (optPartnershipUtr, optPartnershipPostCode) match {
            case (Some(utr), Some(_)) =>
              storePartnershipInformationService.storePartnershipInformation(
                vatNumber = vrn,
                sautr = Some(utr),
                postCode = optPartnershipPostCode
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
            case _ =>
              Future.successful(Redirect(principalRoutes.CaptureBusinessEntityController.show()))
          }
        case (Some(vrn), _, Some(entity: LimitedPartnershipBase)) =>
          (optPartnershipUtr, optPartnershipPostCode, optPartnershipCrn, optPartnershipType) match {
            case (Some(utr), Some(_), Some(crn), Some(partnershipType)) =>
              storePartnershipInformationService.storePartnershipInformation(
                vatNumber = vrn,
                sautr = Some(utr),
                companyNumber = crn,
                partnershipEntity = partnershipType,
                postCode = optPartnershipPostCode
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
            case _ =>
              Future.successful(Redirect(principalRoutes.CaptureBusinessEntityController.show()))
          }
        case (None, _, _) =>
          Future.successful(Redirect(principalRoutes.ResolveVatNumberController.resolve()))
        case _ =>
          Future.successful(Redirect(principalRoutes.CaptureBusinessEntityController.show()))
      }
    }
  }

}
