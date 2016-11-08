import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ip_splitter {
	private static Matcher ip;

	public Matcher getIPs() {
		return ip;
	}

	public static void split(String text) {
		Pattern pattern = Pattern
				.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
		ip = pattern.matcher(text);
		while (ip.find()) {
			System.out.println("found: " + ip.group());

		}
	}

}
