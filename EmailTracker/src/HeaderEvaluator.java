import java.util.Vector;

public class HeaderEvaluator {
	private Vector<Integer> received;
	private Vector<String> rcved;
	private int returnPath;
	private String rp;
	private int deliveredTo;
	private String dt;
	private int date;
	private String da;
	private int from;
	private String fro;
	private int messageID;
	private String mid;
	private int xSender;
	private String xs;
	private Vector<String> id;
	private String[] email; 
	
	public HeaderEvaluator(String _email){
		email = _email.split("\n");
		received = new Vector<Integer>();
		rcved = new Vector<String>();
		id = new Vector<String>();
	}
	
	public void Evaluate(){
		for (int i = 0; i < email.length; i++) {
	        if (email[i].startsWith("Return-Path:")) {
	            id.add(i, "Return-Path:");
	            returnPath = i;
	            System.out.println(i);
	        } else if(email[i].startsWith("Delivered-To:")){
	        	id.add(i,"Delivered-To:");
	        	deliveredTo = i;
	        } else if(email[i].startsWith("Received:")){
	        	id.add(i,"Received:");
	        	received.add(i);
	        } else if(email[i].startsWith("Date:")){
	        	id.add(i,"Date:");
	        	date = i;
	        } else if(email[i].startsWith("From:")){
	        	id.add(i,"From:");
	        	from = i;
	        } else if(email[i].startsWith("Message-ID:")){
	        	id.add(i,"Message-ID:");
	        	messageID = i;
	        } else if(email[i].startsWith("X-Sender:")){
	        	id.add(i,"X-Sender:");
	        	xSender = i;
	        } else {
	        	if(i!=0){
		        	id.add(i, id.get(i-1));
	        	} else {
	        		id.add(i, "empty");
	        	}
	        }
	        	
	    }
	}
	
	public void Received(){
		String rcv = "";
		for(int j = 0; j < received.size(); j++){
			int len;
			if(j+1 < received.size()){
				len = received.get(j+1);
			} else {
				len = email.length;
			}
			rcv = "";
			for(int i = received.get(j); (i < email.length) && (i < len); i++){
				if (id.get(i).contains("Received")){
					rcv = rcv + email[i];
				} else {
					rcved.add(rcv);
					return;
				}
			}
			rcved.add(rcv);
		}
		rcved.add(rcv);
		return;
	}
	
	public void DeliveredTo(){
		dt = "";
		for(int i = deliveredTo; i < email.length; i++){
			if (id.get(i).contains("Delivered-To")){
				dt = dt + email[i];
			} else {
				return;
			}
		}
		return;
	}
	public void ReturnPath(){
		rp = "";
		for(int i = returnPath; i < email.length; i++){
			if (id.get(i).contains("Return-Path")){
				rp = rp + email[i];
			} else {
				return;
			}
		}
		return;
	}
	public void Date(){
		da = "";
		for(int i = date; i < email.length; i++){
			if (id.get(i).contains("Date")){
				da = da + email[i];
			} else {
				return;
			}
		}
		return;
	}
	public void From(){
		fro = "";
		for(int i = from; i < email.length; i++){
			if (id.get(i).contains("From")){
				fro = fro + email[i];
			} else {
				return;
			}
		}
		return;
	}
	public void MessageID(){
		mid = "";
		for(int i = messageID; i < email.length; i++){
			if (id.get(i).contains("MessageID")){
				mid = mid + email[i];
			} else {
				return;
			}
		}
		return;
	}
	public void XSender(){
		xs = "";
		for(int i = xSender; i < email.length; i++){
			if (id.get(i).contains("Return-Path")){
				xs = xs + email[i];
			} else {
				return;
			}
		}
		return;
	}
	
	public void prints(){
		for (int i = 0; i < email.length; i++) {
			System.out.println(i + ": " + email[i]);
		}
	}
	
	public static void main(String[] args) {
		HeaderEvaluator he = new HeaderEvaluator("Return-Path: <miguel.guerra@tecnico.ulisboa.pt>\n"
				+ "Delivered-To: ist169494+anabela.borges-ist.utl.pt@mail3-store.ist.utl.pt\n"
				+ "Received: from smtp2.tecnico.ulisboa.pt (smtp2.tecnico.ulisboa.pt [193.136.128.22])\n"
				+ " by mail3.ist.utl.pt (Postfix) with ESMTP id 20D0A8026D69\n"
				+ " for <ist169494+anabela.borges-ist.utl.pt@mail.ist.utl.pt>; Tue, 8 Nov 2016 19:25:41 +0000 (WET)\n"
				+ "Received: from localhost (localhost.localdomain [127.0.0.1])\n"
				+ " by smtp2.tecnico.ulisboa.pt (Postfix) with ESMTP id 87FA2700035C\n"
				+ " for <anabela.borges@ist.utl.pt>; Tue,  8 Nov 2016 19:25:40 +0000 (WET)");
		System.out.println(he.email[0]);
		he.Evaluate();
		System.out.println(he.id.get(4));
		he.DeliveredTo();
		System.out.println(he.dt);
	}
}
