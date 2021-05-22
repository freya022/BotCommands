package com.freya02.botcommands.buttons;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Key {
	private final SecretKey key;
	private final IvParameterSpec iv;

	public Key(SecretKey key, IvParameterSpec iv) {
		this.key = key;
		this.iv = iv;
	}

	private static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(n);

		return keyGenerator.generateKey();
	}

	private static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);

		return new IvParameterSpec(iv);
	}

	public static Key randomKey() throws NoSuchAlgorithmException {
		return new Key(generateKey(128), generateIv());
	}

	public SecretKey getKey() {
		return key;
	}

	public IvParameterSpec getIv() {
		return iv;
	}
}
