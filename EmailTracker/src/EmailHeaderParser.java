import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailHeaderParser {

	private final String HOSTNAME_REGEX_STRING = "[a-zA-Z0-9._+-]+";
	private final String EMAIL_REGEX_STRING = HOSTNAME_REGEX_STRING + "@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
	private final String IP_GENERIC_REGEX_STRING = "[IPv0-9:a-f.]+";

	// PATTERNS

	private static Pattern IPv4_RE = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	private Pattern RETURN_PATH_RE = Pattern.compile("^Return-Path: <(" + EMAIL_REGEX_STRING + ")>");
	private Pattern DELIVERED_TO_RE = Pattern.compile("^Delivered-To: (.+)");

	private Pattern RECEIVED_RE = Pattern.compile("^Received:(?: from (?<fromHostname>" + HOSTNAME_REGEX_STRING
			+ ")\\s\\((?<fromDomain>" + HOSTNAME_REGEX_STRING + "\\s)?\\[(?<fromIP>." + IP_GENERIC_REGEX_STRING
			+ ")\\]\\))?(?:\\s\\(Authenticated sender: (?<authenticatedSender>.+)\\))?(?:\\svia (?<viaHostname>"
			+ HOSTNAME_REGEX_STRING + ")\\s\\((?<viaDomain>.+)?\\[(?<viaIP>.+)\\]\\))?\\sby (?<byHostname>"
			+ HOSTNAME_REGEX_STRING + ")(?: \\((?<byDomain>" + HOSTNAME_REGEX_STRING + ")(?: \\[(?<byIP>"
			+ IP_GENERIC_REGEX_STRING + ")\\])?\\))?(?:.*\\swith (?<withMethod>[A-Z]+))?(?:.*\\sfor <(?<forEmail>"
			+ EMAIL_REGEX_STRING + ")>)?.*;(?<date>.+) ");
	// saved for testing and to check group numbers, because now its a mess.... -> https://regex101.com/r/n2Nq8k/12

	private Pattern CONTENT_RE = Pattern.compile("^Content-Type: (.+?);");
	private Pattern FROM_RE = Pattern.compile("^From: ([a-zA-Z0-9._+\\-\\s]+) <(" + EMAIL_REGEX_STRING + ")>");
	private Pattern TO_RE = Pattern.compile("^To: ([a-zA-Z0-9._+\\-\\s]+) <(" + EMAIL_REGEX_STRING + ")>");

	public class Receiver {
		public String _original;

		public String fromHostname;
		public String fromDomain;
		public String fromIP;

		public String authenticatedSender;

		public String viaHostname;
		public String viaDomain;
		public String viaIP;

		public String withMethod;

		public String byHostname;
		public String byDomain;
		public String byIP;

		public String forEmail; // will be always the same as the recipient (most times at least)
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

	public EmailHeaderParser(String emailHeader) {
		this.receiverPath = new ArrayList<Receiver>();
		this.parse(emailHeader);
	}

	public void parse(String emailHeader){
		emailHeader = emailHeader.replaceAll("(\r)?\n\\s+", " "); // normalize header
		for (int i = 0; i < emailHeader.length(); i++) {
			String toParse = emailHeader.substring(i);

			Matcher ret_path = RETURN_PATH_RE.matcher(toParse);
			Matcher delivered = DELIVERED_TO_RE.matcher(toParse);
			Matcher received = RECEIVED_RE.matcher(toParse);
			Matcher content = CONTENT_RE.matcher(toParse);
			Matcher from = FROM_RE.matcher(toParse);
			Matcher to = TO_RE.matcher(toParse);

			if (toParse.startsWith("Return-Path:") && ret_path.find()) {
				this.returnPathEmail = ret_path.group(1);
				i += ret_path.group().length();
			}
			else if (toParse.startsWith("Delivered-To:") && delivered.find()) {
				this.deliveredTo = delivered.group(1);
				i += delivered.group().length();
			}
			else if (toParse.startsWith("Received:") && received.find()) {
				Receiver newNode = new Receiver();
				newNode._original = received.group();
				newNode.fromHostname = received.group("fromHostname");
				newNode.fromDomain = received.group("fromDomain");
				newNode.fromIP = received.group("fromIP");
				newNode.authenticatedSender = received.group("authenticatedSender");
				newNode.viaHostname = received.group("viaHostname");
				newNode.viaDomain = received.group("viaDomain");
				newNode.viaIP = received.group("viaIP");
				newNode.byHostname = received.group("byHostname");
				newNode.byDomain = received.group("byDomain");
				newNode.byIP = received.group("byIP");
				newNode.withMethod = received.group("withMethod");
				newNode.forEmail = received.group("forEmail");
				newNode.date = received.group("date").trim().replace("\n", " ");
				this.receiverPath.add(newNode);
				i += received.group().length();
			}
			else if (toParse.startsWith("Content-Type:") && content.find()) {
				this.contentType = content.group(1);
				i += content.group().length();
			}
			else if (toParse.startsWith("From:") && from.find()) {
				this.from_Name = from.group(1);
				this.from_Email = from.group(2);
				i += from.group().length();
			}
			else if (toParse.startsWith("To:") && to.find()) {
				this.to_Name = to.group(1);
				this.to_Email = to.group(2);
				i += to.group().length();
			}
		}
	}

	public static void main(String[] args) {
		// example main
		Path path = Paths.get("headertest.txt");
		byte[] data;
		try {
			data = Files.readAllBytes(path);
			String header = new String(data);
			EmailHeaderParser he = new EmailHeaderParser(header);
			// example:
			System.out.println("Header Path: ");
			for (EmailHeaderParser.Receiver r : he.receiverPath) {
				System.out.printf("%s [%s] -> %s\n", r.fromHostname, r.fromIP, r.date);
				System.out.printf("%s   - -  %s\n", r.withMethod, r.forEmail);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
