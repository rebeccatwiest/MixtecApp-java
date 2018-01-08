import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * Creates the corpus which contains all transcriptions files put in an
 * ArrayList so it can be searched for collocations
 */
public class Corpus
{

	private String directoryName;
	public ArrayList<String> corpus;
	private ArrayList<File> transFiles;

	/**
	 * Corpus constructor
	 * 
	 * @param directoryName - Absolute path to directory of transcription files
	 */
	public Corpus(String directoryName)
	{
		this.directoryName = directoryName;
		corpus = new ArrayList<String>();
		transFiles = new ArrayList<File>();

		listFiles(this.directoryName, transFiles);
		showFiles(transFiles);
	}

	/**
	 * Updates files to include all files in the directory and sub directories
	 * with .trs and .eaf extension
	 * 
	 * @param directoryName - absolute path to directory of transcription files
	 * @param files - ArrayList of transcription files
	 */
	public void listFiles(String directoryName, ArrayList<File> files)
	{
		File directory = new File(directoryName);
		File[] fList = directory.listFiles();

		for (File file : fList)
		{
			if (file.isFile())
			{
				if (file.getName().endsWith(".trs") || file.getName().endsWith(".eaf"))
				{
					files.add(file);
				}
			}
			else if (file.isDirectory())
			{
				listFiles(file.getAbsolutePath(), files); // sub directory name

			}
		}
	}

	/**
	 * Concatenates file contents into string, and adds strings to corpus
	 * ArrayList
	 * 
	 * @param files - ArrayList of .trs and .eaf files
	 */
	public void showFiles(ArrayList<File> files)
	{
		for (File file : files)
		{
			try
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));

				String line;
				String str = "";
				String trimLine;

				try
				{
					while ((line = br.readLine()) != null)
					{
						trimLine = line.trim();
						if (trimLine.startsWith("</Turn"))
						{
							corpus.add(str);
							str = "";
						}
						else if (!trimLine.startsWith("<"))
						{
							str = str + trimLine + " ";
						}
					}
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Getter for corpus generated from transcription files
	 * 
	 * @return corpus - ArrayList of Strings containing transcriptions
	 */
	public ArrayList<String> getCorpus()
	{
		return corpus;
	}

	/**
	 * Getter for files
	 * 
	 * @return transFiles - ArrayList of Files containing .trs and .eaf files
	 */
	public ArrayList<File> getFiles()
	{
		return transFiles;
	}
}