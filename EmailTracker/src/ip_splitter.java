import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ip_splitter {
	private static Matcher ip;

	public Matcher getIPs() {
		return ip;
	}

	public static List<String> split(String text) {
		List<String> foundIPs = new ArrayList<String>();
		Pattern pattern = Pattern
				.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
		ip = pattern.matcher(text);
		while (ip.find()) {
			foundIPs.add(ip.group());
		}
		return foundIPs;
	}
}
