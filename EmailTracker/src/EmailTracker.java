import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import com.maxmind.*;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;

public class EmailTracker {
	private UserInterface ui;

	public static void main(String[] args) {
		EmailTracker et = new EmailTracker();
		ip_splitter ip = new ip_splitter();
		et.StartUI(et);
	}

	public void StartUI(EmailTracker tk) {
		this.ui = new UserInterface(tk);
		new Thread(this.ui).start();
	}

	private class UserInterface implements Runnable {
		private static final String UI_QUIT = "^quit";
		private static final String UI_NUMBERS = "^bs\\s[\\-0-9]+";
		private static final String UI_WINDOWS = "(?:[\\w]\\:|\\\\|\\.|\\.\\.)(\\\\[A-Za-z_\\-\\s0-9\\.]+)+\\.(txt|log)";
		private static final String UI_LINUX = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?";
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

			Location location = response.getLocation();
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

				System.out.printf("Insert the file path:\n#>:");
				in = input.nextLine();

				Path path = Paths.get(in);
				try {
					byte[] data = Files.readAllBytes(path);
					String header = new String(data);
					System.out.println("Now retrieving IP data...");
					List<String> foundIPs = ip_splitter.split(header);
					for (int i=0; i< foundIPs.size();i++)
						System.out.printf("[%d] %s\n", i, foundIPs.get(i));
					System.out.printf("Select the IP you want to trace\n#>:");
					in = input.nextLine();
					System.out.printf("Searching for IP: %s\n", foundIPs.get(Integer.parseInt(in)));
					try {
						this.trace(foundIPs.get(Integer.parseInt(in)));
					} catch (AddressNotFoundException e) {
						System.out.printf("IP adress [%s] was not found in the database.\n", foundIPs.get(Integer.parseInt(in)));
					} catch (GeoIp2Exception e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("Invalid File Path.");
				}

			}
		}
	}
}
