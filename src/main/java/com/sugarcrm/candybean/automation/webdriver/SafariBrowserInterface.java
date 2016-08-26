package com.sugarcrm.candybean.automation.webdriver;

import java.io.File;
import java.io.IOException;

import com.sugarcrm.candybean.utilities.OSValidator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.sugarcrm.candybean.exceptions.CandybeanException;

import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ThreadGuard;

public class SafariBrowserInterface extends WebDriverInterface {

	public SafariBrowserInterface() throws CandybeanException {
		super(Type.SAFARI);
	}

	@Override
	public void start() throws CandybeanException {
		String safariDriverPath = candybean.config.getPathValue("browser.safari.driver.path");
		validateSafariDriverExist(safariDriverPath);

		// If parallel is enabled and the safaridriver-<os>_<thread-name> doesn't exist, duplicate one
		// from safari-<os> and give it executable permission.
		if ("true".equals(candybean.config.getPathValue("parallel.enabled"))) {
			String originalSafariPath = safariDriverPath;
			// Cross platform support
			if (OSValidator.isWindows()) {
				safariDriverPath = safariDriverPath.replaceAll("(.*)(\\.exe)", "$1_" +
						Thread.currentThread().getName() + "$2");
			} else {
				safariDriverPath = safariDriverPath.replaceAll("$", "_" + Thread.currentThread().getName());
			}

			if (!new File(safariDriverPath).exists()) {
				try {
					FileUtils.copyFile(new File(originalSafariPath), new File(safariDriverPath));
					if (!OSValidator.isWindows()) { //Not needed in Windows
						Runtime.getRuntime().exec("chmod u+x " + safariDriverPath);
					}
				} catch (IOException e) {
					String error = "Cannot duplicate a new safaridriver for parallelization";
					logger.severe(error);
					throw new CandybeanException(error);
				}
			}
		}

		logger.info("safariDriverPath: " + safariDriverPath);
		System.setProperty("webdriver.safari.driver", safariDriverPath);
		logger.info("Instantiating Safari with:\n    driver path: " + safariDriverPath);

		super.wd = ThreadGuard.protect(new SafariDriver());
		super.start(); // requires wd to be instantiated first
	}

	private void validateSafariDriverExist(String safariDriverPath) throws CandybeanException {
		if (StringUtils.isEmpty(safariDriverPath) || !new File(safariDriverPath).exists()) {
			String error = "Unable to find safari browser driver from the specified location (" + safariDriverPath + ") " +
					"in the configuration file! \n Please add a configuration to the candybean config file for key \"browser.safari.driver.path\" "
					+ "that indicates the absolute or relative location the driver.";
			logger.severe(error);
			throw new CandybeanException(error);
		}
	}

	@Override
	public void stop() throws CandybeanException {
		logger.info("Stopping automation interface with type: " + super.iType);
		super.wd.close();
		super.stop();
	}

	@Override
	public void restart() throws CandybeanException {
		logger.info("Restarting automation interface with type: " + super.iType);
		this.stop();
		this.start();
	}
}
