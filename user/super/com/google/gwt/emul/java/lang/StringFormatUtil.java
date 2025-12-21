package java.lang;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import java.util.ArrayList;
import java.util.List;

class StringFormatUtil {

    private static final RegExp FORMAT_REGEX = RegExp.compile(
        "([^%]+|%(?:\\d+\\$)?[\\+\\-\\ \\#0]*[0-9\\*]*(\\.?[0-9\\*]+)?[hlL]?[cbBdieEfgGosuxXpn%@])", "g");

    private static final RegExp TAG_REGEX = RegExp.compile(
        "(%)((?:(\\d+)\\$)?([\\+\\-\\ \\#0]*)([0-9\\*]*)((?:\\.[0-9\\*]+)?)([hlL]?)([cbBdieEfgGosuxXpn%@]))");

    public static String format(String format, Object... args) {
        List<String> tokens = getMatches(FORMAT_REGEX, format);
        int index = 0;
        StringBuilder result = new StringBuilder();
        int arg = 0; // 0-based indexing for args array

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);

            if (!format.substring(index, index + t.length()).equals(t)) {
                return result.toString();
            }

            index += t.length();

            if (t.charAt(0) != '%') {
                result.append(t);
            } else if (t.equals("%%")) {
                result.append("%");
            } else {
                MatchResult subtokens = TAG_REGEX.exec(t);

                if (subtokens == null || subtokens.getGroupCount() != 9 || !subtokens.getGroup(0).equals(t)) {
                    return result.toString();
                }

                String percentSign = subtokens.getGroup(1);
                String argIndexStr = subtokens.getGroup(3);
                String flags = subtokens.getGroup(4);
                String widthString = subtokens.getGroup(5);
                String precisionString = subtokens.getGroup(6);
                String length = subtokens.getGroup(7);
                String specifier = subtokens.getGroup(8);

                int argIndex;
                if (argIndexStr == null || argIndexStr.isEmpty()) {
                    argIndex = arg++;
                } else {
                    argIndex = Integer.parseInt(argIndexStr) - 1; // Convert to 0-based
                }

                Integer width = null;
                if ("*".equals(widthString)) {
                    width = toInteger(args[argIndex]);
                } else if (!widthString.isEmpty()) {
                    width = Integer.parseInt(widthString);
                }

                Integer precision = null;
                if (".*".equals(precisionString)) {
                    precision = toInteger(args[argIndex]);
                } else if (!precisionString.isEmpty()) {
                    precision = Integer.parseInt(precisionString.substring(1));
                }

                boolean leftJustify = flags.indexOf("-") >= 0;
                boolean padZeros = flags.indexOf("0") >= 0;
                String subresult = "";

                if (specifier.matches("[bBdiufeExXo]")) {
                    double num = toDouble(args[argIndex]);
                    String sign = "";

                    if (num < 0) {
                        sign = "-";
                    } else {
                        if (flags.indexOf("+") >= 0) {
                            sign = "+";
                        } else if (flags.indexOf(" ") >= 0) {
                            sign = " ";
                        }
                    }

                    if (specifier.equals("d") || specifier.equals("i") || specifier.equals("u")) {
                        String number = String.valueOf((long) Math.abs(Math.floor(num)));
                        subresult = justify(sign, "", number, "", width, leftJustify, padZeros);
                    }

                    if (specifier.equals("f")) {
                        String number;
                        if (precision != null) {
                            number = formatDecimal(Math.abs(num), precision);
                        } else {
                            number = String.valueOf(Math.abs(num));
                        }
                        String suffix = (flags.indexOf("#") >= 0 && number.indexOf(".") < 0) ? "." : "";
                        subresult = justify(sign, "", number, suffix, width, leftJustify, padZeros);
                    }

                    if (specifier.equals("e") || specifier.equals("E")) {
                        String number = formatExponential(Math.abs(num), precision != null ? precision : 21);
                        String suffix = (flags.indexOf("#") >= 0 && number.indexOf(".") < 0) ? "." : "";
                        subresult = justify(sign, "", number, suffix, width, leftJustify, padZeros);
                    }

                    if (specifier.equals("x") || specifier.equals("X")) {
                        String number = Long.toHexString((long) Math.abs(num));
                        String prefix = (flags.indexOf("#") >= 0 && num != 0) ? "0x" : "";
                        subresult = justify(sign, prefix, number, "", width, leftJustify, padZeros);
                    }

                    if (specifier.equals("b") || specifier.equals("B")) {
                        String number = Long.toBinaryString((long) Math.abs(num));
                        String prefix = (flags.indexOf("#") >= 0 && num != 0) ? "0b" : "";
                        subresult = justify(sign, prefix, number, "", width, leftJustify, padZeros);
                    }

                    if (specifier.equals("o")) {
                        String number = Long.toOctalString((long) Math.abs(num));
                        String prefix = (flags.indexOf("#") >= 0 && num != 0) ? "0" : "";
                        subresult = justify(sign, prefix, number, "", width, leftJustify, padZeros);
                    }

                    if (specifier.matches("[A-Z]")) {
                        subresult = subresult.toUpperCase();
                    } else {
                        subresult = subresult.toLowerCase();
                    }
                } else {
                    if (specifier.equals("%")) {
                        subresult = "%";
                    } else if (specifier.equals("c")) {
                        String str = String.valueOf(args[argIndex]);
                        subresult = str.isEmpty() ? "" : String.valueOf(str.charAt(0));
                    } else if (specifier.equals("s") || specifier.equals("@")) {
                        subresult = String.valueOf(args[argIndex]);
                    } else if (specifier.equals("p") || specifier.equals("n")) {
                        subresult = "";
                    }

                    subresult = justify("", "", subresult, "", width, leftJustify, false);
                }

                result.append(subresult);
            }
        }

        return result.toString();
    }

    private static String justify(String sign, String prefix, String string, String suffix,
                                   Integer width, boolean leftJustify, boolean padZeros) {
        int length = sign.length() + prefix.length() + string.length() + suffix.length();

        if (leftJustify) {
            return sign + prefix + string + suffix + pad(width != null ? width - length : 0, " ");
        } else {
            if (padZeros) {
                return sign + prefix + pad(width != null ? width - length : 0, "0") + string + suffix;
            } else {
                return pad(width != null ? width - length : 0, " ") + sign + prefix + string + suffix;
            }
        }
    }

    private static String pad(int n, String ch) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    private static List<String> getMatches(RegExp regex, String input) {
        List<String> matches = new ArrayList<String>();
        MatchResult result;
        int lastIndex = 0;

        while ((result = regex.exec(input)) != null) {
            matches.add(result.getGroup(0));
            // Prevent infinite loop on zero-width matches
            if (regex.getLastIndex() == lastIndex) {
                regex.setLastIndex(lastIndex + 1);
            }
            lastIndex = regex.getLastIndex();
        }
        regex.setLastIndex(0); // Reset for next use

        return matches;
    }

    private static double toDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int toInteger(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatDecimal(double value, int precision) {
        String format = "%." + precision + "f";
        // GWT doesn't support String.format, so we do manual formatting
        double multiplier = Math.pow(10, precision);
        long rounded = Math.round(value * multiplier);
        String result = String.valueOf(rounded);

        if (precision == 0) {
            return String.valueOf(Math.round(value));
        }

        while (result.length() <= precision) {
            result = "0" + result;
        }

        int decimalPos = result.length() - precision;
        return result.substring(0, decimalPos) + "." + result.substring(decimalPos);
    }

    private static String formatExponential(double value, int precision) {
        // Simple exponential notation - GWT compatible
        if (value == 0) {
            return "0" + (precision > 0 ? "." + pad(precision, "0") : "") + "e+00";
        }

        int exponent = (int) Math.floor(Math.log(value) / Math.log(10));
        double mantissa = value / Math.pow(10, exponent);

        String mantissaStr = formatDecimal(mantissa, precision);
        String expStr = String.valueOf(Math.abs(exponent));
        if (expStr.length() == 1) expStr = "0" + expStr;

        return mantissaStr + "e" + (exponent >= 0 ? "+" : "-") + expStr;
    }
}