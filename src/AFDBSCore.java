import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Der kryptographische Kern für das AFDBS-System.
 * Schreibt Schlüsseldateien mit einer völlig zufälligen UUID im Dateinamen.
 */
public final class AFDBSCore {
    public static final byte[] MAGIC = "AFDBSLOCK".getBytes(StandardCharsets.UTF_8);
    public static final int VERSION = 1;
    public static final int FILE_ID_SIZE = 16;
    public static final int BUFFER = 8192;

    private AFDBSCore() {}

    /**
     * Generiert einen komplett zufälligen Pfad für die Schlüsseldatei,
     * damit man Dateien nicht über den Namen vergleichen kann.
     */
    public static Path generateRandomKeyPath(Path parentDirectory) {
        String randomUuid = UUID.randomUUID().toString();
        return parentDirectory.resolve("afdbs-" + randomUuid + ".key");
    }

    public static Path encrypt(Path in, Path outLocked) throws Exception {
        SecureRandom rnd = new SecureRandom();
        byte[] fileId = new byte[FILE_ID_SIZE];
        rnd.nextBytes(fileId);

        long origSize = Files.size(in);
        String name = in.getFileName().toString();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";

        try (InputStream is = new BufferedInputStream(Files.newInputStream(in, StandardOpenOption.READ));
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(outLocked, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {

            os.write(MAGIC);
            os.write((byte) VERSION);
            os.write(fileId);

            String id = genAlnum(24, rnd);
            String key = id;
            long[] seeds = deriveSeedsFromKey(key, fileId);

            XorShift128Plus ks = new XorShift128Plus(seeds[0], seeds[1]);
            byte[] inBuf = new byte[BUFFER];
            int r;
            while ((r = is.read(inBuf)) != -1) {
                byte[] stream = ks.keystreamBytes(r);
                for (int i = 0; i < r; i++) {
                    inBuf[i] = (byte) (inBuf[i] ^ stream[i]);
                }
                os.write(inBuf, 0, r);
            }
            os.flush();

            // Erstelle die Schlüsseldatei mit einer zufälligen ID im selben Ordner wie die Ausgabedatei
            Path keyPath = generateRandomKeyPath(outLocked.getParent() != null ? outLocked.getParent() : Path.of("."));
            String sb = "fileId=" + HexFormat.of().formatHex(fileId) + '\n' +
                    "id=" + id + '\n' +
                    "key=" + key + '\n' +
                    "size=" + origSize + '\n' +
                    "ext=" + ext + '\n';
            Files.writeString(keyPath, sb, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return keyPath; // Gibt den zufälligen Pfad zurück, damit das UI ihn anzeigen kann
        }
    }

    public static void decrypt(Path locked, Path keyPath, Path out) throws Exception {
        if (!Files.exists(keyPath)) {
            throw new IOException("Schlüsseldatei nicht gefunden: " + keyPath);
        }
        String keyTxt = Files.readString(keyPath);
        byte[] fileIdInKey = HexFormat.of().parseHex(parseValue(keyTxt, "fileId"));
        String key = parseValue(keyTxt, "key");
        long origSize = Long.parseLong(parseValue(keyTxt, "size"));

        long[] seeds = deriveSeedsFromKey(key, fileIdInKey);

        try (InputStream is = new BufferedInputStream(Files.newInputStream(locked, StandardOpenOption.READ));
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {

            byte[] header = new byte[MAGIC.length];
            if (is.read(header) != header.length || !Arrays.equals(header, MAGIC)) {
                throw new IOException("Keine gültige AFDBS Datei!");
            }
            if (is.read() != VERSION) {
                throw new IOException("Falsche Dateiversion!");
            }
            byte[] fid = is.readNBytes(FILE_ID_SIZE);
            if (!Arrays.equals(fid, fileIdInKey)) {
                throw new IOException("Schlüssel passt nicht zur File ID!");
            }

            XorShift128Plus ks = new XorShift128Plus(seeds[0], seeds[1]);
            byte[] inBuf = new byte[BUFFER];
            long written = 0;
            int r;
            while ((r = is.read(inBuf)) != -1 && written < origSize) {
                int toProcess = (int) Math.min(r, origSize - written);
                byte[] stream = ks.keystreamBytes(toProcess);
                for (int i = 0; i < toProcess; i++) {
                    inBuf[i] = (byte) (inBuf[i] ^ stream[i]);
                }
                os.write(inBuf, 0, toProcess);
                written += toProcess;
            }
            os.flush();
        }
    }

    public static String parseValue(String txt, String key) {
        for (String line : txt.split("\n")) {
            if (line.startsWith(key + "=")) {
                return line.substring((key + "=").length());
            }
        }
        return null;
    }

    private static String genAlnum(int len, SecureRandom rnd) {
        String pool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(pool.charAt(rnd.nextInt(pool.length())));
        }
        return sb.toString();
    }

    private static long[] deriveSeedsFromKey(String key, byte[] fileId) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes(StandardCharsets.UTF_8));
        md.update(fileId);
        byte[] hash = md.digest();

        long s0 = java.nio.ByteBuffer.wrap(hash, 0, 8).getLong();
        long s1 = java.nio.ByteBuffer.wrap(hash, 8, 8).getLong();
        return new long[]{s0, s1};
    }
}
