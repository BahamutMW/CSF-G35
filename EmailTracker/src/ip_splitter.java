import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ip_splitter {
	private static Pattern pattern = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	public static List<String> split(String text) {
		Matcher ip = pattern.matcher(text);
		List<String> foundIPs = new ArrayList<String>();
		while (ip.find()) {
			foundIPs.add(ip.group());
		}
		return foundIPs;
	}
}
