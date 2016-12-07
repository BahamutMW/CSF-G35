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
	private String EMAIL_REGEX_STRING = HOSTNAME_REGEX_STRING + "@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

	private static Pattern IP_RE = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
	private Pattern RETURN_PATH_RE = Pattern.compile("Return-Path: <(" + EMAIL_REGEX_STRING + ")>");
	private Pattern DELIVERED_TO_RE = Pattern.compile("Delivered-To: (.+)");
	private Pattern RECEIVED_RE = Pattern.compile("Received: from (" + HOSTNAME_REGEX_STRING
			+ ")\\s\\((.+)?\\[(.+)\\]\\)((\r)?\n\\s\\(Authenticated sender: (.+)\\))?((\r)?\n\\svia ("
			+ HOSTNAME_REGEX_STRING + ")\\s\\((.+)?\\[(.+)\\]\\))?(\r)?\n\\sby (" + HOSTNAME_REGEX_STRING + ")( \\(("
			+ HOSTNAME_REGEX_STRING + ")( \\[(.+)\\])?\\).*)?(\r)?\n\\s(with.+)?((\r)?\n\\s)?(for <("
			+ EMAIL_REGEX_STRING + ")>)?.*;((\r)?\n)?(.+)");
	// saved for testing and to check group numbers, because now its a mess.... -> https://regex101.com/r/n2Nq8k/2

	private Pattern CONTENT_RE = Pattern.compile("Content-Type: (.+)(\n\\s.+)?");
	private Pattern FROM_RE = Pattern.compile("From: ([a-zA-Z0-9._+\\-\\s]+) <(" + EMAIL_REGEX_STRING + ")>");
	private Pattern TO_RE = Pattern.compile("To: ([a-zA-Z0-9._+\\-\\s]+) <(" + EMAIL_REGEX_STRING + ")>");

	// TODO ADD MORE PATTERNS ?
	// ...

	public class Receiver {
		public String _original;

		public String from_hostname;
		public String from_hostnameDomain;
		public String from_ip;

		public String by_hostname;
		public String by_hostnameDomain;
		public String by_ip;

		public String via_hostname;
		public String via_hostnameDomain;
		public String via_ip;

		public String authenticatedSender;
	 	public String for_email; // will be always the same as the recipient
		public String date;
	}

	// Header Atributes
	public String returnPathEmail;
	public String deliveredTo;
	public String contentType;

	public List<Receiver> receiverPath;

	public String from_Name;
	public String from_Email;
	public String to_Name;
	public String to_Email;

	public HeaderParser(String emailHeader) {
		this.receiverPath = new ArrayList<Receiver>();
		this.parse(emailHeader);
	}

	public void parse(String emailHeader){
		for (int i = 0; i < emailHeader.length(); i++) {
			String toParse = emailHeader.substring(i);
			//System.out.println(toParse);

			Matcher ret_path = RETURN_PATH_RE.matcher(toParse);
			Matcher delivered = DELIVERED_TO_RE.matcher(toParse);
			Matcher received = RECEIVED_RE.matcher(toParse);
			Matcher content = CONTENT_RE.matcher(toParse);
			Matcher from = FROM_RE.matcher(toParse);
			Matcher to = TO_RE.matcher(toParse);

			if (toParse.startsWith("Return-Path") && ret_path.find() && ret_path.groupCount() == 1) {
				this.returnPathEmail = ret_path.group(1);
				i += ret_path.group().length();
			}
			else if (toParse.startsWith("Delivered-To") && delivered.find()) {
				this.deliveredTo = delivered.group(1);
				i += delivered.group().length();
			}
			else if (toParse.startsWith("Received") && received.find()) {
				Receiver newNode = new Receiver();
				newNode._original = received.group();
				newNode.from_hostname = received.group(1);
		 		newNode.from_hostnameDomain = received.group(2);
				newNode.from_ip = received.group(3);
				newNode.by_hostname = received.group(13);
				newNode.by_hostnameDomain = received.group(15);
				newNode.by_ip = received.group(17);
				newNode.via_hostname = received.group(9);
				newNode.via_hostnameDomain = received.group(10);
				newNode.via_ip = received.group(11);
				newNode.authenticatedSender = received.group(6);
				newNode.for_email = received.group(23);
				newNode.date = received.group(26).trim();
				this.receiverPath.add(newNode);
				i += received.group().length();
			}
			else if (toParse.startsWith("Content-Type") && content.find()) {
				this.contentType = content.group();
				i += content.group().length();
			}
			else if (toParse.startsWith("From") && from.find()) {
				this.from_Name = from.group(1);
				this.from_Email = from.group(2);
				i += from.group().length();
			}
			else if (toParse.startsWith("To") && to.find()) {
				this.to_Name = to.group(1);
				this.to_Email = to.group(2);
				i += to.group().length();
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
			HeaderParser he = new HeaderParser(header);
			// example:
			System.out.println("Header Path: ");
			for (HeaderParser.Receiver r : he.receiverPath) {
				System.out.printf("%s [%s] -> %s\n", r.from_hostname, r.from_ip, r.date);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
