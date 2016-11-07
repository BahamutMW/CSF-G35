import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class EmailTracker {
	private UserInterface ui;

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
		private static final String UI_NUMBERS = "^bs\\s[\\-0-9]+";
		private static final String UI_WINDOWS = "(?:[\\w]\\:|\\\\|\\.|\\.\\.)(\\\\[A-Za-z_\\-\\s0-9\\.]+)+\\.(txt|log)";
		private static final String UI_LINUX = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?";
		private boolean exit;
		private EmailTracker _context;
		private UserInterface ui;

		public UserInterface(EmailTracker context) {
			_context = context;
		}

		public void setExit(boolean b) {
			exit = b;
		}

		@Override
		public void run() {
			System.out.println("User Interface loaded...");
			setExit(false);
			Scanner input = new Scanner(System.in);
			String in = "";

			System.out.println("Welcome to the Allmighty Email Tracer!");
			System.out.println("We shall trace those headers for you, Great User!");

			while (!exit) {

				if (in.toLowerCase().matches(UI_QUIT)) {
					setExit(true);

				} else

				System.out.println("Insert the file path:");
				System.out.print("#>:");
				in = input.nextLine();
				while ((!in.toLowerCase().matches(UI_WINDOWS)) || (!in.toLowerCase().matches(UI_LINUX))) {
					System.out.println("Invalid path");
					System.out.print("#>:");
					in = input.nextLine();
				}
				Path path = Paths.get(in);
				try {
					byte[] data = Files.readAllBytes(path);
					String header = new String (data);
					System.out.println(header);
				} catch (IOException e) {
					System.out.println("Problem in the IO");
				}

			}
			System.out.println("User Interface closed...");
			input.close();
		}

	}
}
