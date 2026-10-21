/*
 * Copyright 2026 HM Revenue & Customs
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

///*
// * Copyright 2026 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package utils
//
//import config.FrontendAppConfig
//import models.UserAnswers
//import pages.*
//import scala.math.BigDecimal.RoundingMode
//import javax.inject.{Inject, Singleton}
//import scala.util.{Success, Try}
//
//@Singleton
//class UnderDeclareDutyCalculator @Inject() (appConfig: FrontendAppConfig) {
//
//  def calculateMaximumAllowed(userAnswers: UserAnswers): BigDecimal = {
//
//    val lowerNetTakings =
//      userAnswers
//        .get(NetTakingsLowerPage)
//        .getOrElse(BigDecimal(0))
//
//    val standardNetTakings =
//      userAnswers
//        .get(NetTakingsStandardPage)
//        .getOrElse(BigDecimal(0))
//
//    val higherNetTakings =
//      userAnswers
//        .get(NetTakingsHigherPage)
//        .getOrElse(BigDecimal(0))
//
//    val totalNetTakings =
//      lowerNetTakings +
//        standardNetTakings +
//        higherNetTakings
//
//    val percentageOfTotalNetTakings =
//      (
//        totalNetTakings *
//          appConfig.underDeclaredDutyPercentage
//        ).setScale(
//        2,
//        RoundingMode.HALF_EVEN
//      )
//
//    appConfig.underDeclaredDutyMinimumLimit.max(
//      percentageOfTotalNetTakings.min(
//        appConfig.underDeclaredDutyMaximumLimit
//      )
//    )
//  }
//
//  def revalidateUnderDeclaredDuty(userAnswers: UserAnswers): Try[UserAnswers] = {
//    userAnswers.get(TotalUnderDeclaredDutyPage) match {
//      case Some(underDeclaredDuty) if underDeclaredDuty > calculateMaximumAllowed(userAnswers) => userAnswers.remove(TotalUnderDeclaredDutyPage)
//      case _                                                                                   => Success(userAnswers)
//    }
//  }
//}
