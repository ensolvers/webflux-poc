# Enable TokenCleaningProcessorJob

Core allows a way to automatically clean expired auth tokens that can' t be used anymore

To enable the token cleaning processor job you need to add the following property to your **application.properties** file

`core.ensolvers.token.cleaning.processor.enabled=true`

By default, the jobs run at the start of each day, but you can configure the repeat interval in milliseconds by adding the following property to your application.properties file

`core.ensolvers.token.cleaning.processor.repeat.interval=60000` (one minute in milliseconds)

**Note:** You need to implement [Job configuration with Quartz](docs/Job_Configuration_With_Quartz.md). Otherwise, the processor will not run.