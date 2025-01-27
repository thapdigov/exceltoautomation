package az.edu.turing;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {

        WebDriverManager.firefoxdriver().setup();

        WebDriver driver = new FirefoxDriver();

        String excelPath = "src/main/resources/task_data.xlsx";
        File excelFile = new File(excelPath);
        if (!excelFile.exists()) {
            System.err.println("Excel file not found: " + excelFile.getAbsolutePath());
            return;
        }

        FileInputStream fis = new FileInputStream(excelFile);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet("Vərəq1");

        if (sheet == null) {
            System.err.println("Sheet not found in the workbook!");
            fis.close();
            workbook.close();
            return;
        }

        DataFormatter formatter = new DataFormatter();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell voenCell = row.getCell(1);
            if (voenCell == null) continue;

            String voen = formatter.formatCellValue(voenCell).trim();
            if (voen.isEmpty()) continue;

            driver.get("https://www.e-taxes.gov.az/");

            WebElement melumatLink = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(), 'Məlumat-axtarış sistemi')]")
                    )
            );
            melumatLink.click();

            WebElement voenSearchLink = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[@href='javascript:checkVoenOrName();']")
                    )
            );
            voenSearchLink.click();

            String originalWindow = driver.getWindowHandle();
            wait.until(driver2 -> (driver2.getWindowHandles().size() > 1));

            Set<String> allWindows = driver.getWindowHandles();
            for (String window : allWindows) {
                if (!window.equals(originalWindow)) {
                    driver.switchTo().window(window);
                    break;
                }
            }

            WebElement voenInput = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.name("voen"))
            );
            voenInput.clear();
            voenInput.sendKeys(voen);

            char lastDigit = voen.charAt(voen.length() - 1);
            if (lastDigit == '1') {
                driver.findElement(By.xpath("//input[@name='tip' and @value='L']")).click();
            } else if (lastDigit == '2') {
                driver.findElement(By.xpath("//input[@name='tip' and @value='P']")).click();
            }

            WebElement yoxlaButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(text(), 'Yoxla')]")
                    )
            );
            yoxlaButton.click();

            Thread.sleep(10000);

            List<WebElement> noExistMessages = driver.findElements(
                    By.xpath("//span[contains(text(), 'Belə bir vergi ödəyicisi müəyyən edilmədi')]")
            );

            Cell cellC = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell cellD = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell cellE = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell cellF = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            if (!noExistMessages.isEmpty()) {

                cellC.setCellValue("FALSE");
                cellD.setCellValue("Yox");
                cellE.setCellValue("");
                cellF.setCellValue("");
            } else {

                cellC.setCellValue("TRUE");
                cellD.setCellValue("Var");

                try {

                    WebElement tableRow = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(
                                    By.xpath("//table[@class='com']/tbody/tr[2]")
                            )
                    );
                    List<WebElement> tds = tableRow.findElements(By.tagName("td"));
                    if (tds.size() >= 5) {

                        String name = tds.get(2).getText().trim();
                        String taxOrg = tds.get(3).getText().trim();

                        cellE.setCellValue(name);
                        cellF.setCellValue(taxOrg);
                    } else {
                        cellE.setCellValue("TABLE PARSE ERROR");
                        cellF.setCellValue("");
                    }
                } catch (NoSuchElementException | TimeoutException ex) {
                    cellE.setCellValue("TABLE NOT FOUND");
                    cellF.setCellValue("");
                }
            }

            driver.close();
            driver.switchTo().window(originalWindow);
        }

        fis.close();
        FileOutputStream fos = new FileOutputStream(excelFile);
        workbook.write(fos);
        fos.close();
        workbook.close();

        driver.quit();
    }
}
