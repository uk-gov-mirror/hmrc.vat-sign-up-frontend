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
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, JointVenturePropertyJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreJointVentureInformationHttpParser.{StoreJointVentureInformationFailureResponse, StoreJointVentureInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.{StoreJointVentureInformationService, StorePartnershipInformationService}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships

import scala.concurrent.Future

@Singleton
class CheckYourAnswersPartnershipController @Inject()(val controllerComponents: ControllerComponents,
                                                      val storePartnershipInformationService: StorePartnershipInformationService,
                                                      val storeJointVentureInformationService: StoreJointVentureInformationService)
  extends AuthenticatedController(AgentEnrolmentPredicate, featureSwitches = Set(GeneralPartnershipJourney, LimitedPartnershipJourney)) {

  override protected def featureEnabled[T](func: => T): T =
    if (featureSwitches exists isEnabled) func
    else throw new NotFoundException(featureSwitchError)

  private def utrPostcodePredicate(f: (String, PostCode) => Future[Result])(implicit request: Request[_]): Future[Result] = {

    val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
    val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)

    (optPartnershipUtr, optPartnershipPostCode) match {
      case (Some(utr), Some(postcode)) => f(utr, postcode)
      case (None, _) => Future.successful(Redirect(routes.CapturePartnershipUtrController.show()))
      case (_, None) => Future.successful(Redirect(routes.PartnershipPostCodeController.show()))
    }
  }

  private def crnAndEntityTypePredicate(f: (String, PartnershipEntityType) => Future[Result])(implicit request: Request[_]): Future[Result] = {

    val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)
    val optPartnershipType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)

    (optPartnershipCrn, optPartnershipType) match {
      case (Some(crn), Some(entityType)) => f(crn, entityType)
      case (_, _) => Future.successful(Redirect(routes.AgentCapturePartnershipCompanyNumberController.show()))
    }
  }


  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {

      val optBusinessEntityType = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey) filter (_.nonEmpty)

      (optVatNumber, optBusinessEntityType) match {
        case (Some(_), Some(GeneralPartnership)) => showGeneralPartnershipAnswers()
        case (Some(_), Some(entity: LimitedPartnershipBase)) => showLimitedPartnershipAnswers(entity)
        case (None, _) => Future.successful(Redirect(agentRoutes.CaptureVatNumberController.show()))
        case (_, None) => Future.successful(Redirect(agentRoutes.CaptureBusinessEntityController.show()))
      }
    }
  }

  private def showGeneralPartnershipAnswers()(implicit request: Request[_]): Future[Result] = {

    val optJointVentureProperty = request.session.getModel[YesNo](SessionKeys.jointVentureOrPropertyKey)

    (isEnabled(JointVenturePropertyJourney), optJointVentureProperty) match {
      case (true, None) => Future.successful(Redirect(routes.JointVenturePropertyController.show()))
      case (true, Some(Yes)) =>
        Future.successful(Ok(check_your_answers_partnerships(
          entityType = GeneralPartnership,
          companyUtr = None,
          companyNumber = None,
          postCode = None,
          jointVentureProperty = optJointVentureProperty,
          postAction = routes.CheckYourAnswersPartnershipController.submit()
        )))
      case (_, _) => utrPostcodePredicate { (utr, postcode) =>
        Future.successful(Ok(check_your_answers_partnerships(
          entityType = GeneralPartnership,
          companyUtr = Some(utr),
          companyNumber = None,
          postCode = Some(postcode),
          jointVentureProperty = optJointVentureProperty,
          postAction = routes.CheckYourAnswersPartnershipController.submit()
        )))
      }
    }
  }

  private def showLimitedPartnershipAnswers(entity: LimitedPartnershipBase)(implicit request: Request[_]): Future[Result] = {
    crnAndEntityTypePredicate { (crn, _) =>
      utrPostcodePredicate { (utr, postcode) =>
        Future.successful(Ok(check_your_answers_partnerships(
          entityType = entity,
          companyUtr = Some(utr),
          companyNumber = Some(crn),
          postCode = Some(postcode),
          jointVentureProperty = None,
          postAction = routes.CheckYourAnswersPartnershipController.submit()
        )))
      }
    }
  }



  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {

      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optBusinessEntityType = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)

      (optVatNumber, optBusinessEntityType) match {
        case (Some(vrn), Some(GeneralPartnership)) => submitGeneralPartnershipAnswers(vrn)
        case (Some(vrn), Some(entity: LimitedPartnershipBase)) => submitLimitedPartnershipAnswers(vrn, entity)
        case (None, _) => Future.successful(Redirect(agentRoutes.CaptureVatNumberController.show()))
        case (_, None) => Future.successful(Redirect(agentRoutes.CaptureBusinessEntityController.show()))
      }
    }
  }

  private def submitGeneralPartnershipAnswers(vrn: String)(implicit request: Request[_]): Future[Result] = {

    val optJointVentureProperty = request.session.getModel[YesNo](SessionKeys.jointVentureOrPropertyKey)

    (isEnabled(JointVenturePropertyJourney), optJointVentureProperty) match {
      case (true, None) =>
        Future.successful(Redirect(routes.JointVenturePropertyController.show()))
      case (true, Some(Yes)) =>
        storeJointVentureInformationService.storeJointVentureInformation(vrn) map {
          case Right(StoreJointVentureInformationSuccess) => Redirect(agentRoutes.EmailRoutingController.route())
          case Left(StoreJointVentureInformationFailureResponse(status)) =>
            throw new InternalServerException("Store Joint Venture Partnership Information failed with status code: " + status)
        }
      case (_, _) =>
        utrPostcodePredicate { (utr, postcode) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vrn,
            sautr = utr,
            postCode = Some(postcode)
          ) map handleStorePartnershipResult
        }
    }
  }

  private def submitLimitedPartnershipAnswers(vrn: String, entity: BusinessEntity)(implicit request: Request[_]): Future[Result] = {
    crnAndEntityTypePredicate { (crn, partnershipType) =>
      utrPostcodePredicate { (utr, postcode) =>
        storePartnershipInformationService.storePartnershipInformation(
          vatNumber = vrn,
          sautr = utr,
          companyNumber = crn,
          partnershipEntity = partnershipType,
          postCode = Some(postcode)
        ) map handleStorePartnershipResult
      }
    }
  }

  private val handleStorePartnershipResult: StorePartnershipInformationResponse => Result = {
    case Right(StorePartnershipInformationSuccess) =>
      Redirect(agentRoutes.EmailRoutingController.route())
    case Left(PartnershipUtrNotFound) =>
      Redirect(routes.CouldNotConfirmPartnershipController.show())
    case Left(StorePartnershipKnownFactsFailure) =>
      Redirect(routes.CouldNotConfirmPartnershipController.show())
    case Left(StorePartnershipInformationFailureResponse(status)) =>
      throw new InternalServerException("Store Partnership failed with status code: " + status)
  }
}
