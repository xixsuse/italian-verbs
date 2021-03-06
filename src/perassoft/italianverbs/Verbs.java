package perassoft.italianverbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.SharedPreferences;

public class Verbs extends ArrayList<Verb> {
	private static final String HIDDEN_VERBS = "VISIBLE_VERBS";
	private static final String VERBS = "VERBS";
	private static final String VERBI_BIN = "Verbi.bin";
	/**
	 * 
	 */
	private static final long serialVersionUID = 3464198266953132774L;
	private ArrayList<Verb> visibleVerbs;

	private Verbs() {

	}

	private static Verbs loadFromResource() {
		try {
			Verbs verbs = new Verbs();
			InputStream is = MyApplication.getInstance().getResources()
					.openRawResource(R.raw.verbi);

			BufferedReader input = new BufferedReader(
					new InputStreamReader(is), 1024 * 8);

			String line = null;
			while ((line = input.readLine()) != null) {
				Verb verb = new Verb(line);
				for (int i = 0; i < Verb.MAX_VERBS; i++) {
					line = input.readLine();
					verb.add(line);
				}
				verbs.add(verb);
			}
			input.close();
			return verbs;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Verbs loadVerbs() {
		File file = MyApplication.getInstance().getFileStreamPath(VERBI_BIN);
		if (file.exists()) {
			Verbs v = (Verbs) MyApplication.getInstance().readObject(file);
			if (v != null)
				return v;
		}
		Verbs verbs = loadFromResource();
		try {
			MyApplication.getInstance().saveObject(file, verbs);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return verbs;
	}

	public Verb get(String string) {
		for (Verb v : this)
			if (v.getName().equalsIgnoreCase(string))
				return v;
		return null;
	}

	public void delete(Verb verb) throws IOException {
		remove(verb);
		File file = MyApplication.getInstance().getFileStreamPath(VERBI_BIN);
		MyApplication.getInstance().saveObject(file, this);
		countVisibleVerbs();
	}

	public void addVerb(Verb verb) throws IOException {
		for (Verb v : this)
			if (v.getName().equals(verb.getName()))
				this.remove(v);
		add(verb);
		sort();
		File file = MyApplication.getInstance().getFileStreamPath(VERBI_BIN);
		MyApplication.getInstance().saveObject(file, this);
		countVisibleVerbs();
	}

	public void setHiddenVerb(Verb v, boolean b) {
		SharedPreferences settings = MyApplication.getInstance()
				.getSharedPreferences(VERBS, 0);
		String verbs = settings.getString(HIDDEN_VERBS, "");
		ArrayList<String> vv = new ArrayList<String>();
		for (String s : verbs.split(","))
			vv.add(s);
		if (b) {
			if (!vv.contains(v.getName())) {
				vv.add(v.getName());
			}
		} else {
			if (vv.contains(v.getName())) {
				vv.remove(v.getName());
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String s : vv) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(s);
		}
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(HIDDEN_VERBS, sb.toString());
		editor.commit();
		countVisibleVerbs();
	}

	public static boolean isHiddenVerb(Verb verb) {
		SharedPreferences settings = MyApplication.getInstance()
				.getSharedPreferences(VERBS, 0);
		String verbs = settings.getString(HIDDEN_VERBS, "");
		ArrayList<String> vv = new ArrayList<String>();
		for (String s : verbs.split(","))
			vv.add(s);
		return vv.contains(verb.getName());
	}

	public int countVisibleVerbs() {
		visibleVerbs = new ArrayList<Verb>();
		for (int i = 0; i < size(); i++) {
			Verb verb = get(i);
			if (!isHiddenVerb(verb))
				visibleVerbs.add(verb);
		}
		if (visibleVerbs.size() == 0) {
			Verb v = get(0);
			setHiddenVerb(v, false);
			visibleVerbs.add(v);
		}
		return visibleVerbs.size();
	}

	public ArrayList<Verb> getVisibleVerbs() {
		return visibleVerbs;
	}

	public static void restoreOriginal() {
		File file = MyApplication.getInstance().getFileStreamPath(VERBI_BIN);
		if (file.exists())
			file.delete();
		SharedPreferences settings = MyApplication.getInstance()
				.getSharedPreferences(VERBS, 0);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(HIDDEN_VERBS, "");
		editor.commit();
		MyApplication.getInstance().resetVerbs();
	}

	public void sort() {
		Collections.sort(this, new Comparator<Verb>() {
			public int compare(Verb a, Verb b) {
				return a.getName().compareTo(b.getName());
			}

		});

	}

}
