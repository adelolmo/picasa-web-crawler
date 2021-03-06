/*
 * Copyright (c) 2010-2011 Ardesco Solutions - http://www.ardescosolutions.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ado.picasa;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Set;

/*
    Extracted from https://github.com/Ardesco/Ebselen
*/
public class FileDownloader {

    private final WebDriver driver;
    private final String downloadPath;

    public FileDownloader(WebDriver driverObject, String downloadPath) {
        this.driver = driverObject;
        this.downloadPath = downloadPath;
    }

    public String downloader(String downloadLocation, String filename) throws Exception {
        if (downloadLocation.trim().equals("")) {
            throw new Exception("The element you have specified does not link to anything!");
        }
        URL downloadURL = new URL(downloadLocation);
        HttpClient client = new HttpClient();
        client.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        client.setHostConfiguration(mimicHostConfiguration(downloadURL.getHost(), downloadURL.getPort()));
        client.setState(mimicCookieState(driver.manage().getCookies()));
        HttpMethod getRequest = new GetMethod(downloadURL.getPath());
        FileHandler downloadedFile;
        if (StringUtils.isNotBlank(filename)) {
            downloadedFile = new FileHandler(downloadPath + "/" + filename, true);
        } else {
            downloadedFile = new FileHandler(downloadPath + "/" + downloadURL.getFile().replaceFirst("/|\\\\", ""), true);
        }
        try {
            int status = client.executeMethod(getRequest);
            System.out.println(String.format("HTTP Status %d when getting '%s'", status, downloadURL.toExternalForm()));
            BufferedInputStream in = new BufferedInputStream(getRequest.getResponseBodyAsStream());
            int offset = 0;
            int len = 4096;
            int bytes = 0;
            byte[] block = new byte[len];
            while ((bytes = in.read(block, offset, len)) > -1) {
                downloadedFile.getWritableFileOutputStream().write(block, 0, bytes);
            }
            downloadedFile.close();
            in.close();
            System.out.println(String.format("File downloaded to '%s'", downloadedFile.getAbsoluteFile()));
        } catch (Exception e) {
            throw new Exception("Download failed!");
        } finally {
            getRequest.releaseConnection();
        }
        return downloadedFile.getAbsoluteFile();
    }

    private HttpState mimicCookieState(Set<org.openqa.selenium.Cookie> seleniumCookieSet) {
        HttpState mimicWebDriverCookieState = new HttpState();
        for (org.openqa.selenium.Cookie seleniumCookie : seleniumCookieSet) {
            Cookie httpClientCookie = new Cookie(seleniumCookie.getDomain(), seleniumCookie.getName(), seleniumCookie.getValue(), seleniumCookie.getPath(), seleniumCookie.getExpiry(), seleniumCookie.isSecure());
            mimicWebDriverCookieState.addCookie(httpClientCookie);
        }
        return mimicWebDriverCookieState;
    }

    private HostConfiguration mimicHostConfiguration(String hostURL, int hostPort) {
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(hostURL, hostPort);
        return hostConfig;
    }
}
