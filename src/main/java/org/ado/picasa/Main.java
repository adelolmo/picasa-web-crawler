package org.ado.picasa;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andoni del Olmo
 * @since 27.02.16
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        validateEnvironmentVariables();

        final FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.dir", getDownloadDirectory());
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "image/jpeg");
        final FirefoxDriver driver = new FirefoxDriver(profile);
//        driver.manage().window().maximize();
        loginIntoPicasa(args[0], driver);

        driver.navigate().to("https://picasaweb.google.com/home?showall=true");
        TimeUnit.SECONDS.sleep(2);

        final WebElement album = driver.findElementByLinkText("Hermanos");
        album.click();
        TimeUnit.SECONDS.sleep(1);

        // <div class="goog-icon-list-area goog-icon-list-128">
//        final List<WebElement> albumDiv = driver.findElementsByClassName("goog-icon-list-area goog-icon-list-128");
//        albumDiv.forEach(webElement -> LOGGER.info(webElement.toString()));

        final List<WebElement> photoLinks = driver.findElements(By.xpath("//div[@class='goog-icon-list']//a"));
        final List<String> hrefs = photoLinks.stream().map(a -> a.getAttribute("href")).collect(Collectors.toList());

        for (String href : hrefs) {
            driver.navigate().to(href);
            TimeUnit.SECONDS.sleep(1);

//            downloadPhoto(driver);
//            driver.executeScript("document.getElementsByClassName('post-tag')[0].click();");
//            driver.executeScript("document.getElementsByClassName('goog-inline-block goog-toolbar-menu-button')[2].click();");
            driver.executeScript("document.getElementsByClassName('goog-inline-block goog-toolbar-menu-button')[2].style['display']=''");
            driver.findElement(By.xpath("//div[@class='goog-inline-block goog-toolbar-menu-button'][3]")).click();
            TimeUnit.MILLISECONDS.sleep(200);
            driver.findElement(By.xpath("//div[@role='menu']")).click();
//            driver.executeScript("document.getElementsByClassName('goog-menuitem').click()");
        }


//        driver.navigate().to(hrefs.get(0));
//        TimeUnit.SECONDS.sleep(1);

        // <div class="goog-inline-block lhcl_toolbar_text">Actions</div>
        // style="-moz-user-select: none; display: none;"

        // https://lh3.googleusercontent.com/-CxSeLBAmNWc/TSR_j-84WXI/AAAAAAAAMaY/utTKilxpCaw/I-Ic42/playa.jpg
        // https://lh3.googleusercontent.com/-Jq2m8gGFOlQ/TSR_j-BGJFI/AAAAAAAAMaY/ismErlyVieY/I-Ic42/IMG_0005.jpg
        // <img src="https://lh3.googleusercontent.com/-Jq2m8gGFOlQ/TSR_j-BGJFI/AAAAAAAAMaY/vudJ8aaa--o/s512-Ic42/IMG_0005.jpg" style="width: 512px; height: 406px;">


//        downloadPhoto(driver);


//        TimeUnit.SECONDS.sleep(5);
//        driver.close();
    }

    private static void validateEnvironmentVariables() {
        if (StringUtils.isEmpty(System.getenv("GOOGLE_ACCOUNT"))
                || StringUtils.isEmpty(System.getenv("GOOGLE_PASSWORD"))) {
            System.out.println("Missing environment variables GOOGLE_ACCOUNT or GOOGLE_PASSWORD");
            System.exit(1);
        }
    }

    private static void loginIntoPicasa(String pin, FirefoxDriver driver) throws InterruptedException {
        driver.get("https://picasaweb.google.com");
        driver.findElement(new By.ByName("Email")).sendKeys(System.getenv("GOOGLE_ACCOUNT"), Keys.ENTER);
        TimeUnit.SECONDS.sleep(1);

        driver.findElement(new By.ByName("Passwd")).sendKeys(System.getenv("GOOGLE_PASSWORD"), Keys.ENTER);
        TimeUnit.SECONDS.sleep(1);

        driver.findElement(new By.ByName("Pin")).sendKeys(pin, Keys.ENTER);
        TimeUnit.SECONDS.sleep(1);
    }

    private static void downloadPhoto(FirefoxDriver driver) throws InterruptedException {
        final WebElement actions = getActionsDropDown(driver);
        actions.click();
//        final Point point = actions.getLocation().moveBy(0, 20);

//        new Actions(driver).moveToElement(actions).perform();

        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='menu']/div[7]")));  // until this submenu is found

//        actions.getLocation().moveBy(10, 140).

//        new Actions(driver)
//                .moveToElement(actions).click()
//                .moveByOffset(10, 140).click()
//                .perform();

        // goog-menuitem goog-menuitem-hightlight

        final WebElement download = driver.findElement(By.xpath("//div[@role='menu']/div[7]"));
        LOGGER.info("'actions' displayed: {}", actions.isDisplayed());
        LOGGER.info(download.getText());

        new Actions(driver).moveToElement(download).perform();
        TimeUnit.MILLISECONDS.sleep(200);

        LOGGER.info("'download photo' displayed: {}", download.isDisplayed());
        LOGGER.info("'download photo' selected: {}", download.isSelected());
        LOGGER.info("'download photo' enabled: {}", download.isEnabled());
//        TimeUnit.SECONDS.sleep(30);
        download.click();

        // https://picasaweb.google.com/109839990130280946393/Hermanos?locked=true#5561712616517612962
        // https://lh3.googleusercontent.com/-SeaFCWuazTY/TS8sKFJXNaI/AAAAAAAAUM4/FIRTPKi5UMI/s640-Ic42/DSC00191.JPG
        // https://lh3.googleusercontent.com/-SeaFCWuazTY/TS8sKFJXNaI/AAAAAAAAUM4/FkhvZGZpjYU/I-Ic42/DSC00191.JPG


//        new Actions(driver)
//                .moveToElement(actions).click()
//                .moveToElement(download).click().release()
//                .perform();

        // repeat !
//        getActionsDropDown(driver).click();
//        driver.findElement(By.xpath("//div[@role='menu']/div[7]")).click();
    }

    private static WebElement getActionsDropDown(FirefoxDriver driver) {
        return driver.findElement(By.xpath("//div[@class='goog-inline-block goog-toolbar-menu-button'][not(contains(@style, 'display: none'))]"));
    }

    private static String getDownloadDirectory() throws IOException {
        final String dir = "/tmp/picasa";
        FileUtils.forceMkdir(new File(dir));
        return dir;
    }
}