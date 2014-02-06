package kds.xorss.ayoan.com;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class Util {
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
}
