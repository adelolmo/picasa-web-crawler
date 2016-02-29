package org.ado.picasa;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 27.02.16
 */
public class Main {

    private static final String TMP_DIR = "/tmp/picasa";

    public static void main(String[] args) throws Exception {
        validateEnvironmentVariables();
        FileUtils.deleteQuietly(new File(TMP_DIR));
        FileUtils.forceMkdir(new File(TMP_DIR));
        System.out.println("start: " + new Date().toString());

        final FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.dir", TMP_DIR);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "image/jpeg,image/png");
        final FirefoxDriver driver = new FirefoxDriver(profile);
        loginIntoPicasa(args[0], driver);

        driver.navigate().to("https://picasaweb.google.com/home?showall=true");
        TimeUnit.SECONDS.sleep(2);

//        driver.findElementByLinkText("Anja").click();
        // <a class="gphoto-album-cover-link"
        final Set<String> albumHrefs =
                driver.findElements(By.xpath("//div[@id='lhid_albums']//a[@class='gphoto-album-cover-link']")).stream()
                        .map(a -> a.getAttribute("href"))
                        .collect(Collectors.toSet());
        TimeUnit.SECONDS.sleep(1);

        for (String album : albumHrefs) {
            System.out.println("> " + album);
            final String albumName = getAlbumName(album);
            System.out.println(albumName);
            driver.navigate().to(album);

            final List<String> hrefs = driver.findElements(By.xpath("//div[@class='goog-icon-list']//a"))
                    .stream().map(a -> a.getAttribute("href"))
                    .collect(Collectors.toList());

            for (String href : hrefs) {
                downloadPhoto(driver, href);
            }

            TimeUnit.SECONDS.sleep(10);
            System.out.println("moving photos to directory: " + albumName);
            final File albumDirectory = new File(TMP_DIR, albumName);
            final Collection<File> files =
                    FileUtils.listFiles(new File(TMP_DIR),
                            TrueFileFilter.INSTANCE,
                            TrueFileFilter.INSTANCE);
            for (File file : files) {
                try {
                    FileUtils.moveFileToDirectory(file, albumDirectory, true);
                } catch (IOException e) {
                    FileUtils.moveFile(file, new File(albumDirectory, file.getName() + "-" + System.currentTimeMillis()));
                }
            }
        }

        System.out.println("done");
        System.out.println("end: " + new Date().toString());
        TimeUnit.SECONDS.sleep(60);
        driver.close();
    }

    private static String getAlbumName(String album) {
        // https://picasaweb.google.com/109839990130280946393/Mix
        // https://picasaweb.google.com/109839990130280946393/HangoutAsierDelOlmoAndoniDelOlmo?locked=true
        if (album.contains("?")) {
            return album.substring(album.lastIndexOf("/") + 1, album.indexOf("?"));
        } else {
            return album.substring(album.lastIndexOf("/") + 1);
        }
    }

    private static void downloadPhoto(FirefoxDriver driver, String href) {
        try {
            driver.navigate().to(href);
            TimeUnit.SECONDS.sleep(1);

            driver.executeScript("document.getElementsByClassName('goog-inline-block goog-toolbar-menu-button')[2].style['display']=''");
            driver.findElement(By.xpath("//div[@class='goog-inline-block goog-toolbar-menu-button'][3]")).click();
            TimeUnit.MILLISECONDS.sleep(200);
            driver.findElement(By.xpath("//div[@role='menu']")).click();
        } catch (Exception e) {
            System.out.println("Cannot download photo on '" + href + "'. Skipping ...");
        }
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

}