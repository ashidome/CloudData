package aes.ayoan.com;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIO {
	/**
	 * ファイルを読み込み、その中身をバイト配列で取得する
	 * 
	 * @param filePath
	 *            対象ファイルパス
	 * @return 読み込んだバイト配列
	 * @throws Exception
	 *             ファイルが見つからない、アクセスできないときなど
	 */
	public static byte[] readFileToByte(String filePath) throws Exception {
		byte[] b = new byte[1024];
		FileInputStream fis = new FileInputStream(filePath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int size = 0;
		while (true) {
			size = fis.read(b);
			if (size <= 0) {
				break;
			}
			baos.write(b, 0, size);
		}
		baos.close();
		fis.close();
		b = baos.toByteArray();

		return b;
	}

	/**
	 * Byte配列書き込み関数
	 * 
	 * @param data
	 */
	public static void writeByte(byte[] data, String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
		}
	}
}
