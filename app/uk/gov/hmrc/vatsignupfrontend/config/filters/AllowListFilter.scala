/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.config.filters

import javax.inject.{Inject, Singleton}
import akka.stream.Materializer
import play.api.mvc.Call
import uk.gov.hmrc.allowlist.AkamaiAllowlistFilter
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig

@Singleton
class AllowListFilter @Inject()(appConfig: AppConfig,
                                val mat: Materializer
                               ) extends AkamaiAllowlistFilter {

  override lazy val allowlist: Seq[String] = appConfig.allowlistIps

  override lazy val destination: Call = Call("GET", appConfig.shutterPage)

  override lazy val excludedPaths: Seq[Call] = appConfig.ipExclusionList

}

