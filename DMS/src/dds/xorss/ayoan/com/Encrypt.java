package dds.xorss.ayoan.com;

import java.util.Random;

/**
 * 
 * @author 09x3026
 * 
 */
public class Encrypt extends SecretSharing {

	private byte[][] dis;
	private byte[][][] secrets;
	private int sLength;
	private static Random rand;
	private byte[][][] r;
	private int cellLength;

	/**
	 * nは素数であり,1<=L<=k<=nを満たす. 条件を満たさない場合は例外が投げられる.
	 * 配布したい個数が素数じゃなくても内部ではnより大きい最小の素数で演算してその一部を使うから欲しいだけ持ってけ
	 * 
	 * @param k
	 *            復号に必要な数
	 * @param L
	 *            データ量削減に使う
	 * @param n
	 *            分散した数
	 * @param sLength
	 *            秘密情報の長さ
	 */
	public Encrypt(int k, int L, int n, int sLength) {
		super(k, L, n);// パラメータチェックおよびセット
		rand = new Random();
		cellLength = (sLength / (L * (n - 1))); // ()の位置に注意．n-1は分母
		dis = new byte[n][(n - 1) * cellLength]; // 配列の初期化
		secrets = new byte[L][n][cellLength]; // 配列の初期化
		r = new byte[k - L][n][cellLength]; // 配列の初期化
	}

	/**
	 * ランダム数の初期化
	 */
	/*
	 * 2013/10/26 見直したたぶんOK
	 */
	private void generateRnds() {
		for (int i = 0; i < k - L; i++)
			for (int j = 0; j < n; j++)
				rand.nextBytes(r[i][j]);
	}

	/*
	 * アルゴリズムが絶対的におかしいので見直す 2012/12/13 7:33 たぶん直った ただし復号してないので分からない 2012/12/15
	 * 15:26 完成したけど、これの引数に秘密情報を取って戻り値として分散情報を返すように変更したい 2012/12/15 15:36 変更した
	 * 2012/12/17 11:28 cellLengthを毎回求めるのは非常にオーバーヘッドが大きいので,コンストラクタの中で指定するように変更する
	 * sの長さはL(n-1)の倍数になるようにパディングすること
	 * 
	 * 2013/10/26 間違ってるっぽいので見直します
	 */
	/**
	 * sの長さはL(n-1)になるようにパディングする 分散情報を生成するだけ
	 * 
	 * @param s
	 *            秘密情報
	 */
	public void encrypt(byte[] s) throws IllegalArgumentException {
		// if((s.length / (L * (n - 1))) != cellLength) throw new
		// IllegalArgumentException();

		// s をcellLength分ずつに分割し、secretsに格納
		splitSecret(s);
		// 乱数の生成
		generateRnds();
		byte[][] tmpBytes = new byte[n - 1][cellLength];

		// 部分分散情報 W(i, j) を生成
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= n - 2; j++) {
				tmpBytes[j] = secrets[0][mod(j - i, n)];
				for (int h = 1; h < L; h++) {
					tmpBytes[j] = xor(tmpBytes[j],
							secrets[h][mod(h * i + j, n)]);
				}
				tmpBytes[j] = xor(tmpBytes[j], r[0][j]);
				for (int h = 1; h < k - L; h++) {
					tmpBytes[j] = xor(tmpBytes[j],
							r[h][mod(((L - 1) + h) * i + j, n)]);
				}
			}
			int point = 0;
			for (int j = 0; j < n - 1; j++) {
				System.arraycopy(tmpBytes[j], 0, dis[i], point, cellLength);
				point += cellLength;
			}
		}
	}

	/**
	 * 引数のバイト配列を{@value #cellLength}ごとに分割して,{@link #secrets}に突っ込む
	 * 基本的に秘密情報を分割する専用.
	 * 
	 * @param s
	 *            分割したいバイト配列=秘密情報
	 */
	private void splitSecret(byte[] s) {// 汎用性あげたらオーバーヘッド大きいからしない
		int point = 0;
		for (int i = 0; i < L; i++)
			for (int j = 1; j < this.n; j++) {
				System.arraycopy(s, point, secrets[i][j], 0, cellLength);
				point += cellLength;
			}
	}

	public int getSLength() {
		return sLength;
	}

	public void setSLength(int sLength) {
		this.sLength = sLength;
	}

	public byte[] getDis(int position) {
		return dis[position];
	}

	public byte[][][] getSecrets() {
		return secrets;
	}

	public int getCellLength() {
		return cellLength;
	}

	public void setCellLength(int cellLength) {
		this.cellLength = cellLength;
	}
}
