# Database Migrations

Before continuing to an actual implementation we should discuss how our entities will be created on our database.

A simple approach to this is, _do not reinvent the wheel_, just reuse whatever already exists as an example.

Most projects use migrations that use a numbered naming scheme that starts with 10000 (ie. 10001 is the _first_ migration), although this may very from project to project.

Below are some examples that you may find in your project.

## Liquibase Formatted SQL

A basic migration made in the SQL format could be made using Liquibase Formatted SQL, which allow us to improve the readability and reusability of plain SQL migrations.

<sub>_10001_create_client.sql_</sub>
```sql
--liquibase formatted sql

--changeset Pablo Luna:10001
--preconditions onFail:MARK_RAN onError:WARN onFailMessage:CREATE TABLE client table FAILED onErrorMessage:CREATE TABLE client FAILED
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'client'
--comment: /*comments should go after preCondition. If they are located before the precondition, then Liquibase usually gives error.*/
CREATE TABLE IF NOT EXISTS client (  
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NOT NULL,
	`address1` VARCHAR(50),
	`address2` VARCHAR(50),
	`city` VARCHAR(30),
    `type` VARCHAR(10),
    `idDocument` VARCHAR(32),
    PRIMARY KEY (`id`)
);
```
And then, running this migration is as simple as including it in the appropiate directory, which then will be run by liquibase.

## Liquibase XML

Another example you may find are migrations in the XML format of Liquibase.

Much like formatted SQL, XML is used here to improve the readability and reusability of migrations over plain old SQL.

A big improvement of XML over Formatted SQL is that Liquibase will translate our migration into the specific SQL flavour our database uses. 

At the end of the day, this is a tradeoff, since custom SQL would allow us to fine tune, and use specific functionality of the database of our choice, the choice is between consistency and customizability.

A simple conversion of our prior formatted SQL example would be:

<sub>_10001_create_client.xml_<sub>
```xml
<databaseChangeLog> <!--xml namespaces excluded for brevity-->
    <changeSet author="Pablo Luna" id="10001_create_client">
        
        <preConditions onFail="MARK_RAN" onError="WARN" onFailMessage="CREATE TABLE client table FAILED" onErrorMessage="CREATE TABLE client table FAILED">
            <and>
                <not>
                    <tableExists tableName="client"/>
                </not>
                <sqlCheck expectedResult="0">SELECT COUNT(*) FROM client;
                </sqlCheck>
            </and>
        </preConditions>
        <comment>
        Comments should go after preCondition. If they are located before the precondition, then Liquibase usually gives error.
        </comment>

        <createTable tableName="client">
            <column name="id" type="BIGINT(20)" autoIncrement="true" >
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="name" type="VARCHAR(50)" >
                <constraints nullable="false"/>
            </column>
            <column name="address1" type="VARCHAR(50)" />
            <column name="address2" type="VARCHAR(50)" />
            <column name="city" type="VARCHAR(30)" />
            <column name="type" type="VARCHAR(10)" />
            <column name="idDocument" type="VARCHAR(32)" />
        </createTable>
    </changeSet>
</databaseChangeLog>
```

Which, in the same way as before, must be added to a master changelog file

<sub>_db_changelog_master.xml_<sub>
```xml
<databaseChangeLog> <!--xml namespaces excluded for brevity-->
    <include file="changes/10001_create_client.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

You may have noticed that we can run arbitrary SQL in XML migrations, however it is recommended that you use structured XML unless required, ie. a structured XML 'query' may make the migration more complicated than desired; since Liquibase will not translate raw SQL to the SQL dialect our database uses.


Note: you are not limited to using Formatted SQL migrations with YAML changelogs, or XML migrations with XML changelogs; you are allowed to mix and match as desired, however, it would be preferred to maintain certain consistency between migration and changelog languages for the sake of simplicity.

You may check Liquibase's documentation on Changelogs here: [`Changelog`](https://docs.liquibase.com/concepts/changelogs/home.html#)

## Conditional execution

You may have noticed that our migrations have conditions as comments/as part of tags, these are used to check the consistency and structure of our already existing database before applying any changes, and if issues are found, which actions may be taken, if any at all.

You may check Liquibase's documentation on conditional execution here: [`Preconditions`](https://docs.liquibase.com/concepts/changelogs/preconditions.html#handling-failures-and-errors)

## Core specific recommendations

Because we do have entities that inherit from other entities, that may also inherit from other entitites, it is useful to comment on our migrations about which table columns correspond to each base entity that we may have inherited of in our code.


<sub>_10001_create_table_client.sql_<sub>
```sql
-- ...
CREATE TABLE IF NOT EXISTS `client` (

    -- inherited entity properties
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,  -- GenericModel
    `external_id` VARCHAR(64) NOT NULL,       -- PublicModel
    `created_at` TIMESTAMP NOT NULL,          -- AuditableModel
    `updated_at` TIMESTAMP NOT NULL,          -- AuditableModel
    `disabled_at` TIMESTAMP NULL,             -- AuditableModel
    
    -- general entity properties
    `name` VARCHAR(50) NOT NULL,
	`address1` VARCHAR(50),
	`address2` VARCHAR(50),
	`city` VARCHAR(30),
    `type` VARCHAR(10),
    `idDocument` VARCHAR(32),
    PRIMARY KEY (`id`)
    -- etc, etc...
);
```

### See also

[`Liquibase's documentation`](https://docs.liquibase.com/concepts/home.html)

[`Ensolvers Core Base entities`](./Base_Entities.md)

[`CRUD Documentation`](./README.md)

## Next: [Repositories](./Repositories.md)