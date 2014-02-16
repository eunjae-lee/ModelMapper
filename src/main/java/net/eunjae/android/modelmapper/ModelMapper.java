package net.eunjae.android.modelmapper;

import android.util.Pair;
import net.eunjae.android.modelmapper.annotation.AfterMapping;
import net.eunjae.android.modelmapper.annotation.JsonProperty;
import net.eunjae.android.modelmapper.annotation.JsonResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;

public class ModelMapper {
	private static ModelMapper singletonInstance = null;

	private HashMap<Class<?>, ArrayList<FieldInfo>> fieldInfosMap = new HashMap<Class<?>, ArrayList<FieldInfo>>();
	private HashMap<Class<?>, ArrayList<Method>> callbackMethodsByClass = new HashMap<Class<?>, ArrayList<Method>>();
	private HashMap<Class<?>, ArrayListInfo> arrayListInfoMap = new HashMap<Class<?>, ArrayListInfo>();
	private HashMap<Class<?>, ObjectInfo> objectInfoMap = new HashMap<Class<?>, ObjectInfo>();

	private ModelMapper() {
	}

	public static ModelMapper getInstance() {
		if (singletonInstance == null) {
			synchronized (ModelMapper.class) {
				if (singletonInstance == null) {
					singletonInstance = new ModelMapper();
				}
			}
		}
		return singletonInstance;
	}

	public Object generate(Class<?> clazz, String json) throws IllegalAccessException, JSONException, InstantiationException, IllegalArgumentException {
		if (clazz.equals(ArrayList.class)) {
			throw new IllegalArgumentException("You should put clazz as List_Something.class which is extending ArrayList<Something>. Otherwise use generateList() method.");
		}
		if (clazz.getSuperclass().equals(ArrayList.class)) {
			Class<?> listItemClass = (Class) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
			return generateInternal(clazz, getArrayListInfo(listItemClass), json);
		} else {
			return generateInternal(clazz, getObjectInfo(clazz), json);
		}
	}

	private ObjectInfo getObjectInfo(Class<?> clazz) {
		if (objectInfoMap.containsKey(clazz)) {
			return objectInfoMap.get(clazz);
		} else {
			ObjectInfo objectInfo = new ObjectInfo(clazz);
			objectInfoMap.put(clazz, objectInfo);
			return objectInfo;
		}
	}

	private ArrayListInfo getArrayListInfo(Class<?> clazz) {
		if (arrayListInfoMap.containsKey(clazz)) {
			return arrayListInfoMap.get(clazz);
		} else {
			ArrayListInfo arrayListInfo = new ArrayListInfo(clazz);
			arrayListInfoMap.put(clazz, arrayListInfo);
			return arrayListInfo;
		}
	}

	public Object generateList(Class<?> listItemClass, String json) throws IllegalAccessException, JSONException, InstantiationException {
		ArrayListInfo classInfo = getArrayListInfo(listItemClass);
		return generateInternal(ArrayList.class, classInfo, json);
	}

	private Object generateInternal(Class<?> clazz, ClassInfo classInfo, String json) throws IllegalAccessException, InstantiationException, JSONException {
		if (clazz == null || json == null) {
			return null;
		}

		if (classInfo instanceof ArrayListInfo) {
			Pair<Object, JSONArray> pair = ((ArrayListInfo) classInfo).getTopmostObjectAndLeafArray(json);
			Object topmostObject = pair.first;
			JSONArray leafArray = pair.second;
			if (leafArray == null) {
				return null;
			}

			ArrayList instance = (ArrayList) clazz.newInstance();
			for (int i = 0; i < leafArray.length(); i++) {
				JSONObject jsonItem = leafArray.optJSONObject(i);
				Object item = generateInternal(((ArrayListInfo) classInfo).listItemClass, jsonItem);
				item = invokeCallbackMethod(((ArrayListInfo) classInfo).listItemClass, item, jsonItem);
				instance.add(item);
			}

			instance = (ArrayList) invokeCallbackMethod(((ArrayListInfo) classInfo).listItemClass, instance, topmostObject);
			return instance;
		} else {
			Pair<Object, JSONObject> pair = ((ObjectInfo) classInfo).getTopmostObjectAndLeafObject(json);
			Object topmostObject = pair.first;
			JSONObject leafObject = pair.second;
			Object instance = generateInternal(clazz, leafObject);
			instance = invokeCallbackMethod(clazz, instance, topmostObject);
			return instance;
		}
	}

	private Object invokeCallbackMethod(Class<?> clazz, Object instance, Object data) {
		ArrayList<Method> methods = getCallbackMethodsRecursively(clazz);
		for (Method method : methods) {
			try {
				Class<?>[] parameterTypes = method.getParameterTypes();
				boolean twoParameters = parameterTypes.length == 2;
				boolean firstIsInstance = parameterTypes[0].equals(instance.getClass()) ||
                        parameterTypes[0].isAssignableFrom(instance.getClass()) ||
						(isExtendingArrayList(instance.getClass()) && isExtendingArrayList(parameterTypes[0]));

				if (twoParameters && firstIsInstance) {// && secondIsJsonArrayOrObject) {
					instance = method.invoke(null, instance, data);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	private ArrayList<FieldInfo> getFieldsInClass(Class<?> clazz) {
		if (fieldInfosMap.containsKey(clazz)) {
			return fieldInfosMap.get(clazz);
		}

		ArrayList<FieldInfo> fields = new ArrayList<FieldInfo>();
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field field : declaredFields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			fields.add(new FieldInfo(field));
		}
		fieldInfosMap.put(clazz, fields);
		return fields;
	}

	private Object generateInternal(Class<?> clazz, JSONObject jsonOfModel) throws IllegalAccessException, InstantiationException {
		if (clazz == null || jsonOfModel == null) {
			return null;
		}

		ArrayList<FieldInfo> fieldItems = getFieldsInClass(clazz);

		Object instance = clazz.newInstance();
		for (FieldInfo fieldItem : fieldItems) {
			try {
				setFieldValue(instance, fieldItem, jsonOfModel);
			} catch (Exception e) {
			}
		}
		return instance;
	}

	private ArrayList<Method> getCallbackMethodsRecursively(Class<?> clazz) {
		if (clazz == null || clazz.equals(Class.class)) {
			callbackMethodsByClass.put(clazz, null);
			return null;
		}
		if (callbackMethodsByClass.containsKey(clazz)) {
			return callbackMethodsByClass.get(clazz);
		}
		ArrayList<Method> result = new ArrayList<Method>();
		Method[] methods = clazz.getDeclaredMethods();
		if (methods == null) {
			callbackMethodsByClass.put(clazz, null);
			return null;
		}
		for (Method method : methods) {
			AfterMapping afterMapping = method.getAnnotation(AfterMapping.class);
			if (afterMapping != null && Modifier.isStatic(method.getModifiers())) {
				result.add(method);
			}
		}
		ArrayList<Method> methodsFromSuperClass = getCallbackMethodsRecursively(clazz.getSuperclass());
		if (methodsFromSuperClass != null) {
			result.addAll(methodsFromSuperClass);
		}
		callbackMethodsByClass.put(clazz, result);
		return result;
	}

	private void setFieldValue(Object instance, FieldInfo fieldItem, JSONObject jsonOfModel) throws InstantiationException, IllegalAccessException, JSONException {
		Field field = fieldItem.field;
		Class<?> fieldType = field.getType();

		if (fieldItem.isArrayList) {
			ArrayList fieldValue = null;

			JSONObject leafWrapper = fieldItem.getLeafWrapperObject(jsonOfModel);
			JSONArray leafArray = leafWrapper.optJSONArray(fieldItem.leafPropertyName);
			for (int i = 0; i < leafArray.length(); i++) {
				Object item = generateInternal(fieldItem.listItemType, leafArray.optJSONObject(i));
				if (fieldValue == null) {
					fieldValue = (ArrayList) fieldType.newInstance();
				}
				item = invokeCallbackMethod(fieldItem.listItemType, item, leafArray.optJSONObject(i));
				fieldValue.add(item);
			}

			if (fieldValue != null) {
				fieldValue = (ArrayList) invokeCallbackMethod(fieldItem.listItemType, fieldValue, fieldItem.getTopmostObjectJSON(jsonOfModel));
			}
			field.set(instance, fieldValue);
		} else {
			JSONObject leafWrapper = fieldItem.getLeafWrapperObject(jsonOfModel);
			String propertyName = fieldItem.leafPropertyName;
			boolean jsonHasProperty = leafWrapper.has(propertyName);
			if (!jsonHasProperty) {
				return;
			}

			if (fieldType.equals(Object.class)) {
				// skip object type
			} else if (fieldType.equals(String.class)) {
				field.set(instance, leafWrapper.optString(propertyName));
			} else if (Boolean.TYPE.equals(fieldType)) {
				field.setBoolean(instance, new Boolean(leafWrapper.optBoolean(propertyName)));
			} else if (Byte.TYPE.equals(fieldType)) {
				field.setByte(instance, new Byte((byte) leafWrapper.optInt(propertyName)));
			} else if (Character.TYPE.equals(fieldType)) {
				String str = leafWrapper.optString(propertyName);
				if (str != null && str.length() > 0) {
					field.setChar(instance, str.charAt(0));
				}
			} else if (Double.TYPE.equals(fieldType)) {
				field.setDouble(instance, new Double(leafWrapper.optDouble(propertyName)));
			} else if (Float.TYPE.equals(fieldType)) {
				field.setFloat(instance, new Float((float) leafWrapper.optDouble(propertyName)));
			} else if (Integer.TYPE.equals(fieldType)) {
				field.setInt(instance, new Integer(leafWrapper.optInt(propertyName)));
			} else if (Long.TYPE.equals(fieldType)) {
				field.setLong(instance, new Long(leafWrapper.optLong(propertyName)));
			} else if (Short.TYPE.equals(fieldType)) {
				field.setShort(instance, new Short((short) leafWrapper.optInt(propertyName)));
			} else {
				Object item = generateInternal(fieldItem.fieldType, leafWrapper.optJSONObject(propertyName));
				if (item != null) {
					invokeCallbackMethod(fieldItem.fieldType, item, fieldItem.getTopmostObjectJSON(jsonOfModel));
				}
				field.set(instance, item);
			}
		}
	}

	private static class ClassInfo {
	}

	private static class ObjectInfo extends ClassInfo {
		private final JsonResponse classAnnotation;
		private final String propertyPath;
		private final String[] propertyPathPieces;

		public ObjectInfo(Class<?> clazz) {
			this.classAnnotation = clazz.getAnnotation(JsonResponse.class);
			this.propertyPath = classAnnotation == null ? "" : classAnnotation.path();
			this.propertyPathPieces = propertyPath.split("\\.");
		}

		public Pair<Object, JSONObject> getTopmostObjectAndLeafObject(String json) throws JSONException {
			Object topmostObject;
			JSONObject leafObject;
			if ("".equals(propertyPath)) {
				topmostObject = leafObject = new JSONObject(json);
			} else {
				JSONObject jsonObject = new JSONObject(json);
				topmostObject = jsonObject.get(propertyPathPieces[0]);
				for (String path : propertyPathPieces) {
					jsonObject = jsonObject.optJSONObject(path);
				}
				leafObject = jsonObject;
			}
			return new Pair<Object, JSONObject>(topmostObject, leafObject);
		}
	}

	private static boolean isExtendingArrayList(Class<?> clazz) {
		return AbstractList.class.isAssignableFrom(clazz);
	}

	private static class ArrayListInfo extends ClassInfo {

		private final Class listItemClass;
		private final String propertyPathAsList;

		public ArrayListInfo(Class<?> listItemClass) {
			this.listItemClass = listItemClass;
			JsonResponse listItemAnnotation = listItemClass.getAnnotation(JsonResponse.class);
			propertyPathAsList = listItemAnnotation == null ? "" : listItemAnnotation.pathAsList();
		}

		public Pair<Object, JSONArray> getTopmostObjectAndLeafArray(String json) throws JSONException {
			Object topmostObject;
			JSONArray leafArray = null;
			if ("".equals(propertyPathAsList)) {
				topmostObject = leafArray = new JSONArray(json);
			} else {
				String[] paths = propertyPathAsList.split("\\.");
				JSONObject jsonObject = new JSONObject(json);
				topmostObject = jsonObject.get(paths[0]);
				for (int i = 0; i < paths.length; i++) {
					if (i == paths.length - 1) {
						leafArray = jsonObject.optJSONArray(paths[i]);
					} else {
						jsonObject = jsonObject.optJSONObject(paths[i]);
					}
				}
			}
			return new Pair<Object, JSONArray>(topmostObject, leafArray);
		}
	}

	private static class FieldInfo {
		private final Field field;
		private final String fullPropertyName;
		private final Class<?> fieldType;
		private final boolean isArrayList;
		private final Class listItemType;
		private final String[] propertyNamePieces;
		private final String leafPropertyName;

		public FieldInfo(Field field) {
			this.field = field;
			JsonProperty annotation = field.getAnnotation(JsonProperty.class);
			if (annotation == null) {
				this.fullPropertyName = field.getName();
			} else {
				this.fullPropertyName = annotation.value();
			}
			this.fieldType = field.getType();
			this.isArrayList = isExtendingArrayList(fieldType);
			if (isArrayList) {
				this.listItemType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			} else {
				this.listItemType = null;
			}
			this.propertyNamePieces = fullPropertyName.split("\\.");
			this.leafPropertyName = propertyNamePieces[propertyNamePieces.length - 1];
		}

		public Object getTopmostObjectJSON(JSONObject jsonObject) throws JSONException {
			return jsonObject.get(propertyNamePieces[0]);
		}

		public JSONObject getLeafWrapperObject(JSONObject jsonObject) {
			JSONObject nestedObject = jsonObject;
			for (int i = 0; i < propertyNamePieces.length - 1; i++) {
				nestedObject = nestedObject.optJSONObject(propertyNamePieces[i]);
			}
			return nestedObject;
		}
	}
}