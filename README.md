slack-budget-tool
=================

What it is
----------
Spring Boot backend ("slack-builder") for budgets and spending: REST APIs for budgets and transactions, PostgreSQL persistence via JPA, and a Slack-driven flow so users can log purchases through messages. The app stores transactions through the existing service/DAO layer. Users log expenses directly via Slack messages using a structured workflow:

	User submits an expense message → prompted for category and cost
	•	Application processes the input, validates data, and persists the transaction via the service/DAO layer
	•	Transaction is stored in PostgreSQL using JPA, updating budget state in real time

This workflow effectively turns Slack into a lightweight mobile interface for expense tracking, allowing users to log purchases on demand without a dedicated frontend.

Slack integration
-----------------
Outbound: SlackBot calls Slack Web API `chat.postMessage` using a bot OAuth token (Bearer).

Inbound: Slack Events API sends JSON to POST `/slack/events`. The app responds to URL verification (`challenge`), then handles `message` events and runs a guided conversation (amount, vendor, category) that persists via TransactionDAO and replies in the channel or DM.

Configuration (environment variables)
-------------------------------------
Do not commit real tokens. Set:

  SLACK_BOT_TOKEN      Bot User OAuth Token (xoxb-...)
  SLACK_CHANNEL_ID     Default channel for test/manual message endpoints
  SLACK_SIGNING_SECRET Signing secret from the Slack app (use for request signature verification in production; not yet wired in code)

Database settings are in `slack-builder/src/main/resources/application.properties` (URL, user, password).

Prerequisites to run
--------------------
- JDK 11, Maven
- PostgreSQL database matching `application.properties`; schema/data init uses `schema.sql` and `data.sql` as configured
- From repo: `cd slack-builder` then `mvn spring-boot:run` (or your IDE run configuration)

Slack app setup (replicate Events)
----------------------------------
1. Create an app at https://api.slack.com and install it to a workspace.
2. Add a bot token with scopes appropriate for posting and reading messages you need (e.g. `chat:write`; add channel/DM read scopes if required for your event types).
3. Event Subscriptions: enable and set Request URL to `https://<your-public-host>/slack/events` (use ngrok or similar for local development).
4. Subscribe to `message.channels` and/or `message.im` depending on whether you use channels or DMs.

Service listens on `server.port` in application.properties (default 8080).
