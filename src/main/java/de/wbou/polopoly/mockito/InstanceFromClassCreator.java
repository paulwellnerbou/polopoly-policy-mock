package de.wbou.polopoly.mockito;

import com.polopoly.cm.policy.Policy;

/**
 * @author Paul Wellner Bou <pwb@faz.net>
 */
public class InstanceFromClassCreator implements InstanceCreator {

	private final Class<? extends Policy> policyClass;

	public InstanceFromClassCreator(Class<? extends Policy> policyClass) {
		this.policyClass = policyClass;
	}


	@Override
	public com.polopoly.cm.policy.Policy instantiate() throws IllegalAccessException, InstantiationException {
		return policyClass.newInstance();
	}
}
