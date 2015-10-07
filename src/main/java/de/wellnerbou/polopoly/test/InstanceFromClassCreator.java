package de.wellnerbou.polopoly.test;

import com.polopoly.cm.policy.Policy;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public class InstanceFromClassCreator implements InstanceCreator {

	private final Class<? extends Policy> policyClass;

	public InstanceFromClassCreator(Class<? extends Policy> policyClass) {
		this.policyClass = policyClass;
	}


	@Override
	public Policy instantiate() {
		try {
			return policyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
