package api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import api.spec.Specification;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ReqresNoPojoTest {
   private final static String URL ="https://reqres.in/";
   
   @Test
   public void checkAvatarNoPojoTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOk200());

      Response response = given()
         .when()
         .get("api/users?page=2")
         .then().log().all()
         .body("page", equalTo(2))
         .body("data.id", notNullValue())
         .body("data.email", notNullValue())
         .body("data.avatar", notNullValue())
         .extract().response();
      
      JsonPath jsonPath = response.jsonPath();
      List<String> emails = jsonPath.get("data.email");
      List<Integer> ids = jsonPath.get("data.id");
      List<String> avatars = jsonPath.get("data.avatar");

      for(int i=0; i<avatars.size(); i++){
         Assert.assertTrue(avatars.get(i).contains(ids.get(i).toString()));
      } //check that avatarName include id

      Assert.assertTrue(emails.stream().allMatch(x->x.endsWith("@reqres.in"))); // check that all emails ending to "@reqres.in"

   }

   @Test
   public void successUserRegNoPojoTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOk200());

      Map<String, String> user = new HashMap<>();
      user.put("email", "eve.holt@reqres.in");
      user.put("password", "pistol");

      //Example V1
      given()
         .body(user)
         .when()
         .post("api/register")
         .then().log().all()
         .body("id", equalTo(4))
         .body("token", equalTo("QpwL5tke4Pnpja7X4"));

      //Example V2
      Response response = given()
         .body(user)
         .when()
         .post("api/register")
         .then().log().all()
         .extract().response();

         JsonPath jsonPath = response.jsonPath();
         int id = jsonPath.get("id");
         String token = jsonPath.get("token");

         Assert.assertEquals(id, 4);
         Assert.assertEquals(token, "QpwL5tke4Pnpja7X4");
   }

   @Test
   public void unSuccessRegNoPojoTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400()); 

      Map<String, String> user = new HashMap<>();
      user.put("email", "sydney@fife");

      //Example V1
      given()
         .body(user)
         .when()
         .post("api/register")
         .then().log().all()
         .body("error", equalTo("Missing password"));

      //Example V2
      Response response = given()
         .body(user)
         .when()
         .post("api/register")
         .then().log().all()
         .extract().response();

         JsonPath jsonPath = response.jsonPath();
         String error = jsonPath.get("error");
         
         Assert.assertEquals(error, "Missing password");
      
         
   }
}
