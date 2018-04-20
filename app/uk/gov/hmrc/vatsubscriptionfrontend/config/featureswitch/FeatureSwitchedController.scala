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

package uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch

import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController

import scala.concurrent.Future

trait FeatureSwitchedController[A] extends FeatureSwitching {
  self: AuthenticatedController[A] =>

  val featureSwitches: Set[FeatureSwitch]

  def featureEnabledWithAuth(block: => Future[Result])(implicit request: Request[_]): Future[Result] =

    if (featureSwitches.forall(isEnabled)) authorised()(block)
    else Future.failed(new NotFoundException(request.uri))

}
