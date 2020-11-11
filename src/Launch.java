import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

public class Launch {
	public static void main(String... strings) throws FileNotFoundException, IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		StringBuilder builder = new StringBuilder();
		Object obj = jsonParser.parse(new java.io.FileReader("schemaDetails.json"));
		JSONObject jsonObject = (JSONObject) obj;
		PrintWriter printWriter = new PrintWriter(new File(jsonObject.get("APPLICATION_NAME").toString() + ".csv"));
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "//drivers//chrome//chromedriver");
		WebDriver driver = new ChromeDriver();
		try {
			driver.manage().window().maximize();
			WebDriverWait driverWait = new WebDriverWait(driver, 200);
			System.out.println(jsonObject.get("SCHEMA_URL").toString());
			driver.get(jsonObject.get("SCHEMA_URL").toString());
			driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//input[@value='Login'] | //input[@alt='submit'] ")));
			driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@value='Login'] | //input[@alt='submit'] ")));
			driver.findElement(By.xpath("//input[@name='userName']")).sendKeys(jsonObject.get("USER_NAME").toString());
			driver.findElement(By.xpath("//input[@name='password']")).sendKeys(jsonObject.get("PASSWORD").toString());
			driver.findElement(By.xpath("//input[@value='Login'] | //input[@alt='submit'] ")).click();
			driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@id,'loading_layer')]")));
			driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@id,'loading_layer')]")));
			driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
			                        By.xpath("//div[contains(@id,'loading_layer') and contains(@style,'display: none')]")));
			driverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(@id,'loading_layer')]")));
			String pageSource = driver.getPageSource();
			Pattern p = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
			Matcher m = p.matcher(pageSource);
			String url = null;
			builder.append("PAGE_ID,NAV_ACTION,NAV_USER,NAV_TOKEN,MENU_ITEM_NAME\n");
			while (m.find()) {
				url = m.group(1); // this variable should contain the link URL
				if (url.contains("navToken") && !url.contains("target="))
					builder.append(Launch.tokenFetcher(url.replace("\"", "").replace("?navMethod=GET", "").replace("navAction=", "")
					                        .replace("navUsr=", "").replace("navToken=", ""), m.group(2)));
			}
			printWriter.write(builder.toString());
			printWriter.close();
		} finally {
			driver.close();
		}
	}

	public static String tokenFetcher(String url, String menuItemName) throws FileNotFoundException {

		StringBuilder string = new StringBuilder();
		String aray[] = url.replace("&amp", "").split("/")[7].split(";");
		for (int i = 0; i < aray.length; i++)
			string.append(aray[i]).append(",");
		string.append(menuItemName + ",");
		string.append("\n");
		return string.toString();
	}
}
