package fr.castor.core;

import fr.castor.core.security.HashHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashTest {

	private static final Logger logger = LoggerFactory.getLogger(HashTest.class);

	@Test
	public void testHash() {

		String password = "lollollol";

		if (logger.isDebugEnabled()) {
			logger.debug("pwd : " + password);
		}

		String hashedPassword = HashHelper.hashScrypt(password);

		if (logger.isDebugEnabled()) {
			logger.debug("hash : " + hashedPassword);
		}

		Assert.assertTrue(HashHelper.check(password, hashedPassword));
	}

}
