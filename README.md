slack-budget-tool
=================

What it is
----------
Spring Boot backend ("slack-builder") for budgets and spending: REST APIs for budgets and transactions, PostgreSQL persistence via JPA, and a Slack-driven flow so users can log purchases through messages. The app stores transactions through the existing service/DAO layer.

Stack: Java 11, Spring Boot 2.7, Spring Web, Spring Data JPA, PostgreSQL. The project includes the Slack Bolt artifact on the classpath; outbound posts and inbound events are implemented with HTTP + JSON handling in application code, not Bolt servlet wiring.

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
