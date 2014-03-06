package net.eunjae.android.modelmapper;

import net.eunjae.android.modelmapper.annotation.AfterMapping;
import net.eunjae.android.modelmapper.annotation.JsonResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class SimpleCallbackTest {

	public static final String JSON = "{\n" +
			"  \"status\": 200,\n" +
			"  \"data\": {\n" +
			"    \"blocked\": true,\n" +
			"    \"user\": {\n" +
			"      \"name\": \"Eunjae Lee\",\n" +
			"      \"gender\": \"male\"\n" +
			"    }\n" +
			"  }\n" +
			"}";

	@Test
	public void testMappingSuccess() throws IllegalAccessException, JSONException, InstantiationException {
		User user = (User) ModelMapper.getInstance().generate(User.class, JSON);
		assertNotNull(user);
		assertEquals("Eunjae Lee", user.name);
		assertEquals("male", user.gender);
	}

	@Test
	public void afterMappingWorks() throws IllegalAccessException, JSONException, InstantiationException {
		User user = (User) ModelMapper.getInstance().generate(User.class, JSON);
		assertEquals(true, user.blocked);
	}

	@JsonResponse(path = "data.user")
	public static class User {
		String name;
		String gender;
		boolean blocked;

		@AfterMapping
		public static User doSomething(User user, JSONObject object) {
			user.blocked = object.optBoolean("blocked");
			return user;
		}
	}
}
