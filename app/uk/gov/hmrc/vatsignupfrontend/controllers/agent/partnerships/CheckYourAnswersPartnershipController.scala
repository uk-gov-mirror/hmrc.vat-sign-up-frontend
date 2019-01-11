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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.StorePartnershipInformationService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers

import scala.concurrent.Future

@Singleton
class CheckYourAnswersPartnershipController @Inject()(val controllerComponents: ControllerComponents,
                                                      val storePartnershipInformationService: StorePartnershipInformationService)
  extends AuthenticatedController(AgentEnrolmentPredicate, featureSwitches = Set(GeneralPartnershipJourney, LimitedPartnershipJourney)) {

  override protected def featureEnabled[T](func: => T): T =
    if (featureSwitches exists isEnabled) func
    else throw new NotFoundException(featureSwitchError)

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey) filter (_.nonEmpty)
      val optSaUtr = request.session.get(SessionKeys.partnershipSautrKey) filter (_.nonEmpty)
      val optBusinessPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optBusinessEntity = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)

      (optVatNumber, optSaUtr, optBusinessPostCode) match {
        case (Some(_), Some(saUtr), Some(postCode)) =>
          optBusinessEntity match {
            case Some(businessEntity: LimitedPartnershipBase) =>
              Future.successful(
                Ok(check_your_answers(
                  utr = saUtr,
                  entityType = businessEntity,
                  postCode = postCode,
                  companyNumber = optPartnershipCrn,
                  postAction = routes.CheckYourAnswersPartnershipController.submit())
                )
              )
            case Some(GeneralPartnership) =>
              Future.successful(
                Ok(check_your_answers(
                  utr = saUtr,
                  entityType = GeneralPartnership,
                  postCode = postCode,
                  companyNumber = None,
                  postAction = routes.CheckYourAnswersPartnershipController.submit())
                )
              )
            case _ =>
              Future.successful(
                Redirect(agentRoutes.CaptureBusinessEntityController.show())
              )
          }
        case (None, _, _) =>
          Future.successful(Redirect(agentRoutes.CaptureVatNumberController.show()))
        case _ =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
      }
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optSaUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optBusinessPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optPartnershipEntityType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)

      (optVatNumber, optSaUtr, optBusinessPostCode, optPartnershipEntityType, optPartnershipCrn) match {
        case (Some(vatNumber), Some(saUtr), Some(postCode), Some(partnershipEntityType: LimitedPartnershipEntityType), Some(companyNumber)) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = saUtr,
            companyNumber,
            partnershipEntityType,
            postCode = Some(postCode)
          ) map {
            case Right(StorePartnershipInformationSuccess) =>
              Redirect(agentRoutes.EmailRoutingController.route())
            case Left(StorePartnershipKnownFactsFailure) =>
              Redirect(routes.CouldNotConfirmPartnershipController.show())
            case Left(PartnershipUtrNotFound) =>
              Redirect(routes.CouldNotConfirmPartnershipController.show())
            case Left(StorePartnershipInformationFailureResponse(failure)) =>
              throw new InternalServerException(s"Failed to save partnership information with error $failure")
          }
        case (Some(vatNumber), Some(saUtr), Some(postCode), None, None) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = saUtr,
            postCode = Some(postCode)
          ) map {
            case Right(StorePartnershipInformationSuccess) =>
              Redirect(agentRoutes.EmailRoutingController.route())
            case Left(StorePartnershipKnownFactsFailure) =>
              Redirect(routes.CouldNotConfirmPartnershipController.show())
            case Left(PartnershipUtrNotFound) =>
              Redirect(routes.CouldNotConfirmPartnershipController.show())
            case Left(StorePartnershipInformationFailureResponse(failure)) =>
              throw new InternalServerException(s"Failed to save partnership information with error $failure")
          }
        case (None, _, _, _, _) =>
          Future.successful(
            Redirect(agentRoutes.CaptureVatNumberController.show())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
      }
    }
  }

}
