package de.wellnerbou.polopoly.test;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.InputTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public class InputTemplateFactory {

	public static InputTemplate createInputTemplateMock(final String policyInternalName, final String externalID) {
		final InputTemplate inputTemplate = mock(InputTemplate.class);
		try {
			when(inputTemplate.getExternalId()).thenReturn(new ExternalContentId(externalID));
			when(inputTemplate.getName()).thenReturn(policyInternalName);
		} catch (CMException e) {
			throw new RuntimeException(e);
		}
		return inputTemplate;
	}
}
