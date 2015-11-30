package batchregister;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.liangzhi.commons.domain.User;
import com.liangzhi.commons.domain.UserCredentials;
import com.liangzhi.commons.domain.UserRegistration;
import com.liangzhi.commons.domain.UserType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class BatchRegisterNewUser {

    private static final String PASS = "coreapi!123";

    public static void main(String[] args) throws Exception {
        File myFile = new File("E://liangzhi/huashida.xlsx");
        //BufferedReader fis = new BufferedReader(new InputStreamReader(new FileInputStream(myFile), "UTF8"));
        FileInputStream fis = new FileInputStream(myFile);

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator < Row > rowIterator = mySheet.iterator();
        
        rowIterator.next();

        boolean skip = false;
        // Traversing over each row of XLSX file
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell name = row.getCell(2);
            name.setCellType(Cell.CELL_TYPE_STRING);
            Cell nationalId = row.getCell(3);
            nationalId.setCellType(Cell.CELL_TYPE_STRING);
            Cell phone = row.getCell(8);
            phone.setCellType(Cell.CELL_TYPE_STRING);
            String nameStr = name.getStringCellValue();
            String nationalIdStr = nationalId.getStringCellValue();
            //String phoneStr = "1234567";
            String phoneStr = phone.getStringCellValue();
            if (!nationalIdStr.equals("3101071048220150230") && skip) {
                System.out.println("Skipping to 3101071048220150230");
                continue;
            } else {
                skip = false;
            }
            UserRegistration registration = new UserRegistration();
            registration.setUsername(nationalIdStr);
            registration.setBasicPassword("stem123456");
            registration.setType(UserType.REGULAR);
            registration.setRealName(nameStr);
            registration.setPhoneNumber(phoneStr);
            registration.setNationalId(nationalIdStr);
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
                    .resource("http://www.stemcloud.cn:8080/users").path("findByUsername").queryParam("username", nationalIdStr);
            response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if (response.getStatus() == 404) {
                webResource = client.resource("http://www.stemcloud.cn:8080/users/register");
                response = webResource.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                        .post(ClientResponse.class, registration);
                if (response.getStatus() != 200) {
                    throw new Exception("response is not 200");
                }
                System.out.println("Registering new user " + registration.toString());
            } else if (response.getStatus() == 200) {
                user = response.getEntity(User.class);
                BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
                String encryptedPassword = passwordEncryptor.encryptPassword("stem123456");
                user.setBasicPassword(encryptedPassword);
                user.setType(UserType.REGULAR);
                user.setRealName(nameStr);
                user.setPhoneNumber("1234567");
                user.setNationalId(nationalIdStr);
                user.setUsername(user.getUsername().trim());
                webResource = client.resource("http://www.stemcloud.cn:8080/users");
                response = webResource.type(MediaType.APPLICATION_JSON)
                        .put(ClientResponse.class, user);
                if (response.getStatus() != 200) {
                    throw new Exception("response is not 200");
                }
                System.out.println("Updating exising user " + registration.toString());
            }
            // Verify login
            webResource = client.resource("http://www.stemcloud.cn:8080/users/login");
            UserCredentials credz = new UserCredentials(nationalIdStr, "stem123456");
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