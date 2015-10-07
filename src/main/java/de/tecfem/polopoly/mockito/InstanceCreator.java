package de.tecfem.polopoly.mockito;

import com.polopoly.cm.policy.Policy;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public interface InstanceCreator<T extends Policy> {
	public T instantiate();
}
