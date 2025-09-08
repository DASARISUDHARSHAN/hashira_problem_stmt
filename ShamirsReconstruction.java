import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Fraction {
    BigInteger num;
    BigInteger den;

    public Fraction(BigInteger num, BigInteger den) {
        if (den.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Denominator cannot be zero");
        }
        // Normalize: keep denominator positive
        if (den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        BigInteger gcd = num.gcd(den);
        this.num = num.divide(gcd);
        this.den = den.divide(gcd);
    }

    public Fraction add(Fraction other) {
        BigInteger n = this.num.multiply(other.den).add(other.num.multiply(this.den));
        BigInteger d = this.den.multiply(other.den);
        return new Fraction(n, d);
    }

    public Fraction multiply(Fraction other) {
        return new Fraction(this.num.multiply(other.num), this.den.multiply(other.den));
    }
}

public class ShamirsReconstruction  {
    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("input.json")));

        // Simple manual JSON parsing for the expected structure
        Map<String, Map<String, String>> shares = new HashMap<>();
        int n = 0, k = 0;

        // Remove whitespace and braces for easier parsing
        content = content.replaceAll("[\\n\\r\\t ]", "");
        content = content.substring(1, content.length() - 1); // remove outer {}

        String[] entries = content.split("},");
        for (String entry : entries) {
            if (!entry.endsWith("}")) entry += "}";
            int colonIdx = entry.indexOf(':');
            String key = entry.substring(0, colonIdx).replaceAll("\"", "");
            String value = entry.substring(colonIdx + 1);

            if (key.equals("keys")) {
                // Parse n and k
                value = value.substring(1, value.length() - 1); // remove {}
                String[] params = value.split(",");
                for (String param : params) {
                    String[] pair = param.split(":");
                    if (pair[0].replaceAll("\"", "").equals("n")) {
                        n = Integer.parseInt(pair[1]);
                    } else if (pair[0].replaceAll("\"", "").equals("k")) {
                        k = Integer.parseInt(pair[1]);
                    }
                }
            } else {
                // Parse share
                value = value.substring(1, value.length() - 1); // remove {}
                String[] params = value.split(",");
                Map<String, String> share = new HashMap<>();
                for (String param : params) {
                    String[] pair = param.split(":");
                    share.put(pair[0].replaceAll("\"", ""), pair[1].replaceAll("\"", ""));
                }
                shares.put(key, share);
            }
        }

        // Sort share keys numerically and pick first k
        List<Integer> shareKeys = new ArrayList<>();
        for (String key : shares.keySet()) {
            shareKeys.add(Integer.parseInt(key));
        }
        Collections.sort(shareKeys);

        List<BigInteger> xVals = new ArrayList<>();
        List<BigInteger> yVals = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            String key = String.valueOf(shareKeys.get(i));
            Map<String, String> obj = shares.get(key);
            int base = Integer.parseInt(obj.get("base"));
            String val = obj.get("value");
            BigInteger decimalValue = new BigInteger(val, base);
            xVals.add(new BigInteger(key));
            yVals.add(decimalValue);
        }

        Fraction constant = new Fraction(BigInteger.ZERO, BigInteger.ONE);

        for (int i = 0; i < k; i++) {
            Fraction term = new Fraction(yVals.get(i), BigInteger.ONE);
            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                BigInteger num = BigInteger.ZERO.subtract(xVals.get(j));
                BigInteger den = xVals.get(i).subtract(xVals.get(j));
                term = term.multiply(new Fraction(num, den));
            }
            constant = constant.add(term);
        }

        if (constant.den.equals(BigInteger.ONE)) {
            System.out.println("Constant term of polynomial = " + constant.num);
        } else {
            System.out.println("Constant term of polynomial = " + constant.num + "/" + constant.den);
        }
    }
}