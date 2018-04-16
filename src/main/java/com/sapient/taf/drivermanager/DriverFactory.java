package com.sapient.taf.drivermanager;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Splitter;
import com.sapient.taf.exceptions.AppiumServerPortNullException;
import com.sapient.taf.exceptions.BrowserInitException;
import com.sapient.taf.exceptions.BrowserTypeInvalidException;
import com.sapient.taf.framework.coreclasses.FrameworkConstants;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
public class DriverFactory {
    private static Properties configData;
    private static ApplicationContext ctx;

    private static String execPath;

    static {


        configData = new Properties();
        try {
            configData.load(new FileInputStream(FrameworkConstants.configParametrs));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Driver<?> createInstance(String browserName)
            throws MalformedURLException, InvocationTargetException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, SecurityException, IllegalArgumentException, FileNotFoundException {
        System.out.println("BresssHold" );
        boolean gridExecution = Boolean
                .valueOf(configData.getProperty("framework.execution.remote.value", "false").toLowerCase());
        URL url = new URL(gridExecution ? configData.getProperty("framework.execution.remote.hub.url")
                : configData.getProperty("framework.appium.url"));
        Capabilities cap = getCapabilities(browserName, gridExecution,
                new DesiredCapabilities(Splitter.on(',').withKeyValueSeparator('=').split(
                        configData.getProperty("framework.execution.additional.capabilities", "cap1=val1,cap2=val2"))));
        System.out.println("Bresss2" );
        return createInstance(browserName, url, cap, execPath);
    }

    private static Capabilities getCapabilities(String browserName, boolean grid, Capabilities... moreCapabilities)
            throws FileNotFoundException {
        DesiredCapabilities capabilities = new DesiredCapabilities(moreCapabilities);
        capabilities.setBrowserName(browserName);
        capabilities.setPlatform(grid ? Platform.valueOf(configData.getProperty("framework.execution.remote.platform"))
                : Platform.getCurrent());
        setExecPath(browserName, grid);
        return capabilities;
    }

    private static void setExecPath(String browserName, boolean grid) throws FileNotFoundException {
        if (grid) {
            execPath = null;
            return;
        } else {
            execPath= (String) configData.get("chromeDriverPath");
        }
    }

    public static Driver<?> createInstance(String browserName, URL url, Capabilities cap, String execPath)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            SecurityException, IllegalArgumentException, BrowserInitException {
        Driver<?> driver = null;
        Capabilities myCap;

        if (cap != null) {
            myCap = new DesiredCapabilities(cap);
        } else {
            myCap = new DesiredCapabilities();
        }

        switch (browserName.trim()) {
            case BrowserType.ANDROID:
                if (url == null)
                    throw new AppiumServerPortNullException();
                if (cap == null)
                    myCap.merge(DesiredCapabilities.android());
                driver = new Driver<AndroidDriver<MobileElement>>(url, myCap, AndroidDriver.class);
                break;
            case BrowserType.IPHONE:
                if (url == null)
                    throw new AppiumServerPortNullException();
                if (cap == null)
                    myCap.merge(DesiredCapabilities.iphone());
                driver = new Driver<IOSDriver<MobileElement>>(url, myCap, IOSDriver.class);
                break;
           
            case "headlesschrome" :
            	
                
                ChromeOptions chromeOptions = new ChromeOptions();
                 chromeOptions.addArguments("--headless");
                
   
                System.setProperty("webdriver.chrome.driver", execPath);
                driver = new Driver<ChromeDriver>(chromeOptions, ChromeDriver.class);
                break;
            	
            case BrowserType.CHROME:
                if (cap == null)
                    myCap.merge(DesiredCapabilities.chrome());
                
                if (execPath == null)
                    throw new BrowserInitException(new RuntimeException("Driver executable path invalid - " + execPath));
            
                
              	
                System.setProperty("webdriver.chrome.driver", execPath);
                driver = new Driver<ChromeDriver>(myCap, ChromeDriver.class);
                break;
                
           /* case BrowserType.CHROME:
                if (cap == null)
                    myCap.merge(DesiredCapabilities.chrome());
                
                if (execPath == null)
                    throw new BrowserInitException(new RuntimeException("Driver executable path invalid - " + execPath));
                else
                    System.setProperty("webdriver.chrome.driver", execPath);
                driver = new Driver<ChromeDriver>(myCap, ChromeDriver.class);
                break;*/
            case BrowserType.EDGE:
                if (cap == null)
                    myCap.merge(DesiredCapabilities.edge());
                if (execPath == null)
                    throw new BrowserInitException(new RuntimeException("Driver executable path invalid - " + execPath));
                else
                    System.setProperty("webdriver.edge.driver", execPath);
                driver = new Driver<EdgeDriver>(myCap, EdgeDriver.class);
                break;
            case BrowserType.FIREFOX:
                if (cap == null)
                    myCap.merge(DesiredCapabilities.firefox());
                if (execPath != null)
                    System.setProperty("webdriver.gecko.driver", execPath);
                driver = new Driver<FirefoxDriver>(myCap, FirefoxDriver.class);
                break;
            case BrowserType.PHANTOMJS:
                if (execPath == null) {
                    throw new BrowserInitException(new RuntimeException("Driver executable path invalid - " + execPath));
                } else {
                    DesiredCapabilities phan = new DesiredCapabilities();
                    phan.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, execPath);
                    myCap.merge(phan);
                }
               // System.setProperty("phantomjs.binary.path", execPath);
                driver = new Driver<PhantomJSDriver>(myCap, PhantomJSDriver.class);
                break;
            case BrowserType.IE:
                if (cap == null)
                    myCap.merge(DesiredCapabilities.internetExplorer());
                if (execPath == null)
                    throw new BrowserInitException(new RuntimeException("Driver executable path invalid - " + execPath));
                else
                    System.setProperty("webdriver.ie.driver", execPath);
                driver = new Driver<InternetExplorerDriver>(myCap, InternetExplorerDriver.class);
                break;
            case BrowserType.SAFARI:
                if (cap == null)
                    myCap.merge(DesiredCapabilities.safari());
                if (execPath == null)
                    throw new BrowserInitException(new RuntimeException("Driver executable path invalid - " + execPath));
                else
                    System.setProperty("webdriver.safari.driver", execPath);
                driver = new Driver<SafariDriver>(myCap, SafariDriver.class);
                break;
            case "GRID":
                if (cap == null)
                    throw new BrowserInitException(
                            "Cannot instantiate remote web browser without proper capabilities, please provide capabilities. Capabilities provided = ["
                                    + cap + "]");
                if (url == null)
                    throw new BrowserInitException(
                            "Cannot instantiate remote web browser without url for hub, please provide the same. Url provided = ["
                                    + url + "]");
                driver = new Driver<RemoteWebDriver>(url, myCap, RemoteWebDriver.class);

            case "None" :break;
            case BrowserType.HTMLUNIT:
           
                if (cap == null)
                    myCap.merge(DesiredCapabilities.htmlUnit());
                if (execPath != null)

                driver = new Driver<HtmlUnitDriver>(myCap, HtmlUnitDriver.class);
System.out.println("Done");
//driver.getWebDriver().get("google.com");

break;
            default:
                throw new BrowserTypeInvalidException();
        }
        return driver;
    }

    public static Driver<?> createInstance(String browserName, URL url, String execPath)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            SecurityException, IllegalArgumentException {
        return createInstance(browserName, url, null, execPath);
    }

    public static Driver<?> createInstance(String browserName, String execPath)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            SecurityException, IllegalArgumentException {
        return createInstance(browserName, null, null, execPath);
    }

    public static Driver<?> createInstance(String browserName, Capabilities cap, String execPath)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            SecurityException, IllegalArgumentException {
        return createInstance(browserName, null, cap, execPath);
    }
}
