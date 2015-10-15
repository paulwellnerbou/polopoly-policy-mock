package de.wellnerbou.polopoly.test;

import com.polopoly.cm.policy.Policy;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public class InstanceFromClassCreator<T extends Policy> implements InstanceCreator {

	private final Class<T> policyClass;

	public InstanceFromClassCreator(Class<T> policyClass) {
		this.policyClass = policyClass;
	}

	@Override
	public T instantiate() {
		try {
			return policyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
