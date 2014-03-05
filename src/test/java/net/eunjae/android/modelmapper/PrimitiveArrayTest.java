package net.eunjae.android.modelmapper;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PrimitiveArrayTest {

    /*
        {
            "arr": [1, 2, 3]
        }
     */
    @Test
    public void testIntegerArray() throws IllegalAccessException, JSONException, InstantiationException {
        String json = "{\n" +
                "\t\"arr\": [1, 2, 3]\n" +
                "}";
        Model model = (Model) ModelMapper.newInstance().generate(Model.class, json);
        assertNotNull(model);
        assertNotNull(model.getArr());
        assertEquals(3, model.getArr().size());
        assertEquals(1, (int) model.getArr().get(0));
        assertEquals(2, (int) model.getArr().get(1));
        assertEquals(3, (int) model.getArr().get(2));
    }

    /*
        {
            "arr": [
                [
                    "a", "b"
                ],
                [
                    "c", "d"
                ],
                [
                    "e", "f"
                ]
            ]
        }
     */
    @Test
    public void testNestedStringArray() throws IllegalAccessException, JSONException, InstantiationException {
        String json = "\n" +
                        "{\n" +
                        "\t\"arr\": [\n" +
                        "\t\t[\n" +
                        "\t\t\t\"a\", \"b\"\n" +
                        "\t\t],\n" +
                        "\t\t[\n" +
                        "\t\t\t\"c\", \"d\"\n" +
                        "\t\t],\n" +
                        "\t\t[\n" +
                        "\t\t\t\"e\", \"f\"\n" +
                        "\t\t]\n" +
                        "\t]\n" +
                        "}";
        Model2 model = (Model2) ModelMapper.newInstance().generate(Model2.class, json);
        assertNotNull(model);
        assertNotNull(model.getArr());
        assertEquals(3, model.getArr().size());
        assertEquals("a", model.getArr().get(0).get(0));
        assertEquals("b", model.getArr().get(0).get(1));
        assertEquals("c", model.getArr().get(1).get(0));
        assertEquals("d", model.getArr().get(1).get(1));
        assertEquals("e", model.getArr().get(2).get(0));
        assertEquals("f", model.getArr().get(2).get(1));
    }

    /*
        {
            "arr": [
                [
                    [
                        1, 2
                    ]
                ],
                [
                    [
                        3, 4
                    ]
                ]
            ]
        }
     */
    @Test
    public void testNestedNestedIntegerArray() throws IllegalAccessException, JSONException, InstantiationException {
        String json = "{\n" +
                    "\t\"arr\": [\n" +
                    "\t\t[\n" +
                    "\t\t\t[\n" +
                    "\t\t\t\t1, 2\n" +
                    "\t\t\t]\n" +
                    "\t\t],\n" +
                    "\t\t[\n" +
                    "\t\t\t[\n" +
                    "\t\t\t\t3, 4\n" +
                    "\t\t\t]\n" +
                    "\t\t]\n" +
                    "\t]\n" +
                    "}";
        Model3 model = (Model3) ModelMapper.newInstance().generate(Model3.class, json);
        assertEquals(1, (int) model.getArr().get(0).get(0).get(0));
        assertEquals(2, (int) model.getArr().get(0).get(0).get(1));
        assertEquals(3, (int) model.getArr().get(1).get(0).get(0));
        assertEquals(4, (int) model.getArr().get(1).get(0).get(1));
    }

    /*
        {
            "arr": [
                [
                    [
                        {
                            "name": "Paul"
                        }
                    ]
                ],
                [
                    [
                        {
                            "name": "Jane"
                        }
                    ]
                ]
            ]
        }
     */
    @Test
    public void testNestedNestedObjectArray() throws IllegalAccessException, JSONException, InstantiationException {
        String json = " {\n" +
                "            \"arr\": [\n" +
                "                [\n" +
                "                    [\n" +
                "                        {\n" +
                "                            \"name\": \"Paul\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                ],\n" +
                "                [\n" +
                "                    [\n" +
                "                        {\n" +
                "                            \"name\": \"Jane\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                ]\n" +
                "            ]\n" +
                "        }";
        Model4 model = (Model4) ModelMapper.newInstance().generate(Model4.class, json);
        assertTrue(model.getArr().get(0).get(0).get(0) instanceof Model5);
        assertEquals("Paul", model.getArr().get(0).get(0).get(0).name);
        assertEquals("Jane", model.getArr().get(1).get(0).get(0).name);
    }

    public static class Model {
        ArrayList<Integer> arr;

        public ArrayList<Integer> getArr() {
            return arr;
        }
    }

    public static class Model2 {
        ArrayList<ArrayList<String>> arr;

        public ArrayList<ArrayList<String>> getArr() {
            return arr;
        }
    }

    public static class Model3 {
        ArrayList<ArrayList<ArrayList<Integer>>> arr;

        public ArrayList<ArrayList<ArrayList<Integer>>> getArr() {
            return arr;
        }
    }

    public static class Model4 {
        ArrayList<ArrayList<ArrayList<Model5>>> arr;

        public ArrayList<ArrayList<ArrayList<Model5>>> getArr() {
            return arr;
        }
    }

    public static class Model5 {
        String name;
    }
}
