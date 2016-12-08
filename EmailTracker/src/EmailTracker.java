import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;

public class EmailTracker {
	private UserInterface ui;
	private Location location;
	public static final String imageFilePath = "image.jpg";
	public static final String logExt = ".log";

	public static void main(String[] args) {
		EmailTracker et = new EmailTracker();
		et.StartUI();
	}

	public void StartUI() {
		this.ui = new UserInterface();
		new Thread(this.ui).start();
	}

	private class UserInterface implements Runnable {
		private static final String UI_QUIT = "^quit";

		public void trace(String IP) throws GeoIp2Exception, UnknownHostException {
			URL url = getClass().getResource("City.mmdb");
			String filename = url.getPath();

			// A File object pointing to your GeoIP2 or GeoLite2 database
			File database = new File(filename);

			// This creates the DatabaseReader object, which should be reused
			// across
			// lookups.
			CityResponse response = null;
			try {
				DatabaseReader reader = new DatabaseReader.Builder(database).build();

				InetAddress ipAddress = InetAddress.getByName(IP);

				// Replace "city" with the appropriate method for your database,
				// e.g.,
				// "country".
				 response = reader.city(ipAddress);
			} catch (UnknownHostException e) {
				throw e;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			Country country = response.getCountry();
			System.out.println("Country Iso Code: " + country.getIsoCode());
			System.out.println("Country: " + country.getName());

			Subdivision subdivision = response.getMostSpecificSubdivision();
			System.out.println("State Iso Code: " + subdivision.getIsoCode());
			System.out.println("State: " + subdivision.getName());

			City city = response.getCity();
			System.out.println("City: " + city.getName());

			Postal postal = response.getPostal();
			System.out.println("Postal Code: " + postal.getCode());

			location = response.getLocation();
			System.out.println("Latitude: " + location.getLatitude());
			System.out.println("Longitude: " + location.getLongitude());
		}

		@Override
		public void run() {
			System.out.println("User Interface loaded...");
			Scanner input = new Scanner(System.in);
			String in = "";

			System.out.println("Welcome to the Allmighty Email Tracer!");
			System.out.println("We shall trace those headers for you, Great User!");

			while (true) {
				if (in.toLowerCase().matches(UI_QUIT)) { // EXIT
					System.out.println("User Interface closed...");
					input.close();
					System.exit(1);
				}

				// ------ Select File
				System.out.printf("Insert the file path:\n#>:");
				in = input.nextLine();

				String header = null;
				byte[] data = null;
				Path path = Paths.get(in);
				try { // read file
					data = Files.readAllBytes(path);
					header = new String(data);
				} catch (IOException e) {
					System.out.println("File doesnt exist: " + e.getMessage());
					continue;
				}

				// ------ Parse given file
				System.out.println("Parsing File...");
				EmailHeaderParser hp = new EmailHeaderParser(header);

				try {
					writeParsedHeader(hp, path.getFileName().toString());
					System.out.println("Logged To File : "+path.getFileName().toString()+logExt);
				} catch (IOException e) {
					System.out.println("Failed to create log file.");
				}

				// ------ Present retrieved IPs
				System.out.println("Now retrieving IP data...");
				for (int i = 0; i < hp.receiverPath.size(); i++) {
					EmailHeaderParser.Receiver r = hp.receiverPath.get(i);
					if (r.fromIP != null) {
						System.out.printf("[%d] %s [ %s ] - (%s)\n", i, r.fromIP, r.fromHostname, r.date);
					}
					// TODO there may also be IPs in the "by" field
				}

				if (!(hp.receiverPath.size() > 0)) {
					System.out.println("No IPs were found.");
					continue;
				}

				// ------ Select IP
				System.out.printf("Select the IP you want to trace\n#>:");
				in = input.nextLine();
				String selectedIP = null;
				try {
					selectedIP = hp.receiverPath.get(Integer.parseInt(in)).fromIP;
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					System.out.println("Invalid number given, please select a valid option.");
					continue;
				}
				System.out.printf("Searching for IP: %s\n", selectedIP);

				// ------ Trace IP
				try {
					this.trace(selectedIP);
				} catch (AddressNotFoundException | UnknownHostException e) {
					System.out.printf("IP adress [%s] was not found in the database.\n", selectedIP);
					continue;
				} catch (GeoIp2Exception e) {
					e.printStackTrace();
				}

				try {
					displayMapsLocation();
				} catch (Exception e) {
					continue;
				}
			}
		}

		public void writeParsedHeader(EmailHeaderParser he, String filename) throws IOException {
			String logFile = filename + logExt;
			FileOutputStream os = new FileOutputStream(logFile);
			String line1 = String.format("From : %s [ %s ]\n",he.from_Name, he.from_Email);
			String line2 = String.format("To : %s [ %s ]\n",he.to_Name, he.to_Email);
			String line3 = String.format("Delivered To : %s\n",he.deliveredTo);
			String line4 = String.format("Return Path : %s\n",he.returnPathEmail);
			String line5 = String.format("Content Type : %s\n\n",he.contentType);
			os.write((line1+line2+line3+line4+line5).getBytes()); // write 5 lines
			int maxFH = 0, maxFI = 0, maxBH = 0, maxBI = 0, maxWT = 0;
			for (int i = he.receiverPath.size()-1; i>=0; i--) { // get max lengths
				EmailHeaderParser.Receiver r = he.receiverPath.get(i);
				if (r.fromHostname != null) maxFH = (maxFH > r.fromHostname.length() ? maxFH : r.fromHostname.length());
				if (r.fromIP != null) maxFI = (maxFI > r.fromIP.length() ? maxFI : r.fromIP.length());
				if (r.byHostname != null) maxBH = (maxBH > r.byHostname.length() ? maxBH : r.byHostname.length());
				if (r.byIP != null) maxBI = (maxBI > r.byIP.length() ? maxBI : r.byIP.length());
				if (r.withMethod != null) maxWT = (maxWT > r.withMethod.length() ? maxWT : r.withMethod.length());
			}
			List<String> toWrite = new ArrayList<String>();
			for (int i = he.receiverPath.size()-1; i>=0; i--) {
				EmailHeaderParser.Receiver r = he.receiverPath.get(i);
				Integer.toString(maxFH);
				String rString = String.format(
						"%d   | %" + Integer.toString(maxFH) + "s -> %" + Integer.toString(maxFI) + "s  | %"
								+ Integer.toString(maxBH) + "s -> %" + Integer.toString(maxBI) + "s   |  %"
								+ Integer.toString(maxWT) + "s  |  %s\n",
						i, r.fromHostname, r.fromIP, r.byHostname, r.byIP, r.withMethod, r.date);
				toWrite.add(rString);
			}
			os.write(String.format("Hop | From%" + Integer.toString(maxFH + maxFI) + "s  |  By%"
					+ Integer.toString(maxBH + maxBI + 2) + "s  |  With%" + Integer.toString(maxWT-4) + "s  |  Date\n",
					" ", " ", " ").getBytes());
			os.write(String.format("----+-----%" + Integer.toString(maxFH + maxFI) + "s--+----%"
					+ Integer.toString(maxBH + maxBI + 2) + "s--+------%" + Integer.toString(maxWT-4) + "s--+----------------------\n",
					" ", " ", " ").replace(" ", "-").getBytes());
			for (String s : toWrite) os.write(s.getBytes());
			os.close();
		}

		public void displayMapsLocation() throws MalformedURLException, FileNotFoundException, IOException{
			// ------ Googling location
			JFrame test = new JFrame("Google Maps");

			String latitude = location.getLatitude().toString();
			String longitude = location.getLongitude().toString();
			String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + ","
					+ longitude
					+ "&zoom=15&size=612x612&scale=2&maptype=roadmap&markers=color:blue%7Clabel:T%7C"
					+ latitude + "," + longitude;

			// ------ Read map image and save
			try {
				URL url = new URL(imageUrl);
				InputStream is = url.openStream();
				OutputStream os = new FileOutputStream(imageFilePath);
				byte[] b = new byte[2048];
				int length;
				while ((length = is.read(b)) != -1) {
					os.write(b, 0, length);
				}
				is.close();
				os.close();
			} catch (FileNotFoundException e) {
				System.out.println("Failed to create image file.");
				throw e; // rethrow exceptions to allow while loop to continue...
			} catch (IOException e) {
				e.printStackTrace();
				throw e; // rethrow exceptions to allow while loop to continue...
			}

			// ------ Starting GUI to show image
			ImageIcon imageIcon = new ImageIcon((new ImageIcon(imageFilePath)).getImage().getScaledInstance(630,
					600, java.awt.Image.SCALE_SMOOTH));
			test.add(new JLabel(imageIcon));
			test.setVisible(true);
			test.pack();
		}
	}
}
