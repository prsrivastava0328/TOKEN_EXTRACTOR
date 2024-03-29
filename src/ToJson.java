import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ToJson {
	static ArrayList<String> menuItems = new ArrayList<>();

	public static  void main(String... cmdLineArguments) throws IOException, ParseException {
		Map<String, String> hashMap = new HashMap<>();
		for (String menuItem : cmdLineArguments)
			menuItems.add(menuItem);
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new java.io.FileReader("schemaDetails.json"));
		JSONObject jsonObject = (JSONObject) obj;
		System.setProperty("webdriver.chrome.driver",
				System.getProperty("user.dir") + "//drivers//chrome//chromedriver");
		WebDriver driver = new ChromeDriver();
		try {
			driver.manage().window().maximize();
			WebDriverWait driverWait = new WebDriverWait(driver, 200);
			driver.get(jsonObject.get("SCHEMA_URL").toString());
			FileWriter fileWriter = new FileWriter("TokenData_" + jsonObject.get("APPLICATION_NAME") + ".json");
			driverWait.until(ExpectedConditions
					.presenceOfAllElementsLocatedBy(By.xpath("//input[@value='Login'] | //input[@alt='submit'] ")));
			driverWait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//input[@value='Login'] | //input[@alt='submit'] ")));
			driver.findElement(By.xpath("//input[@name='userName']")).sendKeys(jsonObject.get("USER_NAME").toString());
			driver.findElement(By.xpath("//input[@name='password']")).sendKeys(jsonObject.get("PASSWORD").toString());
			driver.findElement(By.xpath("//input[@value='Login'] | //input[@alt='submit'] ")).click();
			driverWait.until(ExpectedConditions
					.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@id,'loading_layer')]")));
			driverWait.until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@id,'loading_layer')]")));
			driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
					By.xpath("//div[contains(@id,'loading_layer') and contains(@style,'display: none')]")));
			driverWait.until(
					ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(@id,'loading_layer')]")));
			String pageSource = driver.getPageSource();
			Pattern p = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
			Matcher m = p.matcher(pageSource);
			String url = null;
			while (m.find()) {
				url = m.group(1); // this variable should contain the link URL
				if (url.contains("navToken") && !url.contains("target=")) {
					String tokenData = ToJson.tokenFetcher(url.replace("\"", "").replace("?navMethod=GET", "")
							.replace("navAction=", "").replace("navUsr=", "").replace("navToken=", ""), m.group(2));
					if (tokenData != null)
						hashMap.put(tokenData.split(",")[1], tokenData.split(",")[0]);
				}
			}
			jsonObject.clear();
			jsonObject.putAll(hashMap);
			fileWriter.write(jsonObject.toJSONString());
			fileWriter.close();
		} finally {
			driver.close();
		}
	}

	public static String tokenFetcher(String url, String menuItemName) throws FileNotFoundException {
		if (menuItems.contains(menuItemName)) {
			StringBuilder string = new StringBuilder();
			String aray[] = url.replace("&amp", "").split("/")[7].split(";");
			string.append(aray[3]).append(",");
			string.append(menuItemName + ",");
			string.append("\n");
			return string.toString();
		} else if (menuItems.isEmpty()) {
			StringBuilder string = new StringBuilder();
			String aray[] = url.replace("&amp", "").split("/")[7].split(";");
			string.append(aray[3]).append(",");
			string.append(menuItemName + ",");
			string.append("\n");
			return string.toString();
		}
		return null;

	}
}
