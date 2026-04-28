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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.ApplicationWithWiremock
import models.MgdCertificate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import org.scalatest.RecoverMethods.*

import scala.concurrent.ExecutionContext

class GamblingConnectorSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val connector: GamblingConnector =
    app.injector.instanceOf[GamblingConnector]

  private val mgdRegNumber = "MGD12345"

  private val validJson =
    s"""
       |{
       |  "mgdRegNumber": "$mgdRegNumber",
       |  "registrationDate": "2026-01-01",
       |  "individualName": "John Doe",
       |  "businessName": "Test Business Ltd",
       |  "tradingName": "Test Trading",
       |  "repMemName": "Rep Member",
       |  "busAddrLine1": "Line 1",
       |  "busAddrLine2": "Line 2",
       |  "busAddrLine3": "Line 3",
       |  "busAddrLine4": "Line 4",
       |  "busPostcode": "AB1 2CD",
       |  "busCountry": "UK",
       |  "busAdi": "123",
       |  "repMemLine1": "Rep Line 1",
       |  "repMemLine2": "Rep Line 2",
       |  "repMemLine3": "Rep Line 3",
       |  "repMemLine4": "Rep Line 4",
       |  "repMemPostcode": "EF3 4GH",
       |  "repMemAdi": "456",
       |  "typeOfBusiness": "Limited",
       |  "businessTradeClass": 1,
       |  "noOfPartners": 2,
       |  "groupReg": "N",
       |  "noOfGroupMems": 0,
       |  "dateCertIssued": "2026-01-01",
       |  "partMembers": [],
       |  "groupMembers": [],
       |  "returnPeriodEndDates": []
       |}
       |""".stripMargin

  "GamblingConnector#getCertificate" should {

    "return MgdCertificate when BE returns 200 with valid JSON" in {

      stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(validJson)
          )
      )

      val result: MgdCertificate =
        connector.getCertificate(mgdRegNumber).futureValue

      result.mgdRegNumber mustBe mgdRegNumber
      result.groupReg mustBe "N"
      result.partMembers mustBe empty
      result.groupMembers mustBe empty
    }

    "fail when BE returns 200 with invalid JSON" in {

      stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getCertificate(mgdRegNumber).futureValue
      }

      ex.getMessage.toLowerCase must include("js")
    }

    "propagate UpstreamErrorResponse when BE returns 500" in {

      stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber)
      }.map { ex =>
        ex.statusCode mustBe INTERNAL_SERVER_ERROR
      }
    }

    "propagate UpstreamErrorResponse when BE returns 404" in {

      stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
              .withBody("not found")
          )
      )

      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber)
      }.map { ex =>
        ex.statusCode mustBe NOT_FOUND
      }
    }
  }
}
