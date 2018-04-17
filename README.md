polopoly-policy-mock
====================

Utilities to mock Atex' Polopoly's Policy classes using the data model objects the policy classes rely on.
Basically it relies on the concepts and code examples explained on
[Polopoly's Guide on Writing Tests](http://support.polopoly.com/doc/jar/10.6.1/dev-guide/testing.html) (You'll need
a valid Polopoly Support Account.'), extends them and creates a fluent API for it.

*Any issues? Need more features for your policies? [Create an issue](https://github.com/paulwellnerbou/polopoly-policy-mock/issues), or drop me a line.*

## Changelog

### Release 1.0

* Versions for Polopoly 10.16 and 10.8 available:
  * [ ![Download](https://api.bintray.com/packages/wellnerbou-polopoly/maven/polopoly-policy-mock/images/download.svg?version=polopoly10.16.5-1.0) ](https://bintray.com/wellnerbou-polopoly/maven/polopoly-policy-mock/polopoly10.16.5-1.0)
  * [ ![Download](https://api.bintray.com/packages/wellnerbou-polopoly/maven/polopoly-policy-mock/images/download.svg?version=polopoly10.8-1.0) ](https://bintray.com/wellnerbou-polopoly/maven/polopoly-policy-mock/polopoly10.8-1.0)
* Some significant fixes in the mocked content setup regarding external IDs, content lists, and others.
* Switching bintray organisation to allow some others to release fixes of this library

### Release 0.4

* Add branches for Polopoly 10.8 and 10.14, so there are two artifacts available
* Add `withExternalContentId(...)` to allow external ContentIDs for mocked policies
* Added mocked behavior for `getAvailableContentListNames()` (thanks to Fabian Oehlmann)
* Support for Polopoly 10.6 discontinued

### Release 0.3

* Add branches for Polopoly 10.6 and 10.14, so there are two artifacts available. Since 10.6 is discontinued, you can stil grab it here:
  * [ ![Download](https://api.bintray.com/packages/wellnerbou-polopoly/maven/polopoly-policy-mock/images/download.svg?version=polopoly10.6-0.3) ](https://bintray.com/wellnerbou-polopoly/maven/polopoly-policy-mock/polopoly10.6-0.3)
* Type MockPolicyBuilder with Generics, so casting is not necessary any more.
* Add fluent API to MockPolicyBuilder to add slots
* Add usage examples in MockPolicyBuilderTest
* This library is now available on [bintray](https://bintray.com/wellnerbou-polopoly/maven/polopoly-policy-mock)

## Basic usage

## Gradle

	repositories {
		maven {
			url  "http://dl.bintray.com/wellnerbou-polopoly/maven"
		}
	}

## Maven

Add this to your repositories (or include it in your own nexus/artifactory):

	<repositories>
		<repository>
			<id>bintray-paulwellnerbou-maven</id>
			<name>bintray</name>
			<url>http://dl.bintray.com/wellnerbou-polopoly/maven</url>
		</repository>
	</repositories>

### Mocking a policy with a default constructor

The simplest way to created a mocked policy is, for example:

    PolicyCMServer policyCMServer = mock(PolicyCMServer.class);
    YourArticlePolicy articlePolicy = new MockPolicyBuilder<>(YourArticlePolicy.class, policyCMServer).withMajor(1).build();

All methods as <code>articlePolicy.getContentId()</code> will work. The <code>PolicyCmServer</code> needs to be a mock instance. It will be configured
automatically to return the policy if you call <code>policyCmServer.getPolicy()</code> with the corresponding versioned id or content id. <code>contentExists</code>
and <code>getContent</code> will be mocked as well.

### Mocking content of the model behind the policy

<code>MockPolicyBuilder</code> is creating a new mocked <code>Content</code> to initialize the policy. If you want to configure this content mock to add data, you can create it
on your own and give it to the builder later.

    Content content = mock(Content.class);
    when(content.getContentList("contentListName")).thenReturn(myContentList); // Just an example, you can add content lists using .withContentList(...) easier.
    YourArticlePolicy articlePolicy = new MockPolicyBuilder<>(YourArticlePolicy.class, policyCMServer).withMajor(1).withContent(content).build();

This content mock is also used internally to mock the calls to <code>getContentId()</code> and <code>getName()</code>.

### Mocking child policy values

<code>MockPolicyBuilder</code> uses partial mocking based on Mockito's <code>spy()</code> to mock the child policies. It would be possible to mock the complete
Polopoly behaviour with child policies as well, but that requires knowledge of classes and implementation of code not marked as <code>@PublicApi</code>, which I try
to avoid and it would bring an huge overhead of code with it.

Assuming the input template of the model of the policy, we are working with, has a dropdown (select box) named "articleType", which should have "type1" as selected value:

    YourArticlePolicy articlePolicy = new MockPolicyBuilder<>(YourArticlePolicy.class, policyCMServer).withMajor(1)
        .withSingleValuedChildPolicyValue("articleType", "type1", SelectPolicy.class)
        .build();

### Policies without public default constructor

If the policy you want to mock does not have a public default constructor you can use the InstanceCreator interface.

    YourArticlePolicy articlePolicy = new MockPolicyBuilder<>(new InstanceCreator<YourArticlePolicy>() {
            @Override
            public YourArticlePolicy instantiate() {
                return new YourArticlePolicy(parameterYouNeedForInstantiation);
            }
        }, policyCMServer).build();

## Building this project

You will need the polopoly jar from Atex' public maven repositories, so you have to put your credentials for this repository in your <code>~/.gradle/gradle.properties</code>:

    polopolyRepoUser=<YOUR POLOPOLY SUPPORT USER>
    polopolyRepoPassword=<YOUR POLOPOLY SUPPORT PASSWORD>

After that, you can run <code>./gradlew build</code> to build the project.

## Deploying artifact in your own maven repository (nexus, for example)

The build.gradle uses [Gradle's publishing plugin](http://www.gradle.org/docs/current/userguide/publishing_maven.html). You will have to give the url
and credentials for your repository.

    ./gradlew publish -Prepo="http://nexus.example.com:8081/nexus/content/repositories/repo-name" -PpublishUser=user -PpublishPassword=password
