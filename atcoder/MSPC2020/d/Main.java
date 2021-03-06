package atcoder.MSPC2020.d;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Main {
    static final FS sc = new FS();  // 封装输入类
    static final PrintWriter pw = new PrintWriter(System.out);
    public static void main(String[] args) {
        int n = sc.nextInt();
        long[] a = new long[n];
        for (int i = 0;i < n;i++) {
            a[i] = sc.nextLong();
        }
        long money = 1000;
        for (int i = 1; i < n; i++) {
            long k = 0;
            if (a[i - 1] < a[i]) {
                k = money / a[i - 1];
            }
            money += k * (a[i] - a[i - 1]);
        }
        pw.println(money);
        pw.flush();
    }

    static class FS {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");
        String next() {
            while(!st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch(Exception ignored) {}
            }
            return st.nextToken();
        }
        int[] nextArray(int n) {
            int[] a = new int[n];
            for(int i = 0;i < n;i++) {
                a[i] = nextInt();
            }
            return a;
        }
        int nextInt() {
            return Integer.parseInt(next());
        }
        long nextLong() {
            return Long.parseLong(next());
        }
    }
}