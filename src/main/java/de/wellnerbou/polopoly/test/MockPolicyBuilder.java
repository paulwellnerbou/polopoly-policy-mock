package de.wellnerbou.polopoly.test;

import com.google.common.collect.Lists;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.ContentListWrapperPolicy;
import com.polopoly.cm.app.policy.SelectPolicy;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.client.impl.VersionInfoImpl;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListSimple;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.layout.slot.SlotPolicy;
import com.polopoly.user.server.UserId;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public class MockPolicyBuilder<T extends Policy> {

	public static final int CHILD_POLICY_MAJOR = 14;
	public static final String POLICY_NAME_OF_NAME_CHILD_POLICY = "nameField";
	public static final String P_TEXT_INPUT = "p.TextInput";
	private static int minorCounter = 1;

	private final InstanceCreator<T> instanceCreator;
	private VersionedContentId parentContentId = new VersionedContentId(2, 999, 999);
	private Content content = ContentFactory.createContentMock();

	private PolicyCMServer policyCmServer = mock(PolicyCMServer.class);
	private int major = 1;

	private String inputTemplateExternalId = "de.wellnerbou.defaultInputTemplateName";
	private int minor = MockPolicyBuilder.minorCounter++;
	private String policyName = "TestPolicy#" + minor;
	private Map<String, Policy> childPolicies = new LinkedHashMap<>();
	private Map<String, ContentList> contentLists = new LinkedHashMap<>();
	private Map<ComponentIdentifier, String> components = new HashMap<>();
	private String externalContentIdString;
	private InputTemplate inputTemplateInstance;
	private boolean committed = false;

	private class ComponentIdentifier {
		String componentGroupName;
		String componentName;

		public ComponentIdentifier(final String componentGroupName, final String componentName) {
			this.componentGroupName = componentGroupName;
			this.componentName = componentName;
		}
	}

	public MockPolicyBuilder(final Class<T> policyClass, final PolicyCMServer mockedPolicyCmServer) {
		this(new InstanceFromClassCreator<T>(policyClass), mockedPolicyCmServer);
	}

	public MockPolicyBuilder(final InstanceCreator<T> instanceCreator, final PolicyCMServer policyCMServer) {
		this.instanceCreator = instanceCreator;
		this.policyCmServer = policyCMServer;
	}

	public MockPolicyBuilder<T> withParent(VersionedContentId parentContentId) {
		this.parentContentId = parentContentId;
		return this;
	}

	public MockPolicyBuilder<T> withContent(final Content mockedContent) {
		this.content = mockedContent;
		return this;
	}

	public MockPolicyBuilder<T> withContentList(final String modelPath, final ContentId... contentListContents) {
		final List<ContentId> contentIds = Arrays.asList(contentListContents);
		final ContentListSimple contentListSimple = new ContentListSimple(contentIds, modelPath);
		contentLists.put(modelPath, ContentListUtil.unmodifiableContentList(contentListSimple));
		return this;
	}

	public MockPolicyBuilder<T> withMajor(int major) {
		this.major = major;
		return this;
	}

	public MockPolicyBuilder<T> withExternalContentIdString(String externalContentIdString) {
		this.externalContentIdString = externalContentIdString;
		return this;
	}

	/**
	 * This is the internal name used to initialize the policy, not the name retrieved by {@link ContentPolicy#getName()} of {@link ContentPolicy}.
	 *
	 * @param name
	 * @return MockPolicyBuilder
	 */
	public MockPolicyBuilder<T> withInternalPolicyName(String name) {
		this.policyName = name;
		return this;
	}

	public MockPolicyBuilder<T> withChildPolicy(final String childPolicyName, final Policy childPolicy) {
		childPolicies.put(childPolicyName, childPolicy);
		return this;
	}

	public MockPolicyBuilder<T> withInputTemplateExternalId(final String inputTemplateExternalId) {
		this.inputTemplateExternalId = inputTemplateExternalId;
		return this;
	}

	public MockPolicyBuilder<T> withInputTemplate(final InputTemplate inputTemplate) {
		this.inputTemplateInstance = inputTemplate;
		return this;
	}

	public MockPolicyBuilder<T> withSlot(final String slotName, final ContentId... contentListContents) {
		return withSlot(slotName, new InstanceFromClassCreator<>(SlotPolicy.class), contentListContents);
	}

	public MockPolicyBuilder<T> withSlot(final String slotName, final Policy... contentListContents) {
		return withSlot(slotName, new InstanceFromClassCreator<>(SlotPolicy.class), contentListContents);
	}

	public MockPolicyBuilder<T> withSlot(final String slotName, final InstanceCreator<? extends SlotPolicy> policyInstanceCreator, final ContentId... contentListContents) {
		final ContentListWrapperPolicy slotElementsPolicy = new MockPolicyBuilder<>(ContentListWrapperPolicy.class, policyCmServer)
				.withContentList("default", contentListContents)
				.build();
		final SlotPolicy slotPolicy = new MockPolicyBuilder<>(policyInstanceCreator, policyCmServer)
				.withChildPolicy("slotElements", slotElementsPolicy)
				.build();
		this.withChildPolicy(slotName, slotPolicy);
		return this;
	}

	public MockPolicyBuilder<T> withSlot(final String slotName, final InstanceCreator<? extends SlotPolicy> policyInstanceCreator, final Policy... contentListContents) {
		return withSlot(slotName, policyInstanceCreator, Arrays.asList(contentListContents).stream().map(input -> input.getContentId()).toArray(ContentId[]::new));
	}

	public MockPolicyBuilder<T> withSingleValuedChildPolicyValue(final String childPolicyName, final String childPolicyValue, final InstanceCreator<? extends SingleValuePolicy> childPolicyInstanceCreator) {
		return this.withSingleValuedChildPolicyValue(childPolicyName, childPolicyValue, P_TEXT_INPUT, childPolicyInstanceCreator);
	}

	public MockPolicyBuilder<T> withSingleValuedChildPolicyValue(final String childPolicyName, final String childPolicyValue, final String childPolicyInputTemplateExternalId, final InstanceCreator<? extends SingleValuePolicy> childPolicyInstanceCreator) {
		final Content childPolicyContent = ContentFactory.createContentMock();
		final InputTemplate inputTemplate = InputTemplateFactory.createInputTemplateMock(childPolicyName, childPolicyInputTemplateExternalId);
		final Policy childPolicy = new MockPolicyBuilder<>(childPolicyInstanceCreator, policyCmServer)
				.withMajor(CHILD_POLICY_MAJOR).withContent(childPolicyContent)
				.withInputTemplate(inputTemplate).withInternalPolicyName(childPolicyName).build();
		try {
			when(childPolicyContent.getComponent(childPolicyName, getChildPolicyValueModelPath(childPolicy))).thenReturn(childPolicyValue);
		} catch (CMException e) {
			throw new RuntimeException(e);
		}
		return withChildPolicy(childPolicyName, childPolicy);
	}

	public MockPolicyBuilder<T> withNameChildPolicy(final String name) {
		return this.withComponent("polopoly.Content", "name", name)
				.withSingleValuedChildPolicyValue(POLICY_NAME_OF_NAME_CHILD_POLICY, name, "p.ContentName",
						new InstanceFromClassCreator<>(SingleValuePolicy.class));
	}

	public MockPolicyBuilder<T> withComponent(final String componentGroupName, final String componentName, final String componentValue) {
		components.put(new ComponentIdentifier(componentGroupName, componentName), componentValue);
		return this;
	}

	private String getChildPolicyValueModelPath(Policy childPolicy) {
		if (childPolicy instanceof SelectPolicy) {
			return "selected_0";
		} else if (childPolicy instanceof SelectableSubFieldPolicy) {
			return "subField";
		}
		return "value";
	}

	public MockPolicyBuilder<T> withSingleValuedChildPolicyValue(final String childPolicyName, final String childPolicyValue) {
		return withChildPolicyValue(childPolicyName, childPolicyValue, SingleValuePolicy.class);
	}

	public MockPolicyBuilder<T> withChildPolicyValue(final String childPolicyName, final String childPolicyValue, final Class<? extends Policy> childPolicyClass) {
		return withSingleValuedChildPolicyValue(childPolicyName, childPolicyValue, new InstanceFromClassCreator(childPolicyClass));
	}

	public MockPolicyBuilder<T> withCommittedVersion() {
		this.committed = true;
		return this;
	}

	public T build() {
		final VersionedContentId versionedContentId = new VersionedContentId(major, minor, getTimestamp());
		return build(versionedContentId);
	}

	public T build(int major, int minor) {
		final VersionedContentId versionedContentId = new VersionedContentId(major, minor, getTimestamp());
		return build(versionedContentId);
	}

	private int getTimestamp() {
		return new BigDecimal(System.currentTimeMillis() / 1000).intValueExact();
	}

	public T build(VersionedContentId versionedContentId) {
		try {
			T policy = internalBuildWithoutMockigCmServer(versionedContentId);
			persistInMockedCmServer(policy, content);
			return policy;
		} catch (CMException e) {
			throw new RuntimeException(e);
		}
	}

	public T buildNewVersionFor(T currentVersionOfPolicy) {
		final VersionedContentId currentVersionedContentId = currentVersionOfPolicy.getContentId();
		int version = getTimestamp();
		if(currentVersionedContentId.getVersion() >= version) {
			version++;
		}
		final VersionedContentId newVersionedContentId = new VersionedContentId(currentVersionedContentId.getMajor(), currentVersionedContentId.getMinor(), version);
		return build(newVersionedContentId);
	}

	private T internalBuildWithoutMockigCmServer(final VersionedContentId versionedContentId) throws CMException {
		when(content.getContentId()).thenReturn(versionedContentId);
		when(content.getSecurityParentId()).thenReturn(parentContentId);
		InputTemplate inputTemplate = getInputTemplate();

		T policy = spy(instanceCreator.instantiate());
		when(content.getName()).thenReturn(this.policyName);
		if (externalContentIdString != null) {
			final ExternalContentId externalContentId = new ExternalContentId(versionedContentId.getMajor(), versionedContentId.getMinor(), versionedContentId.getVersion(), externalContentIdString);
			when(content.getExternalId()).thenReturn(externalContentId);
		}
		for (Map.Entry<String, ContentList> entry : contentLists.entrySet()) {
			if (entry.getKey().equals("default")) {
				when(content.getContentList()).thenReturn(entry.getValue());
			}
			when(content.getContentList(entry.getKey())).thenReturn(entry.getValue());
			for (int i = 0; i < entry.getValue().size(); i++) {
				when(content.getContentReference(entry.getKey(), String.valueOf(i))).thenReturn(entry.getValue().getEntry(i).getReferredContentId());
			}
		}
		when(content.getAvailableContentListNames()).thenReturn(contentLists.keySet().toArray(new String[0]));
		mockComponentNames(components, content);

		when(content.getName()).thenAnswer((Answer<String>) invocation -> ((Content) invocation.getMock()).getComponent("polopoly.Content", "name"));

		initPolicy(policy, content, policyCmServer, inputTemplate);
		for (Map.Entry<String, Policy> entry : childPolicies.entrySet()) {
			final Policy childPolicy = entry.getValue();
			doReturn(childPolicy).when(policy).getChildPolicy(entry.getKey());
		}
		doReturn(Lists.newArrayList(childPolicies.keySet())).when(policy).getChildPolicyNames();

		final VersionInfo versionInfo = createVersionInfo(versionedContentId, committed ? System.currentTimeMillis() : -1L);
		when(content.getVersionInfo()).thenReturn(versionInfo);

		persistInMockedCmServer(policy, content);
		mockRemove(content);
		return policy;
	}

	private void mockRemove(final Content content) throws CMException {
		when(content.remove()).thenAnswer(invocationOnMock -> {
			removeContentVersionFromCmServer(content.getContentId());

			final ContentId unversionedContentId = content.getContentId().getContentId();
			if(policyCmServer.contentExists(unversionedContentId)) {
				final VersionInfo versionInfo = policyCmServer.getContent(unversionedContentId).getVersionInfo();
				if(versionInfo.getVersion() == content.getContentId().getVersion()) {
					if(versionInfo.getPreviousVersion() > 0) {
						final Policy policy = policyCmServer.getPolicy(unversionedContentId);
						persistInMockedCmServer(policy, policy.getContent());
					} else {
						removeContentVersionFromCmServer(unversionedContentId);
					}
				}
			}

			return new VersionedContentId[]{content.getContentId()};
		});
	}

	private void removeContentVersionFromCmServer(final ContentId contentId) throws CMException {
		when(policyCmServer.getPolicy(contentId)).thenThrow(new CMException("Policy " + contentId + " does not exist any more."));
		when(policyCmServer.getContent(contentId)).thenThrow(new CMException("Content " + contentId + " does not exist any more."));
		when(policyCmServer.contentExists(contentId)).thenReturn(false);
	}

	private VersionInfo createVersionInfo(final VersionedContentId versionedContentId, final long committed) throws CMException {
		final UserId sysadminUserId = sysadminUserId();
		final VersionInfoImpl versionInfo = new VersionInfoImpl(versionedContentId.getVersion(),
				0, getPreviousVersionIfExists(versionedContentId),
				committed, sysadminUserId, sysadminUserId);
		return versionInfo;
	}

	private int getPreviousVersionIfExists(final VersionedContentId versionedContentId) throws CMException {
		if(policyCmServer.contentExists(versionedContentId.getContentId())) {
			return policyCmServer.getContent(versionedContentId.getContentId()).getContentId().getVersion();
		} else {
			return -1;
		}
	}

	private UserId sysadminUserId() {
		return new UserId("98");
	}

	private InputTemplate getInputTemplate() {
		if (this.inputTemplateInstance != null) {
			return this.inputTemplateInstance;
		} else {
			return InputTemplateFactory.createInputTemplateMock(this.policyName, this.inputTemplateExternalId);
		}
	}

	private void mockComponentNames(final Map<ComponentIdentifier, String> components, final Content content) throws CMException {
		final ArrayList<String> componentGroupNames = new ArrayList<>();
		for (Map.Entry<ComponentIdentifier, String> entry : components.entrySet()) {
			componentGroupNames.add(entry.getValue());
			when(content.getComponent(entry.getKey().componentGroupName, entry.getKey().componentName)).thenReturn(entry.getValue());
			when(content.getComponentNames(entry.getKey().componentGroupName)).thenReturn(getAllComponentNamesForGroup(entry.getKey().componentGroupName, components));
		}
		when(content.getComponentGroupNames()).thenReturn(componentGroupNames.toArray(new String[0]));
	}

	private String[] getAllComponentNamesForGroup(final String componentGroupName, final Map<ComponentIdentifier, String> components) {
		final ArrayList<String> componentNames = new ArrayList<>();
		for (Map.Entry<ComponentIdentifier, String> entry : components.entrySet()) {
			if (entry.getKey().componentGroupName.equals(componentGroupName)) {
				componentNames.add(entry.getKey().componentName);
			}
		}
		return componentNames.toArray(new String[0]);
	}

	private void persistInMockedCmServer(Policy policy, Content content) throws CMException {
		when(policyCmServer.contentExists(policy.getContentId())).thenReturn(true);
		when(policyCmServer.contentExists(policy.getContentId().getContentId())).thenReturn(true);
		when(policyCmServer.getPolicy(policy.getContentId())).thenReturn(policy);
		when(policyCmServer.getPolicy(policy.getContentId().getContentId())).thenReturn(policy);
		when(policyCmServer.getContent(policy.getContentId())).thenReturn(content);
		when(policyCmServer.getContent(policy.getContentId().getContentId())).thenReturn(content);
		if (content.getExternalId() != null) {
			when(policyCmServer.getPolicy(content.getExternalId())).thenReturn(policy);
			when(policyCmServer.getContent(content.getExternalId())).thenReturn(content);
			final ExternalContentId externalContentIdWithoutVersion = new ExternalContentId(content.getExternalId().getExternalId());
			when(policyCmServer.getPolicy(externalContentIdWithoutVersion)).thenReturn(policy);
			when(policyCmServer.getContent(externalContentIdWithoutVersion)).thenReturn(content);
		}
	}

	private void initPolicy(final Policy policy, final Content content, final PolicyCMServer policyCMServer, final InputTemplate inputTemplate) throws CMException {
		// be sure to have an input template external ID
		if (inputTemplate.getExternalId() == null) {
			when(inputTemplate.getExternalId()).thenReturn(new ExternalContentId("de.wellnerbou.defaultInputTemplateName"));
		}
		policy.init(policyName, new Content[]{content}, inputTemplate, null, policyCMServer);
	}
}
