package dms.ayoan.com;

import java.util.Scanner;

import kds.xorss.ayoan.com.KN;
import aes.ayoan.com.Run;
import dds.xorss.ayoan.com.KLN;

public class Main {
	private static String SEC_PATH = "";
	private static String REC_PATH = "";
	long time = System.currentTimeMillis();
	static int k = 3, L = 2, n = 5;

	public static void main(String[] args) throws Exception {
		System.out.println("Enter D or R.");
		Scanner scan = new Scanner(System.in);
		String command = scan.next();
		if (command.equals("D")) {
			distribution();
		} else if (command.equals("R")) {
			recover();
		}
	}

	public static void distribution() throws Exception {
		System.out.println("Enter the FilePath.");
		Scanner scan = new Scanner(System.in);
		String path = scan.next();
		long time = System.currentTimeMillis();
		Run.enc(path);
		time = System.currentTimeMillis() - time;
		System.out.println(String.format("Proccessing time is %s ms.", time));
		time = System.currentTimeMillis();
		KLN.kln_ss(k, L, n, "Client/test.png");
		KLN.distribution();
		KN.kn_ss(k, n, "Client/key");
		KN.distribution();
		time = System.currentTimeMillis() - time;
		System.out.println(String.format("Proccessing time is %s ms.", time));
		System.out.println("end.");
	}

	public static void recover() throws Exception {
		long time = System.currentTimeMillis();
		KLN.kln_ss(k, L, n, "");
		KLN.recover();
		KN.kn_ss(k, n, "");
		KN.recover();
		time = System.currentTimeMillis() - time;
		System.out.println(String.format("Proccessing time is %s ms.", time));
		time = System.currentTimeMillis();
		Run.dec("Client/rec.png", "Client/boots.zip", "Client/key");
		time = System.currentTimeMillis() - time;
		System.out.println(String.format("Proccessing time is %s ms.", time));
		System.out.println("end.");
	}
}
