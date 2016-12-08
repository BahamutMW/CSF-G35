import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	public static void main(String[] args) {
		EmailTracker et = new EmailTracker();
		et.StartUI(et);
	}

	public void StartUI(EmailTracker tk) {
		this.ui = new UserInterface(tk);
		new Thread(this.ui).start();
	}

	private class UserInterface implements Runnable {
		private static final String UI_QUIT = "^quit";
		private EmailTracker _context;
		private UserInterface ui;

		public UserInterface(EmailTracker context) {
			_context = context;
		}

		public void trace(String IP) throws IOException, GeoIp2Exception {
			URL url = getClass().getResource("City.mmdb");
			String filename = url.getPath();

			// A File object pointing to your GeoIP2 or GeoLite2 database
			File database = new File(filename);

			// This creates the DatabaseReader object, which should be reused
			// across
			// lookups.
			DatabaseReader reader = new DatabaseReader.Builder(database).build();

			InetAddress ipAddress = InetAddress.getByName(IP);

			// Replace "city" with the appropriate method for your database,
			// e.g.,
			// "country".
			CityResponse response = reader.city(ipAddress);

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

				Path path = Paths.get(in);
				try {
					byte[] data = Files.readAllBytes(path);
					String header = new String(data);

					// ------ Parse given file
					System.out.println("Parsing File...");
					EmailHeaderParser hp = new EmailHeaderParser(header);

					// TODO options to select what action to take

					// ------ Present retrieved IPs
					System.out.println("Now retrieving IP data...");
					int c = 0;
					for (int i = 0; i < hp.receiverPath.size(); i++) {
						EmailHeaderParser.Receiver r = hp.receiverPath.get(i);
						if (r.fromIP != null) {
							System.out.printf("[%d] %s [ %s ] - (%s)\n", c, r.fromIP, r.fromHostname, r.date);
							c++;
						}
					}

					if (!(hp.receiverPath.size() > 0)) {
						System.out.println("No IPs were found.");
						continue;
					}

					// ------ Select IP
					System.out.printf("Select the IP you want to trace\n#>:");
					in = input.nextLine();
					String selectedIP = hp.receiverPath.get(Integer.parseInt(in)).fromIP;
					System.out.printf("Searching for IP: %s\n", selectedIP);

					// ------ Trace IP
					try {
						this.trace(selectedIP);
					} catch (AddressNotFoundException e) {
						System.out.printf("IP adress [%s] was not found in the database.\n", selectedIP);
					} catch (GeoIp2Exception e) {
						e.printStackTrace();
					}

					// ------ Googling location
					JFrame test = new JFrame("Google Maps");
					String destinationFile = "image.jpg";
					try {
						String latitude = location.getLatitude().toString();
						String longitude = location.getLongitude().toString();
						String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + ","
								+ longitude
								+ "&zoom=15&size=612x612&scale=2&maptype=roadmap&markers=color:blue%7Clabel:T%7C"
								+ latitude + "," + longitude;

						// ------ Read map image and save
						URL url = new URL(imageUrl);
						InputStream is = url.openStream();
						OutputStream os = new FileOutputStream(destinationFile);
						byte[] b = new byte[2048];
						int length;
						while ((length = is.read(b)) != -1) {
							os.write(b, 0, length);
						}
						is.close();
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}

					// ------ Starting GUI to show image
					ImageIcon imageIcon = new ImageIcon((new ImageIcon(destinationFile)).getImage().getScaledInstance(630,
							600, java.awt.Image.SCALE_SMOOTH));
					test.add(new JLabel(imageIcon));
					test.setVisible(true);
					test.pack();

				} catch (IOException e) {
					System.out.println("IOException: " + e.getMessage());
				}
			}
		}
	}
}
