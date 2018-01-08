import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Dictionary object
 * 
 * parses all information from a dictionary file and provides methods for
 * accessing it
 *
 */
public class Dictionary
{

	private HashMap<String, Lemma> lemmaMap;
	private ArrayList<String> lemmas;
	private HashMap<String, ArrayList<String>> formMap; // forms -> list of
														// associated lemmas
	private ArrayList<String> allParts;

	/**
	 * Constructor
	 * 
	 * @param file path for dictionary file
	 * @throws IOException If the file is not found or cannot be read
	 */
	public Dictionary(String file) throws IOException
	{
		BufferedReader bReader;

		bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

		String line = bReader.readLine();
		lemmaMap = new HashMap<String, Lemma>();
		lemmas = new ArrayList<String>();
		formMap = new HashMap<String, ArrayList<String>>();

		String currentLemmaString = "";
		Lemma currentLemma = null;
		String part = "Unknown";
		ArrayList<String> newForms = new ArrayList<String>();
		allParts = new ArrayList<String>();

		while (line != null)
		{
			if (line.startsWith("\\lx "))//entry lemma
			{
				if (!newForms.isEmpty())
				{
					for (String item : newForms)
					{
						currentLemma.addForm(item, part);
					}
				}
				part = "Unknown";
				newForms = new ArrayList<String>();
				String newLemmaString = line.substring(4);
				newForms.add(newLemmaString);
				if (!newLemmaString.equals(currentLemmaString))
				{
					lemmas.add(newLemmaString);
					currentLemma = new Lemma(newLemmaString);
					lemmaMap.put(newLemmaString, currentLemma);
					currentLemmaString = newLemmaString;
					ArrayList<String> aLemmas = new ArrayList<String>();
					aLemmas.add(currentLemmaString);
					formMap.put(currentLemmaString, aLemmas);
				}
			}
			else if (line.startsWith("\\glosa "))//gloss
			{
				currentLemma.addGloss(line.substring(7));
			}
			else if (line.startsWith("\\lx_") && !line.startsWith("\\lx_cita"))//other forms
			{
				String entry = line.substring(line.indexOf(' '));

				String[] entries = entry.split(";");
				for (String item : entries)
				{
					item = item.trim();
					ArrayList<String> aLemmas;
					if (!formMap.containsKey(item))
					{
						aLemmas = new ArrayList<String>();
						formMap.put(item, aLemmas);
					}
					else
					{
						aLemmas = formMap.get(item);
					}
					if (!aLemmas.contains(currentLemmaString))
					{
						aLemmas.add(currentLemmaString);
					}
					newForms.add(item);
				}
			}
			else if (line.startsWith("\\catgr "))//part of speech
			{
				part = line.substring(7);
				if (!allParts.contains(part))
				{
					allParts.add(part);
				}
			}

			line = bReader.readLine();
		}
		bReader.close();
	}

	/**
	 * returns list of lemmas
	 * 
	 * @return ArrayList of lemmas
	 */
	public ArrayList<String> getLemmaList()
	{
		return (ArrayList<String>) lemmas.clone();
	}

	/**
	 * returns list of lemmas starting with given string
	 * 
	 * @param refine term to search by
	 * @return ArrayList of lemmas
	 */
	public ArrayList<String> getLemmaList(String refine)
	{
		ArrayList<String> returnArr = new ArrayList<String>();
		for (String lemma : lemmas)
		{
			if (lemma.startsWith(refine))
			{
				returnArr.add(lemma);
			}
		}
		return returnArr;
	}

	/**
	 * returns lemmas which the supplied word can be associated with
	 * 
	 * @param term
	 * @return
	 */
	public ArrayList<String> findHeaders(String term)
	{
		ArrayList<String> result;
		if (formMap.containsKey(term))
		{
			result = (ArrayList<String>) formMap.get(term).clone();
		}
		else
		{
			result = new ArrayList<String>();
		}

		return result;
	}

	/**
	 * Returns all forms of provided term.
	 * 
	 * @param term must be a lemma
	 * @return ArrayList of forms
	 */
	public ArrayList<String> getFormList(String term)
	{
		if (lemmaMap.get(term) != null)
		{
			return lemmaMap.get(term).getForms();
		}
		else
		{
			return new ArrayList<String>();
		}
	}

	/**
	 * returns glosses for provided term.
	 * 
	 * @param term must be a lemma
	 * @return
	 */
	public ArrayList<String> getGlossList(String term)
	{
		return lemmaMap.get(term).getGlosses();
	}

	/**
	 * Returns ArrayList of parts of speech associated with a word as an element
	 * of a specific lemma (rather than as an element of any lemma, if word is
	 * in multiple lemmas)
	 * 
	 * @param term word of interest
	 * @param lemmaString specifies lemma to look in
	 * @return
	 */
	public ArrayList<String> getParts(String term, String lemmaString)
	{// limited to parts as form of a specific lemma
		Lemma lemma = lemmaMap.get(lemmaString);
		return lemma.getParts(term);
	}

	/**
	 * Returns ArrayList of all parts of speech associated with a word
	 * 
	 * @param term word of interest
	 * @return
	 */
	public ArrayList<String> getParts(String term)
	{// all parts as form of all associated lemmas
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> aLemmas = formMap.get(term);
		for (String lemmaString : aLemmas)
		{
			result.addAll(getParts(term, lemmaString));
		}
		return result;
	}

	/**
	 * Returns an ArrayList list of all parts of speech found in the dictionary
	 * 
	 * @return
	 */
	public ArrayList<String> getAllParts()
	{
		return (ArrayList<String>) allParts.clone();
	}

	/**
	 * 
	 *
	 */
	private class Lemma
	{
		private String header;
		private ArrayList<String> glosses;
		private ArrayList<String> formList;
		private HashMap<String, ArrayList<String>> forms;

		/**
		 * @param header
		 */

		/**
		 * Constructor
		 * 
		 * @param header word that identifies the lemma
		 */
		public Lemma(String header)
		{
			this.header = header;
			formList = new ArrayList<String>();
			glosses = new ArrayList<String>();
			glosses.add(header);
			forms = new HashMap<String, ArrayList<String>>();// form of lemma
																// ->list of
																// parts for
																// form
		}

		/**
		 * add a new word to lemma
		 * 
		 * @param newForm word to add
		 */
		public void addForm(String form, String part)
		{
			if (forms.containsKey(form))
			{
				ArrayList<String> list = forms.get(form);
				if (!list.contains(part))
				{
					list.add(part);
				}
			}
			else
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add(part);
				forms.put(form, list);
			}
		}

		/**
		 * add a gloss to lemma
		 * 
		 * @param newGloss gloss to add
		 */
		public void addGloss(String newGloss)
		{
			glosses.add(newGloss);
		}

		/**
		 * Getter for forms of lemma.
		 * 
		 * @return ArrayList of forms
		 */
		public ArrayList<String> getForms()
		{
			return new ArrayList<String>(forms.keySet());
		}

		/**
		 * Getter for glosses.
		 * 
		 * @return ArrayList<String> of glosses
		 */
		public ArrayList<String> getGlosses()
		{
			return (ArrayList<String>) glosses.clone();
		}

		/**
		 * Getter for header of lemma
		 * 
		 * @return header
		 */
		public String getHeader()
		{
			return header;
		}

		/**
		 * getter for parts associated with a particular form of the lemma
		 * 
		 * @param form word of interest
		 * @return
		 */
		public ArrayList<String> getParts(String form)
		{
			return (ArrayList<String>) forms.get(form).clone();
		}

		@Override public String toString()
		{
			StringBuilder sb = new StringBuilder(String.format("Header: %s\nGloss: ", header));
			for (String gloss : glosses)
			{
				sb.append(gloss);
				sb.append("; ");
			}
			sb.append("\nForms:");
			for (String form : formList)
			{
				sb.append(form + "; ");
			}

			return sb.toString();
		}

	}

	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (String entry : lemmas)
		{
			sb.append(lemmaMap.get(entry).toString());
			sb.append("\n\n");
		}
		return sb.toString();
	}

}
