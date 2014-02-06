package kds.xorss.ayoan.com;

public class SecretSharing {
	protected static int[] np = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37,
			41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101 };
	protected int k;
	protected int L;
	protected int n;
	private int[][][] vector;

	/**
	 * 1<=L<=k<=n
	 * 
	 * @param k
	 * @param L
	 * @param n
	 */
	public SecretSharing(int k, int L, int n) throws IllegalArgumentException {
		if (1 > L)
			throw new IllegalArgumentException(paramException(1, L));
		if (L > k)
			throw new IllegalArgumentException(paramException(L, k));
		if (k > n)
			throw new IllegalArgumentException(paramException(k, n));
		if (!isP(n))
			throw new IllegalArgumentException("nが素数ではありません");
		setK(k);
		setL(L);
		setN(n);
	}

	/**
	 * パラメータの例外処理用
	 * 
	 * @param x
	 * @param y
	 * @return "x<=yである必要があります"
	 */
	private static String paramException(int x, int y) {
		return String.format("%2d<=%2dである必要があります", x, y);
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getL() {
		return L;
	}

	public void setL(int l) {
		L = l;
	}

	/**
	 * mod演算を行う.単純な%ではマイナスに対応できないため関数で扱う
	 * 
	 * @param src
	 *            mod演算を行いたい整数
	 * @param m
	 *            求めるmodの法
	 * @return src mod m の値
	 */
	protected static int mod(int src, int m) {
		return ((src) % m < 0) ? mod(m + src, m) : src % m;
	}

	/**
	 * 2つのバイト配列の同じ位置にある値同士の排他的論理和を取る
	 * 
	 * @param a
	 *            掛ける値
	 * @param b
	 *            掛けられる値
	 * @return 演算結果
	 * @throws IllegalArgumentException
	 *             　aとbの長さが違う場合
	 */
	protected static byte[] xor(byte[] a, byte[] b)
			throws IllegalArgumentException {
		if (a.length != b.length)
			throw new IllegalArgumentException();
		byte[] res = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			res[i] = (byte) (a[i] ^ b[i]);
		}
		return res;
	}

	public int[][][] getVector() {
		return vector;
	}

	public void setVector(int[][][] vector) {
		this.vector = vector;
	}

	/**
	 * 2<=n<=11の最小の自然数を返す
	 * 
	 * @param n
	 *            求める素数の基準となる自然数
	 * @return n以上の最小の素数
	 * @throws IllegalArgumentException
	 *             nが自然数でない場合,12以上の場合に起こる
	 */
	public static int getMinP(int n) throws IllegalArgumentException {
		int res = 0;
		if (np[np.length - 1] < n || n < 0)
			throw new IllegalArgumentException();
		for (int i = 0; i < np.length - 1; i++) {
			if (n == np[i]) {
				res = np[i];
				break;
			}
			if (np[i] < n && n < np[i + 1]) {
				res = np[i + 1];
				break;
			}
		}
		return res;
	}

	/**
	 * 素数であるか否か
	 * 
	 * @param n
	 *            検査する整数
	 * @return　nが{@link #np}に含まれていればtrue
	 */
	public static boolean isP(int n) {
		int i = 0;
		for (i = 0; i < np.length; i++)
			if (n == np[i])
				break;
		return i != np.length;
	}

}
