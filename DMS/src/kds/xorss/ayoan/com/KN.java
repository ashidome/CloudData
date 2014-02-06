package kds.xorss.ayoan.com;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dds.xorss.ayoan.com.SecretSharing;

public class KN {

	private static int k, L, n;
	private static BufferedOutputStream[] outputs;
	private static File input;
	private static String DIS_PATH = new File("").getAbsolutePath()
			+ "/Client/dis%02d.dat";
	private static String SEC_PATH = "";
	private static FileInputStream[] diss;
	private static FileOutputStream rec;
	private static String REC_PATH = "Client/key";
	private static File config;
	private static String CFG_PATH = new File("").getAbsolutePath()
			+ "/DDS/sec.cfg";
	private static Encrypt enc;
	private static Decrypt dec;
	private static long secretLength;
	private static long count;
	private static long lastCount;
	private static final int MODE_DSTR = 1;
	private static final int MODE_RCVR = 2;
	private static final int cellLength = 1024;

	private static String ID_PATH = "Client/kds_id.csv";

	public static void kn_ss(int ks, int ns, String filepath) {
		k = ks;
		L = 1;
		n = SecretSharing.getMinP(ns);
		SEC_PATH = filepath;
	}

	// 分散関数
	public static void distribution() throws Exception {
		// SEC_PATHのファイルインスタンス生成
		input = new File(SEC_PATH);
		File csv = new File(ID_PATH);
		BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
		try {
			FileInputStream fileInputStream = new FileInputStream(input);
			// シェア出力用配列outputsの確保
			outputs = new BufferedOutputStream[n];
			// DIS_PATH形式でn個のシェアファイル作成
			for (int i = 0; i < n; i++) {
				outputs[i] = new BufferedOutputStream(new FileOutputStream(
						String.format(DIS_PATH, i)));
			}
			// L * (n - 1) * d のバッファ領域を確保
			byte[] buffer = new byte[L * (n - 1) * cellLength];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			int len = 0;
			// バッファが空になるまでループ
			while ((len = (fileInputStream.read(buffer))) != -1) {
				digest.update(buffer, 0, len);
				output(buffer);
				// 各シェアの書き込み
				for (int i = 0; i < n; i++) {
					outputs[i].write(enc.getDis(i));
				}
			}
			System.out.println(String.format("MD5:%s",
					hashByte2MD5(digest.digest())));
			config = new File(CFG_PATH);
			BufferedWriter cfgOut = new BufferedWriter(new FileWriter(config));
			cfgOut.write(String.valueOf(input.length()));

			// ハッシュ取得用に一時読込み配列
			byte[] share_temp;
			// outputのclose及び識別情報の作成
			for (int i = 0; i < n; i++) {
				outputs[i].close();
				share_temp = Util.readFileToByte(String.format(DIS_PATH, i));
				digest.update(share_temp, 0, share_temp.length);
				bw.write(i + "," + String.format(DIS_PATH, i) + ","
						+ hashByte2MD5(digest.digest()));
				bw.newLine();
			}
			bw.close();
			fileInputStream.close();
			cfgOut.close();

			// 入力ファイルの消去
			input.delete();
			System.out.println("Distributing Done.");
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	// 出力用シェア領域作成関数
	private static void output(byte[] secret) {
		byte[] s;
		// m = secret.length % L * (n - 1) * d
		// L * (n - 1) * dはバッファサイズ
		int m = SecretSharing.mod(secret.length, L * (n - 1) * cellLength);
		if (m != 0) {
			// secret がバッファサイズと等しくない場合、バッファサイズに補正する
			s = new byte[L * (n - 1) * cellLength];
			System.arraycopy(secret, 0, s, 0, secret.length);
		} else {
			// secret がバッファサイズと等しい場合、そのまま使用する
			s = secret;
		}
		enc = new Encrypt(k, L, n, s.length);
		enc.encrypt(s);
	}

	// 復号用関数
	public static void recover() {
		try {
			// 閾値個のFileInputStreamを確保
			diss = new FileInputStream[k];
			long lastLength = 0;
			config = new File(CFG_PATH);
			rec = new FileOutputStream(REC_PATH);
			BufferedReader cfgOut = new BufferedReader(new FileReader(config));
			secretLength = Long.valueOf(cfgOut.readLine());
			cfgOut.close();
			// 閾値分のシェアサイズを確認し、diss配列にFileInputStreamを格納
			for (int i = 1; i < k + 1; i++) {
				File dis = new File(String.format(DIS_PATH, i));
				if (i == 1) {
					lastLength = dis.length();
				} else if (dis.length() != lastLength) {
					throw new IllegalArgumentException();
				}
				lastLength = dis.length();
				diss[i - 1] = new FileInputStream(dis);
			}
			// 閾値個の(n-1)*dのバッファを用意
			byte[][] buffer = new byte[k][(n - 1) * cellLength];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			while (true) {
				// 各シェアをバッファに読み込む
				for (int i = 0; i < k; i++) {
					diss[i].read(buffer[i]);
				}
				// シェア読込み復号．返り値は長さ
				int length = input(buffer);
				// 復号した秘密情報をdataに格納
				byte[] data = dec.getSecret();
				count += length;
				if (count < secretLength) {
					rec.write(data, 0, length);
					digest.update(data, 0, length);
				} else {
					rec.write(data, 0, (int) (secretLength - lastCount));
					digest.update(data, 0, (int) (secretLength - lastCount));
					break;
				}
				lastCount = count;
			}
			System.out.println(String.format("MD5:%s",
					hashByte2MD5(digest.digest())));
			rec.close();
			for (int i = 0; i < k; i++) {
				diss[i].close();
			}
			System.out.println("Recovering Done.");
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	// シェアを読込み、複合し、長さを返す関数
	private static int input(byte[][] dis) {
		int[] obDisN = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
				17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
				33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
				49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64,
				65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
				81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96,
				97, 98, 99, 100 };
		dec = new Decrypt(k, L, n, cellLength, obDisN);
		dec.decrypt(dis);
		return dec.getSecret().length;
	}

	private static String hashByte2MD5(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}

		return hexString.toString();
	}

	public static void usage() {
		System.out.println("Usage: java -jar ***.jar [D]|[R] -f [FILENAME]");
		System.out.println("[D]|[R]: Distribution or Recover");
		System.out.println("[FILENAME]: the name of secret to share");
	}
}