package aes.ayoan.com;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

public class KeyGen {
	@SuppressWarnings("unused")
	public static Key keyGen() throws NoSuchAlgorithmException{
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		generator.init(128, random);
		Key key = generator.generateKey();
		return key;
	}
}
