package batchregister;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.liangzhi.commons.domain.User;
import com.liangzhi.commons.domain.UserCredentials;
import com.liangzhi.commons.domain.UserGrade;
import com.liangzhi.commons.domain.UserRegistration;
import com.liangzhi.commons.domain.UserType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class AnotherSimpleBatchRegister {
    
    private static final String PASS = "coreapi!123";

    public static void main(String[] args) throws Exception {
        for (int i = 435; i <= 445; i++) {
            String prefix = "2017yingcai";
            //String realNamePrefix = "è‹±æ‰�é¢„å¤‡ç�­";
            String username = prefix + String.format("%03d", i);
            String nameStr = prefix + String.format("%03d", i);
            UserRegistration registration = new UserRegistration();
            registration.setUsername(username);
            registration.setBasicPassword("stem123");
            registration.setType(UserType.REGULAR);
            registration.setRealName("备用账号" + nameStr);
            registration.setPhoneNumber(username);
            registration.setNationalId(nameStr);
            registration.setGrade(UserGrade.HIGH_SCHOOL);
            registration.setCity("上海市");
            registration.setProvince("上海市");
            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter("root", PASS));
            client.setConnectTimeout(60000);
            client.setReadTimeout(60000);
            WebResource webResource = null;
            ClientResponse response = null;
            System.out.println(registration.toString());
            // Get user
            User user;
            webResource = client
                    .resource("http://121.40.132.224:8080/users").path("findByUsername").queryParam("username", username);
            response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if (response.getStatus() == 404) {
                webResource = client.resource("http://121.40.132.224:8080/users/register");
                response = webResource.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                        .post(ClientResponse.class, registration);
                if (response.getStatus() != 200) {
                    throw new Exception("response is not 200");
                }
                System.out.println("Registering new user " + registration.toString());
            } else if (response.getStatus() == 200) {
                user = response.getEntity(User.class);
                BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
                String encryptedPassword = passwordEncryptor.encryptPassword("stem123");
                user.setBasicPassword(encryptedPassword);
                user.setType(UserType.REGULAR);
                user.setRealName(nameStr);
                user.setPhoneNumber(username);
                user.setNationalId(nameStr);
                user.setUsername(user.getUsername().trim());
                webResource = client.resource("http://121.40.132.224:8080/users");
                response = webResource.path(user.getId().toString()).type(MediaType.APPLICATION_JSON)
                        .put(ClientResponse.class, user);
                if (response.getStatus() != 200) {
                    throw new Exception("response is not 200");
                }
                System.out.println("Updating exising user " + registration.toString());
            }
            // Verify login
            webResource = client.resource("http://121.40.132.224:8080/users/login");
            UserCredentials credz = new UserCredentials(username, "stem123");
            response = webResource.type(MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class, credz);
            if (response.getStatus() == 200) {
                System.out.println("Login successful for user=" + registration.toString());
            } else {
                throw new Exception("Login failed for user=" + registration.toString());
            }
         }
    }

}
