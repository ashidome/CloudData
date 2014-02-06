package dds.xorss.ayoan.com;

public class Decrypt extends SecretSharing {

	private int cellLength; // 分割する長さ
	private int[] disN; // 各データが何番目のデータかを格納する配列
	private int[][][] g; // ガウスジョルダン法を使ってG(k,L,n)を求める

	/**
	 * (k,L,n)秘密分散法の復号プログラム
	 * 
	 * @param k
	 *            復号に必要な数
	 * @param L
	 *            データ量削減に使う
	 * @param n
	 *            分散した数
	 * @param cellLength
	 *            各セルの大きさ
	 * @param disN
	 *            集めた分散情報の番号
	 */
	public Decrypt(int k, int L, int n, int cellLength, int[] disN) {
		super(k, L, n);// パラメータチェックおよびセット
		this.cellLength = cellLength;
		this.disN = new int[k];
		System.arraycopy(disN, 0, this.disN, 0, k);
		// ベクトル(3次元配列)の確保
		setVector(new int[k][n - 1][L * (n - 1) + (n - 1) * (k - L)]);
		for (int i = 0; i < k; i++) {
			getVector()[i] = generateVector(this.disN[i]);
		}
		g = gaussJordan();
	}

	/**
	 * 分散情報の2進ベクトルを分割数分全部生成する 理論について詳しくは東京理科大の論文参照
	 * 
	 * @param i
	 *            分散情報の添字
	 * @return w_(i,j) j=0,1,2,...,kn-L-1の2進ベクトル
	 */
	private int[][] generateVector(int i) {
		int[][] res = new int[n - 1][k * n - L - 1];
		// 分散Step3
		for (int j = 0; j <= n - 2; j++) {
			// S^0_(j-i)が立っているかどうか
			if (mod(j - i, n) != 0) {
				res[j][mod(j - i, n) - 1] += 1;
			}
			// XOR S^h_(hi+j)
			for (int h = 1; h < L; h++) {
				if (mod(h * i + j, n) != 0) {
					res[j][h * (n - 1) + mod(h * i + j, n) - 1] += 1;
				}
			}
			// r^0_j
			res[j][L * (n - 1) + j] += 1;
			// XOR r^h_{(L-1)+h}i+j
			for (int h = 1; h < k - L; h++) {
				// どの位置に1を立てるか
				int value = L * (n - 1) + (n - 1) + (h - 1) * n
						+ mod(((L - 1) + h) * i + j, n);
				// L*(nP-1)→Sの分
				// (nP-1)→r^0の分
				// (h-1)*(k-L)→r^(h-1)の分
				res[j][value] += 1;
			}
		}
		return res;
	}

	/**
	 * ガウスジョルダンの掃き出し法を用いて単位行列を生成する ついでに逆行列も求めるとなんか知らんけど排他的論理和を取るべき分散情報の配列が作れる
	 * 逆行列で求めるのは適当にやっただけだからよく知らんけど動いてるからいい
	 * 
	 * @return 掛けるべき分散情報のとこに1が立ってる2次元配列
	 */
	private int[][][] gaussJordan() { // アルゴリズムもう覚えてないからコメント無し
		int[][] src = new int[getVector()[0][0].length][getVector()[0][0].length];
		for (int i = 0; i < getVector().length; i++)
			for (int j = 0; j < getVector()[i].length; j++) {
				for (int k = 0; k < getVector()[i][j].length; k++) {
					int value = i * getVector()[i].length + j;
					src[value][k] = getVector()[i][j][k];
				}
			}
		int length = src.length;
		int[][] inverse = new int[length][length]; // 逆行列が入る配列
													// これが排他的論理和の組み合わせを表してくれるんや！
		for (int i = 0; i < length; i++)
			inverse[i][i] = 1; // 単位行列で初期化

		for (int i = 0; i < length; i++) { // このへんからベクトルを単位行列に変換する
			for (int j = 0; j < length; j++) {
				if (src[i][i] == 1) { // 枢軸が1だったら,同じ列のそこ以外の行が0になるようにする
					if (src[j][i] == 1 && j != i) { // 枢軸以外について
						for (int k = 0; k < length; k++) { // 行ごと足し算
							inverse[j][k] = mod(inverse[j][k] + inverse[i][k],
									2);// 逆行列を求めるやつに足す
							src[j][k] = mod(src[i][k] + src[j][k], 2); // GF(2)では足し算したら0になる
						}
					}
				} else {
					if (src[j][i] == 1 && j > i) { // 枢軸が1じゃなかったら,1の行を足して1にする.ついでに1立ってると邪魔だから消す
						for (int k = 0; k < length; k++) { // 行ごと足し算
							inverse[i][k] = mod(inverse[i][k] + inverse[j][k],
									2);// 枢軸の部分に1が立ってる行の値を足す.
							inverse[j][k] = mod(inverse[j][k] + inverse[i][k],
									2);// 1が立ってた行に足す.元の枢軸行の値になる
							src[i][k] = mod(src[i][k] + src[j][k], 2); // まず枢軸を1にする
							src[j][k] = mod(src[i][k] + src[j][k], 2); // 次に元々1だったとこに足す.結果として元の枢軸行の値になる
						}
						j = 0; // オーバーヘッドがあると思うけど無視
						i = 0; // オーバーヘッドがあると思うけど無視
					}
				}
			}
		}
		int[][][] res = new int[getVector().length][getVector()[0].length][getVector()[0][0].length];
		for (int i = 0; i < res.length; i++)
			for (int j = 0; j < res[i].length; j++)
				for (int k = 0; k < res[i][j].length; k++) {
					res[i][j][k] = inverse[i * res[i].length + j][k];
				}
		return res;
	}

	/**
	 * アルゴリズムは適当に論文読んでください
	 * 
	 * @param data
	 * @return
	 * @throws IllegalArgumentException
	 */
	public byte[][] decrypt(byte[][] data) throws IllegalArgumentException {
		if (data.length < 1)
			throw new IllegalArgumentException(); // データがいくつあるか
		for (int i = 0; i < data.length - 1; i++) { // 全部同じ長さであることを確認する。
			if (data[i].length != data[i + 1].length)
				throw new IllegalArgumentException(); // 違ったら例外
		}
		byte[][] res = new byte[data.length][data[0].length]; // 完全な戻り値
		byte[][][] splitRes = new byte[data.length][data[0].length / cellLength][cellLength]; // 分割された戻り値.最初こっちに入れて結合する
		byte[][][] splitData = new byte[data.length][data[0].length
				/ cellLength][cellLength]; // 分割されたデータ

		for (int i = 0; i < splitData.length; i++)
			// 3次元配列分を
			for (int j = 0; j < splitData[i].length; j++)
				// ループで回して
				for (int k = 0; k < splitData[i][j].length; k++)
					// 分割されたデータに
					splitData[i][j][k] = data[i][j * cellLength + k]; // 元のデータの値をぶち込む

		for (int i = 0; i < g.length; i++)
			// Gが3次元配列だから
			for (int j = 0; j < g[i].length; j++)
				// 3重ループになってる
				for (int k = 0; k < g[i][j].length; k++) { // 回し続ける
					if (g[i][j][k] == 1) { // Gの値が1の時
						int resI = (i * g[i].length + j) / (n - 1); // splitResの場所指定
						int resJ = (i * g[i].length + j) % (n - 1); // splitResの場所指定
						int dataI = k / (n - 1); // 排他的論理和を取る分散情報の指定
						int dataJ = k % (n - 1); // 排他的論理和を取る分散情報の指定
						splitRes[resI][resJ] = xor(splitRes[resI][resJ],
								splitData[dataI][dataJ]); // 分散情報とsplitResを排他的論理和取る
					}
				}
		for (int i = 0; i < splitRes.length; i++)
			// 分割されたresを結合していく
			for (int j = 0; j < splitRes[i].length; j++) { // これもspliResが3重だから
				for (int k = 0; k < splitRes[i][j].length; k++) { // 3重ループになる
					res[i][j * cellLength + k] = splitRes[i][j][k]; // 結合
				}
			}
		secrets = res;
		secret = new byte[L * data[0].length];
		int point = 0;
		for (int i = 0; i < L; i++) {
			System.arraycopy(secrets[i], 0, secret, point, secrets[i].length);
			point += secrets[i].length;
		}
		return res;
	}

	private byte[][] secrets;
	private byte[] secret;

	public byte[] getSecret() {
		return secret;
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

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}
}
