package org.ado.picasa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 27.02.16
 */
public class Main {

    private static final String TMP_DIR = "/tmp/picasa";

    public static void main(String[] args) throws Exception {
        final Options options = new Options();
        options.addOption("a", true, "Album name");
        options.addOption("v", true, "Verification code");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

//        System.exit(1);
        validateEnvironmentVariables();
        FileUtils.deleteQuietly(new File(TMP_DIR));
        FileUtils.forceMkdir(new File(TMP_DIR));
        System.out.println("start: " + new Date().toString());

        final FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.dir", TMP_DIR);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "image/jpeg,image/png");
        final FirefoxDriver driver = new FirefoxDriver(profile);
        loginIntoPicasa(cmd.getOptionValue("v"), driver);

        driver.navigate().to("https://picasaweb.google.com/home?showall=true");
        TimeUnit.SECONDS.sleep(2);

        final List<WebElement> albumLinks =
                driver.findElements(By.xpath("//p[@class='gphoto-album-cover-title']/a"));

        if (cmd.hasOption("a")) {
            final String albumName = cmd.getOptionValue("a").trim();
            System.out.println("Album: " + albumName);
            downloadAlbum(driver,
                    albumLinks.stream()
                            .filter(al -> al.getText().equals(albumName))
                            .findFirst().get()
                            .getAttribute("href"));

        } else {

            final Set<String> albumHrefs =
                    albumLinks.stream()
                            .map(a -> a.getAttribute("href"))
                            .collect(Collectors.toSet());

            albumHrefs.forEach(a -> downloadAlbum(driver, a));

        }
        System.out.println("done");
        System.out.println("end: " + new Date().toString());
        TimeUnit.SECONDS.sleep(10);
        driver.close();
    }

    private static void downloadAlbum(FirefoxDriver driver, String album) {
        try {
            final String albumName = getAlbumName(album);
            System.out.println("> album name: " + albumName + "   url: " + album);
            driver.navigate().to(album);

            final List<String> photoHrefLinks = driver.findElements(By.xpath("//div[@class='goog-icon-list']//a"))
                    .stream()
                    .filter(a -> MediaType.PHOTO.equals(getMediaType(a)))
                    .map(a -> a.getAttribute("href"))
                    .collect(Collectors.toList());

            for (String href : photoHrefLinks) {
                downloadPhoto(driver, href);
            }

            driver.navigate().to(album);

            final List<String> videoHrefLinks = driver.findElements(By.xpath("//div[@class='goog-icon-list']//a"))
                    .stream()
                    .filter(a -> MediaType.VIDEO.equals(getMediaType(a)))
                    .map(a -> a.getAttribute("href"))
                    .collect(Collectors.toList());

            int index = 1;
            final FileDownloader fileDownloader = new FileDownloader(driver, TMP_DIR);
            for (String videoUrl : getVideoUrls(driver, videoHrefLinks, album)) {
                try {
                    final FileHandler downloadedImage = new FileHandler(fileDownloader.downloader(videoUrl, albumName + index++ + ".m4v"));
                } catch (Exception e) {
                    System.out.println("Error! Cannot download video '" + videoUrl + "'.");
                    e.printStackTrace();
                }
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
        } catch (Exception e) {
            System.out.println("Error! Cannot download album '" + album + "'");
            e.printStackTrace();
        }
    }

    private static List<String> getVideoUrls(FirefoxDriver driver, List<String> videoHrefLinks, String returnPage) {
        final List<String> urls = new ArrayList<>();
        for (String href : videoHrefLinks) {
            System.out.println("downloading video: " + href);
            try {
                driver.navigate().to(href);
                TimeUnit.SECONDS.sleep(1);

                driver.switchTo().frame(driver.findElementByXPath("//iframe[@class='scaledimage-onscreenpane']"));
                urls.add(driver.findElementByXPath("//video").getAttribute("src"));

            } catch (Exception e) {
                System.out.println("Cannot get video url from '" + href + "'. Skipping ...");
                e.printStackTrace();
            } finally {
                driver.navigate().to(returnPage);
            }
        }
        return urls;
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
            e.printStackTrace();

        }
    }

    private static MediaType getMediaType(WebElement a) {
        return a.findElement(By.xpath("div/img")).getAttribute("src").contains("m4v") ?
                MediaType.VIDEO : MediaType.PHOTO;
    }

    private static String getAlbumName(String album) {
        if (album.contains("?")) {
            return album.substring(album.lastIndexOf("/") + 1, album.indexOf("?"));
        } else {
            return album.substring(album.lastIndexOf("/") + 1);
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

        if (StringUtils.isNotBlank(pin)) {
            driver.findElement(new By.ByName("Pin")).sendKeys(pin, Keys.ENTER);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private enum MediaType {
        PHOTO, VIDEO
    }
}