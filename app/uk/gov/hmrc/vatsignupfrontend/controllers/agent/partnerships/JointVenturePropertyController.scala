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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.JointVentureOrPropertyForm.jointVentureOrPropertyForm
import uk.gov.hmrc.vatsignupfrontend.models.{No, Yes}
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.joint_venture_or_property

import scala.concurrent.Future

@Singleton
class JointVenturePropertyController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate, featureSwitches = Set(JointVenturePropertyJourney)) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(joint_venture_or_property(
          jointVentureOrPropertyForm(true),
          postAction = routes.JointVenturePropertyController.submit()
        ))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        jointVentureOrPropertyForm(isAgent = true).bindFromRequest.fold(
          formWithErrors => BadRequest(joint_venture_or_property(
            jointVenturePropertyForm = formWithErrors,
            routes.JointVenturePropertyController.submit()
          )),
          {
            case Yes => Redirect(routes.CheckYourAnswersPartnershipController.show())
              .removingFromSession(SessionKeys.partnershipSautrKey)
              .removingFromSession(SessionKeys.businessPostCodeKey)
              .addingToSession(SessionKeys.jointVentureOrPropertyKey -> true.toString)
            case No => Redirect(routes.CapturePartnershipUtrController.show())
              .addingToSession(SessionKeys.jointVentureOrPropertyKey -> false.toString)
          }
        )
      )
    }
  }
}
