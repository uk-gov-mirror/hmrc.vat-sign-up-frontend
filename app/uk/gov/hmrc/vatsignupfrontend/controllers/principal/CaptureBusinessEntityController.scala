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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{CtKnownFactsIdentityVerification, UseIRSA}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.CitizenDetailsHttpParser.CitizenDetailsRetrievalSuccess
import uk.gov.hmrc.vatsignupfrontend.models.{LimitedCompany, Other, SoleTrader}
import uk.gov.hmrc.vatsignupfrontend.services.CitizenDetailsService
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_business_entity

import scala.concurrent.Future

@Singleton
class CaptureBusinessEntityController @Inject()(val controllerComponents: ControllerComponents,
                                                citizenDetailsService: CitizenDetailsService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(capture_business_entity(businessEntityForm, routes.CaptureBusinessEntityController.submit()))
      )
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>
      businessEntityForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(capture_business_entity(formWithErrors, routes.CaptureBusinessEntityController.submit()))
          ),
        businessEntity => {
          {
            (businessEntity, enrolments.vatNumber) match {
              case (Other, _) => Future.successful(Redirect(routes.CannotUseServiceController.show()))
              case (LimitedCompany, Some(_)) =>
                if (isEnabled(CtKnownFactsIdentityVerification))
                  Future.successful(Redirect(routes.CaptureCompanyNumberController.show()))
                else
                  Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
              case (SoleTrader, Some(_)) => gotoSoleTraderFlow(enrolments)
              case _ => Future.successful(Redirect(routes.CheckYourAnswersController.show()))
            }
          } map (_.addingToSession(SessionKeys.businessEntityKey, businessEntity))
        }
      )
    }
  }

  private def gotoSoleTraderFlow(enrolments: Enrolments)(implicit request: Request[_]): Future[Result] = {
    if (isEnabled(UseIRSA)) {
      enrolments.selfAssessmentUniqueTaxReferenceNumber match {
        case None => Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
        case Some(utr) => citizenDetailsService.getCitizenDetails(utr) map {
          case Right(CitizenDetailsRetrievalSuccess(detailsModel)) =>
            Redirect(routes.ConfirmYourRetrievedUserDetailsController.show()).addingToSession(SessionKeys.userDetailsKey, detailsModel)
          case Left(reason) =>
            throw new InternalServerException(s"calls to CID received unexpected failure $reason")
        }
      }
    }
    else Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
  }

}
