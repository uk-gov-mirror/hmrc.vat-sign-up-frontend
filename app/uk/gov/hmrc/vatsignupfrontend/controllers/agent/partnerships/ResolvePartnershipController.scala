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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipNoSAUTR, OptionalSautrJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.models.{BusinessEntity, GeneralPartnership, LimitedPartnershipBase}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.SessionUtils

import scala.concurrent.Future

@Singleton
class ResolvePartnershipController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val resolve: Action[AnyContent] = Action.async { implicit request =>

      val optBusinessEntity = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)

      authorised() {
        optBusinessEntity match {
          case Some(_: LimitedPartnershipBase) =>
            Future.successful(
              Redirect(routes.AgentCapturePartnershipCompanyNumberController.show())
            )
          case Some(GeneralPartnership) if isEnabled(GeneralPartnershipNoSAUTR) =>
            Future.successful(
              Redirect(routes.CapturePartnershipUtrController.show())
            )
          case Some(GeneralPartnership) if isEnabled(OptionalSautrJourney) =>
            Future.successful(
              Redirect(routes.DoesYourClientHaveAUtrController.show())
            )
          case Some(GeneralPartnership) =>
            Future.successful(
              Redirect(routes.CapturePartnershipUtrController.show())
            )
          case _ =>
            throw new InternalServerException("Not a partnership entity")
        }
      }
    }

}
