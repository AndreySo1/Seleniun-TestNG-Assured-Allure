package api;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import api.colors.ColorsData;
import api.registration.Register;
import api.registration.SuccessReg;
import api.registration.UnSuccessReg;
import api.spec.Specification;
import api.users.UserData;
import api.users.UserTime;
import api.users.UserTimeResponse;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class ReqresTest {
   private final static String URL = "https://reqres.in/";
   
   @Test
   public void checkAvatarIdTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOk200()); // for short; info in class Specification

      List<UserData> users = given()
         .when()
         // .contentType(ContentType.JSON) // if we  not use Specification
         // .get(URL + "api/users?page=2") // if we  not use Specification
         .get("api/users?page=2")
         .then().log().all()
         .extract().body().jsonPath().getList("data" , UserData.class);
   
      Assert.assertTrue(users.stream().allMatch(x->x.getEmail().endsWith("@reqres.in")));   

      users.forEach(x->Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));
      //anolog that line up
      List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
      List<String> ids = users.stream().map(x->x.getId().toString()).collect(Collectors.toList());
      for(int i =0; i<avatars.size(); i++){
        Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
      }
   }

   @Test
   public void successRegTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOk200()); 

      Integer expectedId = 4;
      String expectedToken = "QpwL5tke4Pnpja7X4";
      Register expectedUser = new Register("eve.holt@reqres.in", "pistol");

      SuccessReg successReg = given()
         .body(expectedUser)
         .when()
         .post("api/register")
         .then().log().all()
         .extract().as(SuccessReg.class);

      Assert.assertNotNull(successReg.getId());
      Assert.assertNotNull(successReg.getToken());

      Assert.assertEquals(successReg.getId(), expectedId);
      Assert.assertEquals(successReg.getToken(), expectedToken);
   }

   @Test
   public void unSuccessRegTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400()); 

      Register expectedUser = new Register("sydney@fife", "");

      UnSuccessReg unSuccessReg = given()
         .when()
         .body(expectedUser)
         .post("api/register")
         .then().log().all()
         .extract().as(UnSuccessReg.class);

      Assert.assertEquals(unSuccessReg.getError(), "Missing password");
   }

   @Test
   public void sortedYearsTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOk200()); 

      List<ColorsData> colors = given()
         .when()
         .get("api/unknown")
         .then().log().all()
         .extract().body().jsonPath().getList("data", ColorsData.class);

      List<Integer> years = colors.stream().map(ColorsData::getYear).collect(Collectors.toList());
      List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());

      Assert.assertEquals(years, sortedYears);
      // int i=0;
   }

   @Test
   public void deleteUserTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecUnique(204)); 

      given()
         .when()
         .delete("api/users/2")
         .then().log().all();
   }

   @Test
   public void timeTest() {
      Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOk200());
      
      UserTime user = new UserTime("morpheus", "zion resident");

      UserTimeResponse response = given()
         .body(user)
         .when()
         .put("api/users/2")
         .then().log().all()
         .extract().as(UserTimeResponse.class);
      
         String regex = "(.{6})$";
         String currentTime = Clock.systemUTC().instant().toString().replaceAll("(.{12})$", ""); //replace last 12 symbol in time

         Assert.assertEquals(response.getUpdatedAt().replaceAll(regex, ""), currentTime);
   }

}
