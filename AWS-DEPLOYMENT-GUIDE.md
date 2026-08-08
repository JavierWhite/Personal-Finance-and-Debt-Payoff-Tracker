
# AWS Deployment Guide for Checkpoint 3

This deployment uses EC2 for the application and Amazon MQ for the JMS broker. It is intended for the semester-project demonstration.

## 1. Create the EC2 instance

Recommended classroom configuration:

- Amazon Linux 2023
- `t3.large` for the full multi-container stack
- 30 GB gp3 storage
- Same VPC that will contain Amazon MQ
- Assign a public IPv4 address or Elastic IP

Create an EC2 security group with these inbound rules:

- TCP 22 from your public IP only
- TCP 8080 from your public IP for the frontend
- TCP 8090 from your public IP for the Gateway
- TCP 8761 from your public IP for the Eureka demonstration
- TCP 9411 from your public IP for Zipkin

Do not expose MySQL port 3306 or the Amazon MQ OpenWire port to the public internet.

## 2. Install Docker on EC2

SSH to the instance and run the included bootstrap script:

```bash
chmod +x scripts/aws-ec2-bootstrap.sh
scripts/aws-ec2-bootstrap.sh
```

Sign out and reconnect after the script finishes, then verify:

```bash
docker --version
docker compose version
```

## 3. Create Amazon MQ for ActiveMQ

In AWS Console:

1. Open Amazon MQ.
2. Create an ActiveMQ broker.
3. A single-instance broker is sufficient for the classroom checkpoint.
4. Put it in the same VPC as the EC2 instance.
5. Disable public accessibility.
6. Create an application user such as `finance_app` with a strong password.
7. Wait until the broker reaches `RUNNING`.
8. Copy its OpenWire SSL endpoint. It has the form:

```text
ssl://b-xxxxxxxx-1.mq.REGION.amazonaws.com:61617
```

For the Amazon MQ security group, allow inbound TCP 61617 from the EC2 instance security group. Using a security-group source is preferable to allowing a public CIDR.

## 4. Copy the project to EC2

Option A, copy this ZIP:

```bash
scp -i YOUR-KEY.pem personal-finance-checkpoint3-complete.zip ec2-user@EC2_PUBLIC_IP:~
```

On EC2:

```bash
unzip personal-finance-checkpoint3-complete.zip
cd personal-finance-checkpoint3
```

Option B, push the completed code to GitHub and clone it on EC2.

## 5. Configure AWS environment variables

Copy the template:

```bash
cp .env.aws.example .env.aws
nano .env.aws
```

Set:

- a strong MySQL root password
- finance database password
- a long JWT secret
- the exact Amazon MQ OpenWire SSL endpoint
- the Amazon MQ username and password

Compose will read this file directly with `--env-file .env.aws`. Do not commit `.env.aws` to GitHub.

For this classroom deployment, keep `SPRING_PROFILES_ACTIVE=dev`. The existing prod profile expects a separately provisioned SSL-enabled MySQL database and schema validation, while the checkpoint deployment keeps MySQL in Docker.

## 6. Build and launch on AWS

First build the Java modules:

```bash
mvn clean package -DskipTests
```

Then start the application:

```bash
docker compose --env-file .env.aws \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.aws.yml \
  up -d --build
```

Check status:

```bash
docker compose --env-file .env.aws \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.aws.yml \
  ps
```

Check the important logs:

```bash
docker logs finance-debt-service --tail 100
docker logs finance-analytics-service --tail 100
docker logs finance-api-gateway --tail 100
```

## 7. Verify AWS endpoints

Replace `EC2_PUBLIC_IP` below.

- Frontend: `http://EC2_PUBLIC_IP:8080`
- Gateway: `http://EC2_PUBLIC_IP:8090`
- Eureka: `http://EC2_PUBLIC_IP:8761`
- Zipkin: `http://EC2_PUBLIC_IP:9411`

Run the JMS demo against AWS from your local WSL machine:

```bash
BASE_URL=http://EC2_PUBLIC_IP:8090 scripts/checkpoint3-demo-jms.sh
```

The script's Docker log section is intended for local use. On AWS, show the logs in your SSH terminal instead:

```bash
docker logs finance-debt-service --tail 50
docker logs finance-analytics-service --tail 50
```

## 8. Run Gatling against AWS

Run Gatling from your local computer. This creates external traffic instead of generating the load from inside the EC2 instance.

```bash
BASE_URL=http://EC2_PUBLIC_IP:8090 P95_MS=1500 scripts/run-checkpoint3-stress.sh
```

While the test runs, open Zipkin on the EC2 address and search for traces.

If your network or EC2 size cannot support the default load, demonstrate a smaller successful test first:

```bash
BASE_URL=http://EC2_PUBLIC_IP:8090 RAMP_USERS=5 STEADY_USERS_PER_SECOND=2 PEAK_USERS_PER_SECOND=5 P95_MS=2000 scripts/run-checkpoint3-stress.sh
```

Then increase the load and report the actual results honestly.

## 9. What proves the assignment requirements

JMS proof:

- Debt Service source shows `JmsTemplate` publishing.
- Analytics source shows `@JmsListener` consumption.
- Amazon MQ console shows the `finance.debt.payment.recorded` queue and message activity.
- Analytics REST endpoint shows processed events.

Tracing proof:

- Zipkin receives traces from the AWS deployment.
- Payment traces contain HTTP spans and JMS publish/process spans.
- Run the Gatling test while Zipkin is open, then query the traces generated during the test.

AWS proof:

- EC2 Console shows the running instance.
- Amazon MQ Console shows the running ActiveMQ broker.
- Browser uses the EC2 public address for Gateway, frontend, Eureka, and Zipkin.

## 10. Shut down resources after grading

To stop containers:

```bash
docker compose --env-file .env.aws \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.aws.yml \
  down
```

Stop or terminate the EC2 instance and delete the Amazon MQ broker when you no longer need them. Amazon MQ and EC2 continue to incur AWS charges while provisioned.
