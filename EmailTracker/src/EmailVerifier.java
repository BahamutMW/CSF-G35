
public class EmailVerifier {
	public void main(String[] args) {
		String r = phishingVerifier("Miguel Correia Guerra [ miguel.guerra@tecnico.ulisboa.pt ]","miguel.guerra@tecnico.ulisboa.pt");
		System.out.println(r);
	}
	
	public String phishingVerifier(String from, String retpath){
		String response;
		String fromemail = from.trim();
		String returnemail = retpath.trim();
		if(returnemail.equals(fromemail)){
			response = "The return email and sender match! You'll be answering to the right email. "
					+ "\n" + "However be careful. Legitimate banks and most other companies will never ask for personal credentials via email.";
		}else{
			response = "The return email and sender do NOT match! Might be a case of spoofing or phishing! "
					+ "\n" + "Carefully verify the Return Path entry to check if it is a known address."
					+"\n" + "If it is unknown, please do not answer or provide sensitive information.";

		}
		
		return response;
	}
}
