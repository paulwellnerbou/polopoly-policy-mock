package de.wellnerbou.polopoly.test;

import com.polopoly.cm.ContentFileInfo;
import com.polopoly.cm.client.Content;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Paul Wellner Bou <paul@wellnerbou.de>
 */
public class ContentFactory {

	public static Content createContentMock() {
		Content content = mock(Content.class);
		try {
			when(content.listFiles(anyString(), anyBoolean())).thenReturn(new ContentFileInfo[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
}
