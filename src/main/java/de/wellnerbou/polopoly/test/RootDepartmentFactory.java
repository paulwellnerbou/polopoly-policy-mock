package de.wellnerbou.polopoly.test;

import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.policy.DepartmentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public class RootDepartmentFactory {

	public static DepartmentPolicy create(PolicyCMServer policyCMServer) {
		return new MockPolicyBuilder<DepartmentPolicy>(DepartmentPolicy.class, policyCMServer)
				.withExternalContentIdString("p.RootDepartment")
				.withInputTemplateExternalId("p.DepartmentSystemTemplate").build(2, 0);
	}
}
