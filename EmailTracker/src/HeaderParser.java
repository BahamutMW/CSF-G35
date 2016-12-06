import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderParser {

	// REGEX PATTERNS
	private String HOSTNAME_REGEX_STRING = "[a-zA-Z0-9._+-]+";
	private String EMAIL_REGEX_STRING = HOSTNAME_REGEX_STRING+"@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

	private static Pattern IP_RE = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
	private Pattern RETURN_PATH_RE = Pattern.compile("Return-Path: <("+EMAIL_REGEX_STRING+")>");
	private Pattern DELIVERED_TO_RE = Pattern.compile("Delivered-To: .*"); // TODO
	private Pattern RECEIVED_RE = Pattern.compile("Received: from (" + HOSTNAME_REGEX_STRING
			+ ")\\s\\((.+)?\\[(.+)\\]\\)((\r)?\n\\s\\(Authenticated sender: .+)?((\r)?\n\\svia ("
			+ HOSTNAME_REGEX_STRING + ")\\s\\((.+)?\\[(.+)\\]\\))?(\r)?\n\\sby ("
			+ HOSTNAME_REGEX_STRING + ")( \\((" + HOSTNAME_REGEX_STRING
			+ ")( \\[(.+)\\])?\\).*)?(\r)?\n\\s(with.+)?((\r)?\n\\s)?(for <(" + EMAIL_REGEX_STRING + ")>)?.*");
	// testing https://regex101.com/r/n2Nq8k/2

	// TODO ADD MORE PATTERNS
	// ...

	public HeaderParser(String emailHeader) {
		this.parse(emailHeader);
	}

	public void parse(String emailHeader){
		for (int i = 0; i < emailHeader.length(); i++) {
			String toParse = emailHeader.substring(i);
			//System.out.println(toParse);

			Matcher rp = RETURN_PATH_RE.matcher(toParse);
			Matcher del = DELIVERED_TO_RE.matcher(toParse);
			Matcher rc = RECEIVED_RE.matcher(toParse);

			if (toParse.startsWith("Return-Path") && rp.find() && rp.groupCount() == 1) {
				System.out.println(rp.group(1));
				i += rp.group().length();
				// TODO parse groups and save them
			}
			else if (toParse.startsWith("Delivered-To") && del.find()) {
				System.out.println(del.group());
				i += del.group().length();
				// TODO parse groups and save them
			}
			else if (toParse.startsWith("Received") && rc.find()) {
				System.out.println(rc.group());
				i += rc.group().length();
				// TODO parse groups and save them
			}

			// ...
		}
	}

	public static List<String> getIPs(String text) {
		// TODO also match IPv6
		Matcher ip = IP_RE.matcher(text);
		List<String> foundIPs = new ArrayList<String>();
		while (ip.find()) {
			foundIPs.add(ip.group());
		}
		return foundIPs;
	}

	public static void main(String[] args) {
		// example main
		Path path = Paths.get("EmailTracker/src/headertest.txt");
		byte[] data;
		try {
			data = Files.readAllBytes(path);
			String header = new String(data);
			//System.out.println(header);
			HeaderParser he = new HeaderParser(header);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
