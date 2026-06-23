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
import models.{MgdCertificate, SubmittedReturnSingle, SubmittedReturns, SubmittedReturnsItem}
import org.scalatest.RecoverMethods.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.LocalDate
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

  "getSubmittedReturns" should {

    val regNumber = "XWM00003102200"
    val submittedReturnsResponseJson =
      s"""
         |{
         |  "items": [
         |    {
         |      "consec_no":12345,
         |      "mgd_period":"01/01/2025 - 30/03/2025",
         |      "submitted_date":"2025-04-01",
         |      "ack_ref":"123456789012345"
         |    }
         |  ]
         |}
         |""".stripMargin

    val expectedSubmittedReturnsResponse = SubmittedReturns(
      items = Seq(
        SubmittedReturnsItem(consec_no      = 12345,
                             mgd_period     = "01/01/2025 - 30/03/2025",
                             submitted_date = LocalDate.of(2025, 4, 1),
                             ack_ref        = "123456789012345"
                            )
      )
    )

    "must return a deserialized SubmittedReturns for a 200 response" in {
      stubFor(
        get(urlEqualTo(s"/gambling/submitted-returns/$regNumber?sortBy=2&orderBy=DESC"))
          .willReturn(okJson(submittedReturnsResponseJson))
      )

      val result = connector.getSubmittedReturns(regNumber, 2, "DESC").futureValue
      result mustEqual expectedSubmittedReturnsResponse
    }

    "must forward the correct registration number in the URL" in {
      val otherRegNumber = "XWM00003102999"

      stubFor(
        get(urlEqualTo(s"/gambling/submitted-returns/$otherRegNumber?sortBy=2&orderBy=DESC"))
          .willReturn(okJson(submittedReturnsResponseJson))
      )

      val result = connector.getSubmittedReturns(otherRegNumber, 2, "DESC").futureValue
      result mustEqual expectedSubmittedReturnsResponse
    }

    "must forward custom sortBy and orderBy query parameters" in {
      val customSortBy = 1
      val customOrderBy = "DESC"

      stubFor(
        get(urlEqualTo(s"/gambling/submitted-returns/$regNumber?sortBy=$customSortBy&orderBy=$customOrderBy"))
          .willReturn(okJson(submittedReturnsResponseJson))
      )

      val result = connector.getSubmittedReturns(regNumber, customSortBy, customOrderBy).futureValue
      result mustEqual expectedSubmittedReturnsResponse
    }
  }

  "getSubmittedReturn" should {

    val regNumber = "XWM00003102200"
    val consecNo  = 12345

    def submittedReturnResponseJson(consecNo: Int = consecNo) =
      s"""
         |{
         |  "consecNo": $consecNo,
         |  "mgdPeriod": "01/01/2025 - 30/03/2025",
         |  "submittedDate": "2025-04-01",
         |  "ackRef": "123456789012345",
         |  "noOfMachines": 10,
         |  "netTakingsHigherRate": 5000.00,
         |  "netTakingsStdRate": 3000.00,
         |  "netTakingsLowerRate": 1000.00,
         |  "totalDueHigherRate": 1500.00,
         |  "totalDueStdRate": 600.00,
         |  "totalDueLowerRate": 50.00,
         |  "dutyPayable": 2150.00,
         |  "underDeclaredDuty": 0.00,
         |  "previousReturnAmount": 0.00,
         |  "negativeAmountCarriedForward": 0.00,
         |  "totalNetDutyPayable": 2150.00
         |}
         |""".stripMargin

    val expectedSubmittedReturnResponse = SubmittedReturnSingle(
      consecNo                     = consecNo,
      mgdPeriod                    = "01/01/2025 - 30/03/2025",
      submittedDate                = LocalDate.of(2025, 4, 1),
      ackRef                       = "123456789012345",
      noOfMachines                 = 10,
      netTakingsHigherRate         = BigDecimal(5000.00),
      netTakingsStdRate            = BigDecimal(3000.00),
      netTakingsLowerRate          = BigDecimal(1000.00),
      totalDueHigherRate           = BigDecimal(1500.00),
      totalDueStdRate              = BigDecimal(600.00),
      totalDueLowerRate            = BigDecimal(50.00),
      dutyPayable                  = BigDecimal(2150.00),
      underDeclaredDuty            = BigDecimal(0.00),
      previousReturnAmount         = BigDecimal(0.00),
      negativeAmountCarriedForward = BigDecimal(0.00),
      totalNetDutyPayable          = BigDecimal(2150.00)
    )

    "must return a deserialized SubmittedReturnSingle for a 200 response" in {
      stubFor(
        get(urlEqualTo(s"/gambling/submitted-return-details/$regNumber/$consecNo"))
          .willReturn(okJson(submittedReturnResponseJson()))
      )

      val result = connector.getSubmittedReturn(regNumber, consecNo).futureValue
      result mustEqual expectedSubmittedReturnResponse
    }

    "must forward the correct registration number and consecNo in the URL" in {
      val otherRegNumber = "XWM00003102999"
      val otherConsecNo  = 99999

      stubFor(
        get(urlEqualTo(s"/gambling/submitted-return-details/$otherRegNumber/$otherConsecNo"))
          .willReturn(okJson(submittedReturnResponseJson(otherConsecNo)))
      )

      val result = connector.getSubmittedReturn(otherRegNumber, otherConsecNo).futureValue
      result.consecNo mustEqual otherConsecNo
    }

    "must fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlEqualTo(s"/gambling/submitted-return-details/$regNumber/$consecNo"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getSubmittedReturn(regNumber, consecNo).futureValue
      }

      ex.getMessage.toLowerCase must include("js")
    }

    "must propagate UpstreamErrorResponse when BE returns 500" in {
      stubFor(
        get(urlEqualTo(s"/gambling/submitted-return-details/$regNumber/$consecNo"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.getSubmittedReturn(regNumber, consecNo)
      }.map { ex =>
        ex.statusCode mustBe INTERNAL_SERVER_ERROR
      }
    }

    "must propagate UpstreamErrorResponse when BE returns 404" in {
      stubFor(
        get(urlEqualTo(s"/gambling/submitted-return-details/$regNumber/$consecNo"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
              .withBody("not found")
          )
      )

      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.getSubmittedReturn(regNumber, consecNo)
      }.map { ex =>
        ex.statusCode mustBe NOT_FOUND
      }
    }
  }
}
