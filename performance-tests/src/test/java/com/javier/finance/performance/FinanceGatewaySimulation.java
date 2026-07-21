package com.javier.finance.performance;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class FinanceGatewaySimulation extends Simulation {

    private final String baseUrl = System.getProperty("baseUrl", "http://localhost:8090");
    private final String username = System.getProperty("username", "javier");
    private final String password = System.getProperty("password", "Password123!");

    private final int rampUsers = Integer.getInteger("rampUsers", 20);
    private final double steadyUsersPerSecond =
        Double.parseDouble(System.getProperty("steadyUsersPerSecond", "10"));
    private final double peakUsersPerSecond =
        Double.parseDouble(System.getProperty("peakUsersPerSecond", "30"));
    private final int p95Milliseconds = Integer.getInteger("p95Ms", 800);

    private final String loginBody = "{\"username\":\"" + username
        + "\",\"password\":\"" + password + "\"}";

    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(baseUrl)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    private final ScenarioBuilder financeScenario = scenario("Finance gateway security and discovery")
        .exec(
            http("Login through gateway")
                .post("/api/auth/login")
                .body(StringBody(loginBody))
                .check(status().is(200))
                .check(jsonPath("$.token").saveAs("authToken"))
                .check(jsonPath("$.id").saveAs("userId"))
        )
        .exec(
            http("Read debts through gateway")
                .get("/api/debts/user/#{userId}")
                .header("Authorization", "Bearer #{authToken}")
                .check(status().is(200))
        )
        .exec(
            http("Analytics relays token to debt service")
                .get("/api/analytics/demo/debts/#{userId}")
                .header("Authorization", "Bearer #{authToken}")
                .check(status().is(200))
                .check(header("X-Token-Relay").is("forwarded"))
        )
        .exec(
            http("Discover a debt service instance")
                .get("/api/debts/demo/instance")
                .header("Authorization", "Bearer #{authToken}")
                .check(status().is(200))
                .check(jsonPath("$.instance").exists())
        );

    {
        setUp(
            financeScenario.injectOpen(
                rampUsers(rampUsers).during(20),
                constantUsersPerSec(steadyUsersPerSecond).during(30),
                rampUsersPerSec(steadyUsersPerSecond).to(peakUsersPerSecond).during(30)
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile(95).lt(p95Milliseconds)
        );
    }
}
