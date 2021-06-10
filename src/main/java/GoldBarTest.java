import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GoldBarTest {

    public static void main(String[] args) {

        String url = "http://ec2-54-208-152-154.compute-1.amazonaws.com/";
        String resultXpath = "//button[contains(text(),'=') or contains(text(),'>') or contains(text(),'<')]";
        int totalBars = 9;
        int unweighedBars = totalBars;
        int currentBarIndex = 0;
        int fakeBarIndex = -1;
        int weighingCount = 0;
        String result = "";
        String fakeBar = "";
        File testLog = new File("testLog.txt");

        //Change path to match where chromedriver.exe is installed on your machine
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        driver.get(url);

        while (unweighedBars > 0) {
            switch(result) {
                case "=":
                    currentBarIndex = (2 * totalBars)/3;
                    break;
                case "<":
                    currentBarIndex = 0;
                    break;
                case ">":
                    currentBarIndex = totalBars / 3;
                    break;
            }
            driver.findElement(By.xpath("//button[contains(@id, 'reset')][not (@disabled)]")).click();

            int currentBarAIndex = currentBarIndex;
            int currentBarBIndex = currentBarIndex + unweighedBars/3;

            for(int i = 0; i<unweighedBars / 3; i++) {
                driver.findElement(By.id("left_" + i)).sendKeys(String.valueOf(currentBarAIndex));
                driver.findElement(By.id("right_" + i)).sendKeys(String.valueOf(currentBarBIndex));
                currentBarIndex++;
                currentBarAIndex++;
                currentBarBIndex++;
            }
            unweighedBars = unweighedBars - (2 * totalBars)/3;
            driver.findElement(By.xpath("//button[contains(@id, 'weigh')]")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(resultXpath)));
            result = driver.findElement(By.xpath(resultXpath)).getText();
            weighingCount++;
        }

        DesiredCapabilities ignoreAlert = new DesiredCapabilities();
        ignoreAlert.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

        try {
            switch (result) {
                case "=":
                    fakeBarIndex = currentBarIndex + 1;
                    driver.findElement(By.id("coin_" + (fakeBarIndex))).click();
                    break;
                case "<":
                    fakeBarIndex = currentBarIndex - 1;
                    driver.findElement(By.id("coin_" + (fakeBarIndex))).click();
                    break;
                case ">":
                    fakeBarIndex = currentBarIndex;
                    driver.findElement(By.id("coin_" + (fakeBarIndex))).click();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + result);
            }
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            result = driver.switchTo().alert().getText();
            alert.accept();
            fakeBar = driver.findElement(By.xpath("//button[@class='square'][text()][" + (fakeBarIndex + 1) + "]")).getText();
        } catch (NoAlertPresentException ua) {
            System.out.println("No alert appeared.");
        } catch (IllegalStateException ie) {
            System.out.println("Unexpected error occurred.");
        }

        List<String> weighingList = new ArrayList<>(weighingCount);

        for(int i=0; i<weighingCount; i++) {
            weighingList.add((i), driver.findElement(By.xpath("//ol//li[" + (i+1) + "]")).getText());
        }

        try {
            FileWriter writer = new FileWriter(testLog);
            BufferedWriter buffer = new BufferedWriter(writer);
            buffer.write(result + " The fake bar is " + fakeBar + ".");
            buffer.newLine();
            buffer.write("The algorithm weighed " + weighingCount + " times.");
            buffer.newLine();
            for(int i=0; i<weighingCount; i++) {
                buffer.write((i+1) + ". " + weighingList.get(i));
                buffer.newLine();
            }
            buffer.close();
        } catch(IOException e) {
            System.out.println("There is an issue with the test log file.");
        }
    }
}
