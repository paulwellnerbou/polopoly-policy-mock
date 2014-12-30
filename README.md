polopoly-policy-mock
====================

Utilities to mock Atex' Polopoly's Policy classes using the data model objects the policy classes rely on. Basically it relies on the concepts and code examples explained on [http://support.polopoly.com/doc/jar/10.6.1/dev-guide/testing.html].

# Building this project

You will need the polopoly api jar from Atex' public maven repositories, so you have to put your credentials for this repository in your <code>~/.gradle/gradle.properties</code>:

    polopolyRepoUser=<YOUR POLOPOLY SUPPORT USER>
    polopolyRepoPassword=<YOUR POLOPOLY SUPPORT PASSWORD>

After that, you can run <code>./gradlew build</code> to build the project.

# Basic usage

## Mocking a policy with a default constructor

The simplest way to created a mocked policy is, for example:

    PolicyCMServer policyCMServer = mock(PolicyCMServer.class);
    YourArticlePolicy articlePolicy = (YourArticlePolicy) new MockPolicyBuilder(YourArticlePolicy.class, policyCMServer).withMajor(1).build();

All methods as <code>articlePolicy.getContentId()</code> will work. The <code>PolicyCmServer</code> needs to be a mock instance. It will be configured
automatically to return the policy if you call <code>policyCmServer.getPolicy()</code> with the corresponding versioned id or content id. <code>contentExists</code>
and <code>getContent</code> will be mocked as well.

## Mocking content of the model behind the policy

<code>MockPolicyBuilder</code> is creating a new mocked <code>Content</code> to initialize the policy. If you want to configure this content mock to add data, you can create it
on your own and give it to the builder later.

    Content content = mock(Content.class);
    when(content.getContentList("contentListName")).thenReturn(myContentList);
    YourArticlePolicy articlePolicy = (YourArticlePolicy) new MockPolicyBuilder(YourArticlePolicy.class, policyCMServer).withMajor(1).withContent(content).build();

This content mock will be also used to mock the calls to <code>getContentId()</code> and <code>getName()</code>.

## Mocking child policy values

<code>MockPolicyBuilder</code> uses partial mocking based on Mockito's <code>spy()</code> to mock the child policies. It would be possible to mock the complete
Polopoly behaviour with child policies as well, but that requires knowledge of classes and implementation of code not marked as <code>@PublicApi</code>, which I try
to avoid and it would bring an huge overhead of code with it.

Assuming the input template of the model of the policy, we are working with, has a dropdown (select box) named "articleType", which should have "type1" as selected value:

    YourArticlePolicy articlePolicy = (YourArticlePolicy) new MockPolicyBuilder(YourArticlePolicy.class, policyCMServer).withMajor(1)
        .withSingleValuedChildPolicyValue("articleType", "type1", SelectPolicy.class)
        .build();

## Policies without public default constructor

If the policy you want to mock does not have a public default constructor you can use the InstanceCreator interface.

    YourArticlePolicy articlePolicy = (YourArticlePolicy) new MockPolicyBuilder(new InstanceCreator<YourArticlePolicy>() {
            @Override
            public YourArticlePolicy instantiate() {
                return new YourArticlePolicy(parameterYouNeedForInstantiation);
            }
        }, policyCMServer).build();
