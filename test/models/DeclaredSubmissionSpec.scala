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

package models

import models.DeclaredSubmissionTestData.validResponseDeclaredSubmission
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResult, JsSuccess, Json}

class DeclaredSubmissionSpec extends AnyWordSpec with Matchers {

  "DeclaredSubmission JSON format" should {

    "apply method calculates netMGDPayableOnThisReturn correctly" in {
      validResponseDeclaredSubmission.netMGDPayableOnThisReturn mustBe BigDecimal(-127.55)
    }

    "apply method reverses sign on amountBroughtForward correctly" in {
      validResponseDeclaredSubmission.amountBroughtForward mustBe BigDecimal(-1.99)
    }

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseDeclaredSubmission)
      (json \ "dutyPayableBeforeAdjustments").as[BigDecimal] mustBe BigDecimal(-133.33)
      (json \ "underDeclaredTaxFromPreviousPeriods").as[BigDecimal] mustBe BigDecimal(7.77)
      (json \ "amountBroughtForward").as[BigDecimal] mustBe BigDecimal(-1.99)
      (json \ "netMGDPayableOnThisReturn").as[BigDecimal] mustBe BigDecimal(-127.55)
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "dutyPayableBeforeAdjustments": -133.33,
           |  "underDeclaredTaxFromPreviousPeriods": 7.77,
           |  "amountBroughtForward": -1.99,
           |  "netMGDPayableOnThisReturn": -127.55
           |}""".stripMargin
      )

      val result: JsResult[DeclaredSubmission] = json.validate[DeclaredSubmission]

      result mustBe JsSuccess(validResponseDeclaredSubmission)
      result.get.netMGDPayableOnThisReturn mustBe BigDecimal(-127.55)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponseDeclaredSubmission)
      val parsed = json.as[DeclaredSubmission]

      parsed mustBe validResponseDeclaredSubmission
      parsed.netMGDPayableOnThisReturn mustBe validResponseDeclaredSubmission.netMGDPayableOnThisReturn
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        s"""{
           |  "dutyPayableBeforeAdjustments": -133.33,
           |  "underDeclaredTaxFromPreviousPeriods": 7.77,
           |  "netMGDPayableOnThisReturn": -127.55
           |}""".stripMargin
      )

      val result = json.validate[DeclaredSubmission]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "dutyPayableBeforeAdjustments": -133.33,
           |  "underDeclaredTaxFromPreviousPeriods": "£7.77",
           |  "amountBroughtForward": -1.99,
           |  "netMGDPayableOnThisReturn": -127.55
           |}""".stripMargin
      )

      val result = json.validate[DeclaredSubmission]

      result.isError mustBe true
    }
  }
}

object DeclaredSubmissionTestData {
  val validResponseDeclaredSubmission = DeclaredSubmission(
    dutyPayableBeforeAdjustments        = -133.33,
    underDeclaredTaxFromPreviousPeriods = 7.77,
    amountBroughtForward                = 1.99
  )

  val zeroResponseDeclaredSubmission = DeclaredSubmission(
    dutyPayableBeforeAdjustments        = 0.00,
    underDeclaredTaxFromPreviousPeriods = 0.00,
    amountBroughtForward                = 0.00,
    netMGDPayableOnThisReturn           = 0.00
  )
}
