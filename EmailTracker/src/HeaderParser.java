import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderParser {

	// REGEX PATTERNS
	private String HOSTNAME_REGEX_STRING = "[a-zA-Z0-9._+-]+";
	private String EMAIL_REGEX_STRING = HOSTNAME_REGEX_STRING+"@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

	private Pattern RETURN_PATH_RE = Pattern.compile("Return-Path: <("+EMAIL_REGEX_STRING+")>");
	private Pattern DELIVERED_TO_RE = Pattern.compile("Delivered-To: .*"); // TODO
	private Pattern RECEIVED_RE = Pattern.compile("Received: from (" + HOSTNAME_REGEX_STRING
			+ ")\\s\\((.+)?\\[(.+)\\]\\)((\r)?\n\\s\\(Authenticated sender: .+)?((\r)?\n\\svia ("
			+ HOSTNAME_REGEX_STRING + ")\\s\\((.+)?\\[(.+)\\]\\))?(\r)?\n\\sby ("
			+ HOSTNAME_REGEX_STRING + ") \\((" + HOSTNAME_REGEX_STRING
			+ ")( \\[(.+)\\])?\\).*(\r)?\n\\s(with.+)?((\r)?\n\\s)?for <(" + EMAIL_REGEX_STRING + ")>.*");
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

	public static void main(String[] args) {
		// UNCOMENT IF PATH NO WORKING
		//HeaderParser he = new HeaderParser("Return-Path: <miguel.guerra@tecnico.ulisboa.pt>\n Delivered-To: ist169494+anabela.borges-ist.utl.pt@mail3-store.ist.utl.pt\n Received: from smtp2.tecnico.ulisboa.pt (smtp2.tecnico.ulisboa.pt [193.136.128.22])\n by mail3.ist.utl.pt (Postfix) with ESMTP id 20D0A8026D69\n for <ist169494+anabela.borges-ist.utl.pt@mail.ist.utl.pt>; Tue,  8 Nov 2016 19:25:41 +0000 (WET)\n Received: from localhost (localhost.localdomain [127.0.0.1])\n by smtp2.tecnico.ulisboa.pt (Postfix) with ESMTP id 87FA2700035C\n for <anabela.borges@ist.utl.pt>; Tue,  8 Nov 2016 19:25:40 +0000 (WET)\n X-Virus-Scanned: by amavisd-new-2.10.1 (20141025) (Debian) at ist.utl.pt\n Received: from smtp2.tecnico.ulisboa.pt ([127.0.0.1])\n by localhost (smtp2.tecnico.ulisboa.pt [127.0.0.1]) (amavisd-new, port 10025)\n with LMTP id U3Pvs2vTc_Lx for <anabela.borges@ist.utl.pt>;\n Tue,  8 Nov 2016 19:25:40 +0000 (WET)\n Received: from mail3.ist.utl.pt (mail3.ist.utl.pt [IPv6:2001:690:2100:1::e1dc:b6b6])\n by smtp2.tecnico.ulisboa.pt (Postfix) with ESMTP id 2FE137000360\n for <anabela.borges@ist.utl.pt>; Tue,  8 Nov 2016 19:25:40 +0000 (WET)\n DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple; d=tecnico.ulisboa.pt;\n s=mail; t=1478633140;\n bh=SggAfMinBos/Aei5Io5PY6N9ByZFPpr/QsMMo2Ls6qU=;\n h=Date:From:To:Subject:In-Reply-To:References;\n b=lXh8RqBV2XgjjnKT1KDRgAoj9EebkrhG3RMOBFuYNb9tZXTZgfgLcka7KEs7IdLEf\n mopwjgi+Z0j22zHtkhTKVb8274Bwgku5TE5PiF05cctu6+Z2YdAMDkLwjKvI/yEvOl\n SfrCL5KWgI8IKdvyYt1h/wjMzbz0iMleNIOWKCos=\n Received: from webmail.tecnico.ulisboa.pt (webmail3.tecnico.ulisboa.pt [IPv6:2001:690:2100:1::912f:b135])\n (Authenticated sender: ist169770)\n by mail3.ist.utl.pt (Postfix) with ESMTPSA id A51CD8026D69\n for <anabela.borges@ist.utl.pt>; Tue,  8 Nov 2016 19:25:40 +0000 (WET)\n Received: from a109-49-150-47.cpe.netcabo.pt ([109.49.150.47])\n via vs1.ist.utl.pt ([2001:690:2100:1::33])\n by webmail.tecnico.ulisboa.pt\n with HTTP (HTTP/1.1 POST); Tue, 08 Nov 2016 19:25:40 +0000\n MIME-Version: 1.0\n Content-Type: text/plain; charset=US-ASCII;\n format=flowed\n Content-Transfer-Encoding: 7bit\n Date: Tue, 08 Nov 2016 19:25:40 +0000\n From: Miguel Correia Guerra <miguel.guerra@tecnico.ulisboa.pt>\n To: Anabela Sofia Martins Borges <anabela.borges@ist.utl.pt>\n Subject: Teste ciber\n In-Reply-To: <b3c174cca2013a491346f77f4a682e11@mail.ist.utl.pt>\n References: <b3c174cca2013a491346f77f4a682e11@mail.ist.utl.pt>\n Message-ID: <acc0062e847f8b1b7cc4276b25068acf@mail.tecnico.ulisboa.pt>\n X-Sender: miguel.guerra@tecnico.ulisboa.pt\n User-Agent: Roundcube Webmail/1.1.3\n \n Email de teste\n }");
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
