package aes.ayoan.com;

public class Run {
	/**
	 * AES実行関数 ファイルを暗号化し、暗号文と鍵を保存する
	 * 
	 * @param path
	 *            ファイルパス
	 * @throws Exception
	 */
	public static void enc(String path) throws Exception {
		byte[] data = FileIO.readFileToByte(path);
		byte[] key = KeyGen.keyGen().getEncoded();
		byte[] enBytes = EncryptECB.encryptECB(data, key);
		// byte[] deBytes = DecryptECB.decryptECB(enBytes, key);
		FileIO.writeByte(enBytes, "Client/test.png");
		FileIO.writeByte(key, "CLient/key");
	}

	/**
	 * AES復号関数 ファイルを復号化し、平文を保存する
	 * 
	 * @param path
	 *            //暗号文のファイルパス
	 * @param keypath
	 *            //鍵のファイルパス
	 * @throws Exception
	 */
	public static void dec(String path, String rec_path, String keypath)
			throws Exception {
		byte[] data = FileIO.readFileToByte(path);
		byte[] key = FileIO.readFileToByte(keypath);
		byte[] deBytes = DecryptECB.decryptECB(data, key);
		FileIO.writeByte(deBytes, rec_path);
	}
}
