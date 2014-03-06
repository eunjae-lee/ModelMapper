package net.eunjae.android.modelmapper;

import net.eunjae.android.modelmapper.annotation.AfterMapping;
import net.eunjae.android.modelmapper.annotation.JsonProperty;
import net.eunjae.android.modelmapper.annotation.JsonResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NestedModelCallbackTest {

	private static final String JSON = "{\n" +
			"  \"status\": 200,\n" +
			"  \"data\": {\n" +
			"    \"nested\": {\n" +
			"      \"blocked\": true,\n" +
			"      \"user\": {\n" +
			"        \"name\": \"Eunjae Lee\",\n" +
			"        \"gender\": \"male\",\n" +
			"        \"location\": {\n" +
			"          \"confirmed\": \"true\",\n" +
			"          \"real_location\": {\n" +
			"            \"city\": \"Seoul\",\n" +
			"            \"name\": \"Daum Communications\"\n" +
			"          }\n" +
			"        }\n" +
			"      } \n" +
			"    }\n" +
			"  }\n" +
			"}";

	private static final String JSON2 = "{\n" +
			"  \"status\": 200,\n" +
			"  \"responsedttm\": 12345678,\n" +
			"  \"users\": {\n" +
			"    \"usercnt\": 3,\n" +
			"    \"real_data\": [\n" +
			"      {\n" +
			"        \"name\": \"Marry Kim\",\n" +
			"        \"gender\": \"female\",\n" +
			"        \"blocked\": false\n" +
			"      },\n" +
			"      {\n" +
			"        \"name\": \"Eunjae Lee\",\n" +
			"        \"gender\": \"male\",\n" +
			"        \"blocked\": true\n" +
			"      },\n" +
			"      {\n" +
			"        \"name\": \"Jeniffer Kim\",\n" +
			"        \"gender\": \"female\",\n" +
			"        \"blocked\": false\n" +
			"      }\n" +
			"    ]\n" +
			"  }\n" +
			"}";

	@Test
	public void testNestedModel() throws IllegalAccessException, JSONException, InstantiationException {
		User user = (User) ModelMapper.getInstance().generate(User.class, JSON);
		assertNotNull(user);
		assertEquals("Eunjae Lee", user.name);
		assertEquals("male", user.gender);
		assertEquals(true, user.blocked);

		assertNotNull(user.location);
		assertEquals("Seoul", user.location.city);
		assertEquals("Daum Communications", user.location.name);
		assertEquals(true, user.location.confirmed);
	}

	@Test
	public void testNestedArray() throws IllegalAccessException, JSONException, InstantiationException {
		ArrayList<User2> users = (ArrayList<User2>) ModelMapper.getInstance().generateList(User2.class, JSON2);
		assertNotNull(users);
		assertEquals(3, users.size());
		for (User2 user : users) {
			assertEquals(true, user.blocked);
			assertEquals(3, user.totalCount);
		}
	}

	@JsonResponse(path = "data.nested.user")
	public static class User {
		String name;
		String gender;
		boolean blocked;

		@JsonProperty("location.real_location")
		Location location;

		@AfterMapping
		public static User getBlockedProperty(User user, JSONObject obj) {
			boolean blocked = obj.optJSONObject("nested").optBoolean("blocked");
			user.blocked = blocked;
			return user;
		}
	}

	public static class Location {
		String city;
		String name;
		boolean confirmed;

		@AfterMapping
		public static Location getConfirmedProperty(Location location, JSONObject obj) {
			boolean confirmed = obj.optBoolean("confirmed");
			location.confirmed = confirmed;
			return location;
		}
	}

	@JsonResponse(pathAsList = "users.real_data")
	public static class User2 {
		String name;
		String gender;
		boolean blocked;
		int totalCount;

		@AfterMapping
		public static User2 getBlocked(User2 user, JSONObject object) {
			user.blocked = true;
			return user;
		}

		@AfterMapping
		public static ArrayList<User2> getUserCnt(ArrayList<User2> users, JSONObject obj) {
			int userCount = obj.optInt("usercnt");
			for (User2 user : users) {
				user.totalCount = userCount;
			}
			return users;
		}
	}
}
