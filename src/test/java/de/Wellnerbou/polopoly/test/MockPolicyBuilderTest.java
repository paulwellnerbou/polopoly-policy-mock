package de.Wellnerbou.polopoly.test;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ArticlePolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PolicyImplBase;
import com.polopoly.siteengine.layout.slot.SlotPolicy;
import com.polopoly.siteengine.structure.PagePolicy;
import de.wellnerbou.polopoly.test.InstanceCreator;
import de.wellnerbou.polopoly.test.MockPolicyBuilder;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class MockPolicyBuilderTest {

	final PolicyCMServer policyCMServer = Mockito.mock(PolicyCMServer.class);

	@Test
	public void testPolicyMock() throws CMException {
		final PolicyImplBase policyImplBase = new MockPolicyBuilder<>(PolicyImplBase.class, policyCMServer).build();
		assertThat(policyCMServer.getPolicy(policyImplBase.getContentId())).isEqualTo(policyImplBase);
		assertThat(policyCMServer.getPolicy(policyImplBase.getContentId().getContentId())).isEqualTo(policyImplBase);
		assertThat(policyCMServer.contentExists(policyImplBase.getContentId())).isTrue();
	}

	@Test
	public void testAutomaticMinorVersionCreation() {
		final PolicyImplBase policyImplBase1 = new MockPolicyBuilder<>(PolicyImplBase.class, policyCMServer).withMajor(1).build();
		final PolicyImplBase policyImplBase2 = new MockPolicyBuilder<>(PolicyImplBase.class, policyCMServer).withMajor(1).build();
		assertThat(policyImplBase1.getContentId().getMinor() == policyImplBase2.getContentId().getMinor() - 1);
	}

	@Test
	public void testAddSlot() throws CMException {
		final ArticlePolicy articlePolicy1 = new MockPolicyBuilder<>(ArticlePolicy.class, policyCMServer).build();
		final ArticlePolicy articlePolicy2 = new MockPolicyBuilder<>(ArticlePolicy.class, policyCMServer).build();
		final String slotName = "pageLayout/selected/mySlot";
		final PagePolicy pagePolicy = new MockPolicyBuilder<>(PagePolicy.class, policyCMServer)
				.withSlot(slotName, articlePolicy1, articlePolicy2)
				.build();
		final SlotPolicy slotPolicy = (SlotPolicy) pagePolicy.getChildPolicy(slotName);
		final ContentList elements = slotPolicy.getElements();

		assertThat(elements.getEntry(0).getReferredContentId()).isEqualTo(articlePolicy1.getContentId());
		assertThat(elements.getEntry(1).getReferredContentId()).isEqualTo(articlePolicy2.getContentId());
	}

	@Test
	public void createPolicyWithCustomInstantiator() {
		YourArticlePolicy articlePolicy = new MockPolicyBuilder<>(new InstanceCreator<YourArticlePolicy>() {
			@Override
			public YourArticlePolicy instantiate() {
				return new YourArticlePolicy(true);
			}
		}, policyCMServer).build();
		assertThat(articlePolicy.isConstructorParameterForTest()).isTrue();
	}

	private class YourArticlePolicy extends PolicyImplBase {
		private boolean constructorParameterForTest;

		public YourArticlePolicy(boolean constructorParameterForTest) {
			this.constructorParameterForTest = constructorParameterForTest;
		}

		public boolean isConstructorParameterForTest() {
			return constructorParameterForTest;
		}
	}
}
