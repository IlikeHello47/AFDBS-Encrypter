public final class XorShift128Plus {
    private long s0;
    private long s1;

    public XorShift128Plus(long s0, long s1) {
        this.s0 = s0;
        this.s1 = s1;
        if (this.s0 == 0 && this.s1 == 0) {
            this.s0 = 1337;
            this.s1 = 4242;
        }
    }

    public byte[] keystreamBytes(int len) {
        byte[] out = new byte[len];
        int p = 0;
        while (p < len) {
            long x = s0;
            long y = s1;
            s0 = y;
            x ^= x << 23;
            s1 = x ^ y ^ (x >> 17) ^ (y >> 26);
            long value = s1 + y;

            for (int i = 0; i < 8 && p < len; i++) {
                out[p++] = (byte) (value >> (i * 8));
            }
        }
        return out;
    }
}
