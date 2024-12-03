class Test {
public static void main(String[] a) {
int N = 25000;
int res = 0;
for (int i = 0; i < N; i++)
for (int j = 0; j < N; j++) {
res += (i+j);
res -= 17*(res/17);
}
System.out.println(res);
}
}