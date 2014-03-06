package net.eunjae.android.modelmapper;

import net.eunjae.android.modelmapper.annotation.JsonProperty;
import net.eunjae.android.modelmapper.annotation.JsonResponse;
import org.json.JSONException;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ModelMapperSimpleTest {

	private String json = "{\n" +
			"  \"album\": {\n" +
			"    \"albumno\": 123,\n" +
			"    \"albumname\": \"Album Name\"\n" +
			"  }\n" +
			"}";

	@Test
	public void testIfSimpleMappingWorks() throws IllegalAccessException, JSONException, InstantiationException {
		Album album = (Album) ModelMapper.getInstance().generate(Album.class, json);
		assertNotNull(album);
		assertEquals(123, album.getAlbumNo());
		assertEquals("Album Name", album.getAlbumName());
	}

	@JsonResponse(path = "album")
	public static class Album {
		@JsonProperty("albumno")
		private long albumNo;

		@JsonProperty("albumname")
		private String albumName;

		private long getAlbumNo() {
			return albumNo;
		}

		private String getAlbumName() {
			return albumName;
		}
	}
}
