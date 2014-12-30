package de.wbou.polopoly.mockito;

import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Paul Wellner Bou <pwb@faz.net>
 */
public class MockPolicyBuilder {

	public static final int CHILD_POLICY_MAJOR = 14;
	private static int minorCounter = 1;

	private final InstanceCreator instanceCreator;
	private VersionedContentId parentContentId = new VersionedContentId(2, 999, 999);
	private Content content = mock(Content.class);
	private PolicyCMServer policyCmServer = mock(PolicyCMServer.class);
	private int major = 1;

	private InputTemplate inputTemplate = mock(InputTemplate.class);
	private int minor = MockPolicyBuilder.minorCounter++;
	private String policyName = "TestPolicy#" + minor;
	private Map<String, Policy> childPolicies = new LinkedHashMap<>();

	public MockPolicyBuilder(final Class<? extends Policy> policyClass, final PolicyCMServer mockedPolicyCmServer) {
		this(new InstanceFromClassCreator(policyClass), mockedPolicyCmServer);
	}

	public MockPolicyBuilder(final InstanceCreator instanceCreator, final PolicyCMServer policyCMServer) {
		this.instanceCreator = instanceCreator;
		this.policyCmServer = policyCMServer;
	}

	public MockPolicyBuilder withParent(VersionedContentId parentContentId) {
		this.parentContentId = parentContentId;
		return this;
	}

	public MockPolicyBuilder withContent(final Content mockedContent) {
		this.content = mockedContent;
		return this;
	}

	public MockPolicyBuilder withMajor(int major) {
		this.major = major;
		return this;
	}

	public MockPolicyBuilder withName(String name) {
		this.policyName = name;
		return this;
	}

	public MockPolicyBuilder withChildPolicy(final String childPolicyName, final Policy childPolicy) {
		childPolicies.put(childPolicyName, childPolicy);
		return this;
	}

	public MockPolicyBuilder withInputTemplate(final InputTemplate inputTemplate) {
		this.inputTemplate = inputTemplate;
		return this;
	}

	public MockPolicyBuilder withSingleValuedChildPolicyValue(final String childPolicyName, final String childPolicyValue, final InstanceCreator childPolicyInstanceCreator) {
		final Content childPolicyContent = mock(Content.class);
		try {
			when(childPolicyContent.getComponent(childPolicyName, "selected_0")).thenReturn(childPolicyValue);
		} catch (CMException e) {
			throw new RuntimeException(e);
		}
		final Policy childPolicy = new MockPolicyBuilder(childPolicyInstanceCreator, policyCmServer).withMajor(CHILD_POLICY_MAJOR).withContent(childPolicyContent).withName(childPolicyName).build();
		return withChildPolicy(childPolicyName, childPolicy);
	}

	public MockPolicyBuilder withSingleValuedChildPolicyValue(final String childPolicyName, final String childPolicyValue) {
		return withSingleValuedChildPolicyValue(childPolicyName, childPolicyValue, SingleValuePolicy.class);
	}

	public MockPolicyBuilder withSingleValuedChildPolicyValue(final String childPolicyName, final String childPolicyValue, final Class childPolicyClass) {
		return withSingleValuedChildPolicyValue(childPolicyName, childPolicyValue, new InstanceFromClassCreator(childPolicyClass));
	}

	public Policy build() {
		final VersionedContentId versionedContentId = new VersionedContentId(major, minor, new BigDecimal(System.currentTimeMillis() / 1000).intValueExact());
		return build(versionedContentId);
	}

	public Policy build(VersionedContentId versionedContentId) {
		when(content.getContentId()).thenReturn(versionedContentId);
		when(content.getSecurityParentId()).thenReturn(parentContentId);

		Policy policy = spy(instanceCreator.instantiate());
		initPolicy(policy, content, policyCmServer, inputTemplate);

		try {
			for (Map.Entry<String, Policy> entry : childPolicies.entrySet()) {
				doReturn(entry.getValue()).when(policy).getChildPolicy(entry.getKey());
			}
			persistInMockedCmServer(policy, content);
		} catch (CMException e) {
			throw new RuntimeException(e);
		}

		return policy;
	}

	private void persistInMockedCmServer(Policy policy, Content content) throws CMException {
		when(policyCmServer.contentExists(policy.getContentId())).thenReturn(true);
		when(policyCmServer.contentExists(policy.getContentId().getContentId())).thenReturn(true);
		when(policyCmServer.getPolicy(policy.getContentId())).thenReturn(policy);
		when(policyCmServer.getPolicy(policy.getContentId().getContentId())).thenReturn(policy);
		when(policyCmServer.getContent(policy.getContentId())).thenReturn(content);
		when(policyCmServer.getContent(policy.getContentId().getContentId())).thenReturn(content);
	}

	private void initPolicy(final Policy policy, final Content content, final PolicyCMServer policyCMServer, final InputTemplate inputTemplate) {
		policy.init(policyName, new Content[] { content }, inputTemplate, null, policyCMServer);
	}
}
