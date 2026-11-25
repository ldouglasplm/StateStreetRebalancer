# Technical Assessment

Thank you for your interest in a position with the Charles River Development (CRD) / Alpha Platform Engineering organization!

As a part of the technical interview process, candidates are asked to participate in a technical assessment exercise.

Please read through the problem below and answer the questions highlighted in yellow.

## Assessment scenario

Account ABC holds the following securities: IBM, MSFT, ORCL, AAPL and HD.

**Assumption: ABC falls.**

| Account ABC — $100K total assets (100% vested) | Security | Target % | Target variance | Unit price | Output — number of shares to buy/sell |
|---|---|---:|---:|---:|---|
| | IBM | 20 | -10 | $150 | |
| | MSFT | 20 | 0 | $90 | |
| | ORCL | 20 | 10 | $220 | |
| | AAPL | 20 | 0 | $450 | |
| | HD | 20 | 0 | $70 | |
| **Total assets: $100,000** | | | | | |

- **Target %** indicates what percentage of total assets you should hold in your account.
- **Current %** shows what percentage of total assets you currently have in your account.
- **Target variance** shows how much deviation there is. A negative variance means you need to buy; a positive variance means you need to sell.

### What do you have to do to get to zero target variance?

1. Divide the account balance by 100 to get 1% of the account balance.
2. Multiply the 1% value by the variance to get the trade value.
3. Divide the trade value by the market price to get the buy/sell share quantity.

**Assumption:** The system only supports whole-number variance precision.

Assume that this rebalancing problem is your team's application that you are going to test. Please write manual test cases and automated test cases to test the application.

1. Recommended time for this task is 4–5 hours. Please feel free to research.
2. You can use any language/testing tool to code your automated test cases for this scenario. Make any reasonable assumptions, upload them to GitHub and share the URL.
3. The tests should validate that the total number of shares to buy and sell for each security is correct, as this is the application's output.
4. Please be prepared to discuss the logic, test cases and coded implementation during the interview.

**If you make any assumptions, please note them.**

**Additional assumptions:**

- Fractional shares are allowed.
- Rounding is half round down.

---

## Architectural Context

**Input rules are largely vague.** In a real-world scenario, this would prompt immediate clarification against business documentation with sign-offs.

This area requires a large number of assumptions across several possible architectures:

- A Kafka consumer operating within an event-driven architecture.
- A distributed service integrated with upstream trading platforms.
- A component within an algorithmic trading system or ETL pipeline.
- A standalone monolithic application or locally executed script.

Each architecture introduces different considerations around input validation, data ownership, security, authentication, persistence, failure handling, idempotency, observability and end-to-end testing.

**Assumption:** For this exercise, the rebalancer will be treated as an independently accessible API service on a simple EC2 instance, receiving rebalance instructions through HTTP POST requests. Scalability handling will not be included.

### Use Case and Upstream System Context

The intended consumer and operational context of the rebalancer are not explicitly defined.

Potential use cases include:

- **Algorithmic trading systems:** An automated trading strategy identifies portfolio deviations and submits rebalance instructions.
- **Account management dashboards:** An account manager manually initiates portfolio rebalancing based on investment objectives or client instructions.
- **Systematic trading interfaces:** A trading platform or contractual interface submits structured trading instructions for execution by the wider trading infrastructure.
- **Portfolio management systems:** A portfolio manager or investment system initiates rebalancing based on target asset allocations and portfolio constraints.

These scenarios introduce different expectations around request initiation, authorisation, input validation, execution, auditability and error handling.

**Assumption:** For this assessment, the upstream consumer will be treated as an external system or authorised user submitting a rebalance request through the API. The specific originating application and business workflow are outside the implementation scope of the assignment.

**Testing format:** Black-box (specification-based).

### Reading and source list for test plan

- ISTQB Financial Testing (FT)
- ISTQB Certified Tester Foundation Level – Financial Testing (CTFL-FT)
- ISTQB Advanced Level
- ISTQB Certified Tester Foundation Level (CTFL v4.0)

## Ground Rules & Key Assumptions

- The rebalancer is an API used by both systems and authorised users. My team is solely a backend team focused on the rebalancer service.
- The rebalancer has authorised access to a `get_balance(account_id)` API owned by another team and exposed internally. (Reasonable assumption — otherwise.)
- Fractional shares are allowed up to 2 decimal places.
- Account balances are pre-normalised to USD.
- Rounding considerations on purchases will follow a standard 2-decimal-place rule.
- Alterations to account holdings should only require variance changes. Only the variance is provided. Business-rule clarity is absolutely needed around this upstream logic, so I am assuming that either the buy-side/sell-side system and the user have the ability to make alterations using specifically the variances.
- Market data is an upstream service owned by another team and is fed to us in real time.

### Minimum input contract assumption

An extremely broad assumption: the following can be assumed as the minimum input required to establish the core service:

```json
{
  "account_id": "id",
  "securities": [
    {
      "symbol": "IBM",
      "variance": -10,
      "price": 150.00
    }
  ]
}
```

The account ID identifies the account. Each security contains its symbol, variance and price. The exact source of the price (provided in the request or retrieved from the market-data service) needs business clarification; the original assumption is that market data is supplied in real time by another team.

### Minimum output contract assumption

Another assumption can be made to fulfil requirement 3:

> The tests should validate that the total number of shares to buy and sell for each security is correct, as this is the application's output.

```json
{
  "action": "buy/sell/nothing",
  "shares": "amount",
  "symbol": "sym",
  "trade_value": "+/- $x.xx"
}
```

**Reason for including `trade_value`:** This is a reasonable assumption. A small slippage and pre-emptive control mechanism could carry significant weight in a monetary system, where response delay may be less significant than price changes. Having a trade value per security in the response, at the time of calculation, allows a cross-reference in the buy-side/sell-side system, especially where trade execution can be reverted during large price changes.

Returning the calculated trade value alongside the share quantity also gives the downstream system a value to cross-check against the calculation. The exact slippage controls and how the downstream system uses this value would need to be clarified; they are not specified in the assessment.

---

## Test Design

With these assumptions, I can make a minimum viable test-case design using:

- BVA (Boundary Value Analysis)
- Decision tables
- Error guessing
- Equivalence Partitioning

### BVA — Variance

The variance limits need to be established through business rules. For this exercise, the assumption is **-75% / +75%**.

| Test | Data | Expected |
|---|---:|---|
| Below minimum | -75.01% | Rejected |
| Minimum | -75% | Accepted |
| Above minimum | -74.9% | Accepted |
| Below maximum | 74.9% | Accepted |
| Maximum | 75% | Accepted |
| Above maximum | 75.1% | Rejected |

- **Price:** Boundary values still need to be defined.
- **Shares:** Boundary values still need to be defined.

### Decision Table — Authentication and Security Controls

Authentication and security controls are prioritised first. OAuth is an assumption for this exercise, given the importance of this component in a system such as the rebalancer.

1. **REQ-001 – Valid OAuth:** The system shall authenticate requests containing a valid OAuth access token.
2. **REQ-002 – Invalid OAuth:** The system shall reject requests containing an invalid, malformed or expired OAuth token.
3. **REQ-003 – Missing Authentication:** The system shall reject requests that do not contain an OAuth access token.
4. **REQ-004 – Token Expiration:** The system shall reject expired tokens and return an appropriate HTTP 401 response.
5. **REQ-005 – Role-Based Access:** The system shall restrict access to protected resources based on the permissions associated with the authenticated user.
6. **REQ-006 – Request Validation:** The system shall reject requests containing missing or invalid mandatory parameters.

| Rule | OAuth present | OAuth valid | Token expired | API key valid | User authorised | Expected result |
|---|---|---|---|---|---|---|
| R1 | Yes | Yes | No | Yes | Yes | 200 OK |
| R2 | Yes | Yes | No | Yes | No | 403 Forbidden |
| R3 | Yes | No | No | Yes | Yes | 401 Unauthorized |
| R4 | Yes | Yes | Yes | Yes | Yes | 401 Unauthorized |
| R5 | No | N/A | N/A | Yes | Yes | 401 Unauthorized |
| R6 | Yes | Yes | No | No | Yes | 401 Unauthorized |
| R7 | No | N/A | N/A | No | No | 401 Unauthorized |

### Contract — Input Validation

- **REQ-1:** All four conditions are valid. Process transaction.
- **REQ-2:** Invalid account ID. Reject request.
- **REQ-3:** Invalid security symbol. Reject request.
- **REQ-4:** Invalid variance. Reject request.
- **REQ-5:** Invalid price. Reject request.
- **REQ-6:** All four conditions are valid, representing a second valid scenario.

| Condition / Rule | R1 | R2 | R3 | R4 | R5 | R6 |
|---|---|---|---|---|---|---|
| Account ID valid? | Yes | No | Yes | Yes | Yes | Yes |
| Security symbol valid? | Yes | Yes | No | Yes | Yes | Yes |
| Variance valid? | Yes | Yes | Yes | No | Yes | Yes |
| Price valid? | Yes | Yes | Yes | Yes | No | Yes |
| All inputs valid? | Yes | No | No | No | No | Yes |
| Expected result | Calculate trade | Reject | Reject | Reject | Reject | Calculate trade |
| Output action | Buy/Sell/Nothing | N/A | N/A | N/A | N/A | Buy/Sell/Nothing |

### Error Guessing

| Error guessing | Consideration / suggested test |
|---|---|
| Overflow limitations/handling (can be catastrophic) | Apply BVA to variable upper and lower thresholds around numeric variance sizing. Attempt a POST with test data containing extreme variance values. |
| Contract data: ignore case on symbols and capitalisation handling | Input controls need to be dynamic. Check how different symbol capitalisation is handled. |
| Account masking | This should be handled using `account_id` and OAuth, but ensure masking cannot be performed. Ensure OAuth with various different account IDs is covered. |
| `get_account` service dependency unavailable | Is this handled gracefully and within reasonable timeframes? |
| Market-data service dependency unavailable | Is this handled gracefully and within reasonable timeframes? |

---

## Non-functional Considerations

### Performance Testing

**Three main areas I would prioritise:**

- **Load**
- **Stress**
- **Endurance**

**Key goals:**

#### 1. Response Time and Latency

This is a time-sensitive product due to the nature of the product and the likelihood of it being a prerequisite service for a larger trading microservice or system. Response time and latency should therefore be considered.

#### 2. Throughput and Scalability

If this is part of a large systematic trading system or algorithmic trading system, load balancing will be vital. Due to the assumptions and reasonable timeframe constraints, this will not be considered as part of this task.

#### 3. Concurrency, Contention and Idempotency Handling

Two conflicting rebalancing requests on the same account need to be handled. Business-rule clarity around prioritisation is required. The assumption is that later requests are prioritised.

### Dependency Testing

#### 1. Upstream Dependencies

**Assumption:** The rebalancer receives rebalance instructions through authenticated HTTP POST requests from an upstream trading or portfolio platform. The upstream system supplies the account ID and security-level information required for the calculation, including the security symbol and variance. The rebalancer also obtains the account balance through the internal `get_balance(account_id)` API owned by another team, and receives real-time market data from an upstream service owned by another team.

#### 2. Downstream Dependencies

**Assumption:** The rebalancer submits the calculated buy/sell instructions to a downstream trading execution service through an API. It also relies on persistent storage to record the rebalance request, calculated instructions and relevant execution outcomes.

The exact downstream execution flow, persistence ownership and whether execution is synchronous or asynchronous need business clarification.

### E2E Testing

**Source-to-target reconciliation:** As part of a microservice, end-to-end testing should be considered. Financial institutions are required to keep records of trading platform activity. This allows for database-level validation from buy triggers through to purchase records, most likely linked by a unique identifier.

Both E2E scenarios need to be covered on the premise that the triggers will be:

| Scenario | Expected validation |
|---|---|
| Buy/sell trigger | Validate the calculated instruction through to the corresponding purchase/sale record. |
| Rejected or unsuccessful trigger | Validate that the failed/rejected instruction is represented correctly and does not appear as a completed trade. |

**What i would consider high value next tasks:**
Containerisation - Docker network for local testing against stub - 
Single source of truth validation with OpenAPI - 
improvement of the teststub - 
Logging/DOcumentation for tracability - Make file