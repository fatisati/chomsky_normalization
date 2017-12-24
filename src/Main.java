import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class Main {

	Vector<String[]> grammer = new Vector<>();
	String[] terminals;
	String[] noneTerminals;
	Vector<String> exist;
	Vector<Node> nodes = new Vector<>();

	public void readFile(String FILENAME) {

		BufferedReader br = null;
		FileReader fr = null;

		try {

			// br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;
			sCurrentLine = br.readLine();
			// System.out.println("none terminals are: "+sCurrentLine);
			noneTerminals = sCurrentLine.split(" ");

			sCurrentLine = br.readLine();
			// System.out.println("terminals are:"+ sCurrentLine);
			terminals = sCurrentLine.split(" ");

			while ((sCurrentLine = br.readLine()) != null) {
				String tmp[] = sCurrentLine.split(" ");
				grammer.addElement(tmp);
				// System.out.println("rule "+sCurrentLine+" added.");
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

	public void removeUseless1() {

		Vector<String> newTmp = new Vector<>();
		for (String s : terminals) {
			newTmp.addElement(s);
		}

		boolean flag = true;
		while (flag) {

			flag = false;
			for (String[] s : grammer) {

				if (matches(newTmp, s[1]) && !has(newTmp, s[0])) {

					newTmp.addElement(s[0]);
					flag = true;
				}
			}
		}
		Vector<String>nnoneTerminals = new Vector<>();
		Vector<String[]> newGrammer = new Vector<>();
		
		for (String[] rule : grammer) {

			if (matches(newTmp, rule[1])) {
				newGrammer.addElement(rule);
				if(!has(nnoneTerminals, rule[0])){
					nnoneTerminals.addElement(rule[0]);
				}
				// System.out.println(rule[0]+" "+rule[1]+" is ok");
			}
		}

		grammer = newGrammer;
		
		for(int i=0; i<nnoneTerminals.size(); i++){
			noneTerminals[i] = nnoneTerminals.elementAt(i);
		}
	

	}

	boolean has(Vector<String> vec, String s) {

		for (String str : vec) {
			if (s.equals(str)) {
				return true;
			}
		}
		return false;
	}

	boolean matches(Vector<String> vec, String inp) {

		char[] chars = inp.toCharArray();
		for (char c : chars) {

			int flag = 0;
			for (String terminal : vec) {

				if (c == terminal.charAt(0)) {
					flag = 1;
					break;
				}
			}

			if (flag == 0) {
				return false;
			}
		}

		return true;

	}

	public void removeUssless2() {

		Vector<String> reachable = new Vector<>();
		reachable.addElement("S");
		for (String[] rule : grammer) {

			if (rule[0].equals("S")) {
				reachable.addElement(rule[1]);
			}

		}

		boolean flag = true;
		while (flag) {

			flag = false;
			for (String[] s : grammer) {

				if (matches(reachable, s[0])) {

					for (char c : s[1].toCharArray()) {

						String str = Character.toString(c);
						if (!has(reachable, str)) {

							reachable.addElement(str);
							// System.out.println(str +" added");
							flag = true;
						}

					}

				}
			}
		}

		Vector<String[]> newGrammer = new Vector<>();
		for (String[] rule : grammer) {

			if (matches(reachable, rule[0])) {
				newGrammer.addElement(rule);
				// System.out.println(rule[0] + " " + rule[1] + " is ok");
			}
		}

		grammer = newGrammer;
	}

	public void removeNullable() {

		Vector<String> nullable = new Vector<>();
		exist = new Vector<>();

		for (String[] rule : grammer) {

			if (rule[1].equals("L")) {
				nullable.addElement(rule[0]);
			}

			else {
				if (!has(exist, rule[0])) {
					// System.out.println(rule[0]+" exist!");
					exist.addElement(rule[0]);
				}
			}
		}

		boolean flag = true;
		while (flag) {

			flag = false;
			for (String[] s : grammer) {

				if (matches(nullable, s[1]) && !has(nullable, s[0])) {

					nullable.addElement(s[0]);
					flag = true;
				}
			}
		}

		Vector<String[]> newGrammer = new Vector<>();
		StringBuilder strb = new StringBuilder();
		for (String[] rule : grammer) {
			produceRules(rule, nullable, 0, strb);
		}

		String[] nrules = strb.toString().split(";");
		for (String rule : nrules) {

			String[] r = rule.split(" ");
			if (!grammerHas(newGrammer, r)) {
				newGrammer.addElement(r);
			}
		}

		grammer = newGrammer;
	}

	public void produceRules(String[] rule, Vector<String> nullable, int k, StringBuilder ans) {

		if (k == nullable.size()) {
			ans.append(rule[0] + " " + rule[1] + ";");
			// System.out.println("rule: "+rule[0]+" "+rule[1]+" added;");

			return;

		}
		String str = nullable.elementAt(k);

		if (has(exist, str) && !rule[1].equals("L")) {
			// System.out.println(k+" "+rule[1]);
			produceRules(rule, nullable, k + 1, ans);
		}

		String[] newRule = new String[2];
		newRule[0] = rule[0];
		newRule[1] = rule[1].replace(str, "");
		if (newRule[1].length() != 0 && !newRule[1].equals("L")) {
			// System.out.println(k+" "+rule[1]);
			produceRules(newRule, nullable, k + 1, ans);
		}
	}

	public boolean grammerHas(Vector<String[]> gram, String[] rule) {

		for (String[] r : gram) {
			if (r[0].equals(rule[0]) && r[1].equals(rule[1])) {

				return true;
			}
		}
		return false;
	}

	public void removeUnit() {

		Iterator<String[]>itr = grammer.iterator();
		while(itr.hasNext()) {
			String[] rule = itr.next();
			if (rule[1].length() == 1 && rule[1].toUpperCase().equals(rule[1])) {
				Node f = fineNode(rule[0]);
				if (f == null) {
					f = new Node(rule[0]);
					nodes.addElement(f);
				}

				Node next = fineNode(rule[1]);
				if (next == null) {
					next = new Node(rule[1]);
					nodes.addElement(next);
				}

				f.nexts.addElement(next);
				itr.remove();
				System.out.println(f.data+"->"+next.data);
				
			}
			
		}

		Vector<String[]>newGrammer = new Vector<>();
		for(String[] rule:grammer){
			newGrammer.addElement(rule);
		}
		
		for (String nt : noneTerminals) {

			Node node = fineNode(nt);
			StringBuilder strb = new StringBuilder();
			
			
			if (node != null) {
				reachable(node, strb);
				String[] r = strb.toString().split(" ");
				
				for(String str: r){
					
					for(String[] rule: grammer){
						if(rule[0].equals(str)){
							
							String[] newRule = new String[2];
							newRule[0] = nt;
							newRule[1] = rule[1];
							if(notExist(newRule, newGrammer)){
								newGrammer.addElement(newRule);
							}
							
							
						}
					}
				}
			}

		}
		grammer = newGrammer;
		//System.out.println("{ S A B }\nS a\nS bc\nS bb\nS Aa\nA a\nA bb\nA bc\nB a\nB bb\nB bc\n");

	}

	public Node fineNode(String str) {

		for (Node n : nodes) {
			if (n.data.equals(str)) {
				return n;
			}
		}
		return null;
	}

	public void reachable(Node node, StringBuilder ans) {

		for (Node n : node.nexts) {
			
			if(!ans.toString().contains(n.data)){
				ans.append(n.data + " ");
				reachable(n, ans);
			}
			
		}
	}
	
	boolean notExist(String rule[], Vector<String[]>ngrammer){
		
		for(String[] r: ngrammer){
			if(r[0].equals(rule[0])
					&& r[1].equals(rule[1])){
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		
		
		StringBuilder ans = new StringBuilder();
		ans.append("{ ");
		for(String t: noneTerminals){
			ans.append(t+" ");
		}
		ans.append("}\n");
		for (String rule[] : grammer) {
			ans.append(rule[0] + " " + rule[1] + "\n");
		}

		return ans.toString();
	};

	public static void main(String[] args) {
		Main u = new Main();
		u.readFile("inp2.txt");
		//u.removeNullable();
		u.removeUnit();
		//u.removeUseless1();
		//u.removeUssless2();
		//System.out.println(u)
		

		
		System.out.println(u);
		//System.out.println("*********************");


	}
}

class Node {

	String data;
	Vector<Node> nexts;

	public Node(String data) {
		// TODO Auto-generated constructor stub
		this.data = data;
		nexts = new Vector<>();
	}
}
