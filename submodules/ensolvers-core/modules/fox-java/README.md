# What is fox-java?

`fox-java` is a set of utilities and classes written in Java to simplify the development of enterprise applications

## How to use

In your `pom.xml`, simply add the following to enable access to our public Maven repositories

```
	<repositories>
		<repository>
			<id>ensolvers-java-fox</id>
			<name>Ensolvers java-fox</name>
			<url>https://maven.ensolvers.com/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</repository>
	</repositories>
```

Then, libraries can be referenced simply by its groupId, artifactId and version, for instance:

```
		<dependency>
			<groupId>com.ensolvers.fox-java</groupId>
			<artifactId>fox-s3</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
```

## Libraries included

- `fox-cache`: Memcached and Redis typed cache clients which simplify access to most common cache methods, serialization (using Jackson), and so on
- `fox-email`: Utilities for email sending and processing
- `fox-s3`: AWS S3 client that simplifies content uploading and fetching
- `fox-location`: IP2Location using MaxMind DB
- `fox-ses`: Utils for sending emails via AWS SES
- `fox-sns`: Utils for sending notifications via AWS SNS
- [`fox-spring`](modules/fox-spring/README.md): General utils for Spring and Spring Boot


## How to collaborate to this project

1. Ask for write access
2. Create a PR with the changes
3. After the PR is approved, ask for AWS credentials (since the repo is hosted in AWS)
4. Configure your AWS environment 
5. Run `deploy.sh`

If `deploy.sh` runs successfully, both the compiled jars and the source code should be upload to the repo