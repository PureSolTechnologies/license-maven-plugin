=====================================================================
  maven-license-plugin (PureSol Technologies' Maven License Plugin)
=====================================================================

This is a Maven plugin which checks the license settings in all dependencies
defined in the module for a known and valid license name. As soon as a license
is unknown, this Maven module fails.

This plugin is used to enforce license checks and approval. This is especially
useful for open source projects which need to check their dependencies not to
contain proprietary licenses. 

----------
  Usage:
----------

Sample plugin configuration:

<plugins>
	<plugin>
		<groupId>com.puresol.maven.plugins</groupId>
		<artifactId>license-plugin</artifactId>
		<version>1.0.0</version>
		<configuration>
			<recursive>true</recursive>
			<validLicenses>
				<validLicense>PureSol Technologies Commercial License</validLicense>
				<!-- Apache License -->
				<validLicense>Apache License, Version 2.0</validLicense>
				<validLicense>The Apache Software License, Version 2.0</validLicense>
				<!-- BSD License -->
				<validLicense>BSD style</validLicense>
				<!-- CPL -->
				<validLicense>Common Public License Version 1.0</validLicense>
				<!-- EPL -->
				<validLicense>Eclipse Public License</validLicense>
				<!-- LGPL -->
				<validLicense><![CDATA[GNU Lesser General Public License (LGPL), Version 2.1]]></validLicense>
				<!-- MIT License -->
				<validLicense>MIT License</validLicense>
				<validLicense>The MIT License</validLicense>
			</validLicenses>
			<approvedDependencies>
				<approvedDependency>p2.eclipse-plugin:.*:.*</approvedDependency>
				<approvedDependency>p2.eclipse-feature:.*:.*</approvedDependency>
				<approvedDependency></approvedDependency>
			</approvedDependencies>
		</configuration>
	</plugin>
</plugins>

Explanation for the different settings:

<recursive> (true/false):
    Define whether or not the dependencies are checked recursively. That 
    means, not only the dependencies are checked, but also their 
    dependencies.

<validLicenses> (List): 
    Define with <validLicense> the names of licenses which are valid to be
    used in the project.

<approvedDependencies> (List):
    Define with <approvedDependency> which artifacts were checked manually
    (for example on webpage) to have a valid license. This is used for 
    dependencies which do not have a license information set in their pom.xml.
    The string for specification has three parts separated by colon. These
    parts contain regular expressions for artifactId, groupId and version.
                                                     