import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class MainWindow
{

	private JFrame frame;
	private DefaultTableModel tableModel;
	private JTextField textField;
	private JTable table;
	private JTable list;
	private Dictionary dict;
	private DefaultTableModel lemmaList;
	private ArrayList<String> lemmas;
	private Corpus corpus;
	private String lemma;
	private String position;
	private JLabel topLabel;
	private Properties settings;
	private HashMap<String, Integer> results;
	private HashMap<String, Double> advResults;
	private String result;
	
	private static final String PROPERTIES_FILE = "PatternSearch.properties";
	private static final String ICON_FILE = "res/Icon.png";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{

		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try
				{
					Properties settings = new Properties();
					File settingsFile = new File(PROPERTIES_FILE);
					MainWindow window = new MainWindow(settings);
					if (settingsFile.exists())
					{
						FileReader reader = new FileReader(settingsFile);
						settings.load(reader);
						String dictLocation = settings.getProperty("dictionary");
						String corpusLocation = settings.getProperty("corpus");
						if (dictLocation != null)
						{
							window.newDict(dictLocation);
						}
						if (corpusLocation != null)
						{
							window.newCorpus(corpusLocation);
						}
					}
					window.frame.setVisible(true);
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @param settings	The Properties object that contains the information for dictionary and transcription files
	 * 
	 * @throws IOException
	 */
	public MainWindow(Properties settings) throws IOException
	{
		this.settings = settings;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException
	{
		frame = new JFrame("Mixtec Pattern Search");
		frame.setBounds(100, 100, 1000, 800);
		URL url = MainWindow.class.getResource(ICON_FILE); 
		Image image = Toolkit.getDefaultToolkit().getImage(url);
		frame.setIconImage(new ImageIcon(image).getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up menu bar
		JMenuBar menuBar = new JMenuBar();
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenuItem mntmFile = new JMenuItem("Load Transcriptions");
		mntmFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					try
					{
						newCorpus(chooser.getSelectedFile().getAbsolutePath());
					} catch (UnsupportedEncodingException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		mnNewMenu.add(mntmFile);

		JMenuItem mntmDict = new JMenuItem("Load Dictionary");

		mntmDict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					newDict(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		mnNewMenu.add(mntmDict);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		panel.add(splitPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(BorderFactory.createLineBorder(Color.black));
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.NORTH);

		textField = new JTextField();
		panel_3.add(textField);
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setColumns(22);
		textField.setEnabled(false);
		textField.setText("No Dictionary Loaded");

		list = new JTable();

		list.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e)
			{
				textField.setText("");
			}
		});
		JScrollPane listScroll = new JScrollPane(list);
		panel_1.add(listScroll, BorderLayout.CENTER);

		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4, BorderLayout.SOUTH);
		panel_4.setLayout(new BorderLayout(0, 0));

		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("preceding");
		comboBox.addItem("following");
		comboBox.addItem("both");
		comboBox.setSelectedIndex(0);
		panel_4.add(comboBox, BorderLayout.CENTER);

		JButton btnSearch = new JButton("Search");
		panel_4.add(btnSearch, BorderLayout.SOUTH);

		JLabel lblNewLabel = new JLabel("Search for words:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblNewLabel, BorderLayout.NORTH);

		textField.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e)
			{
				if (dict == null)
				{
					textField.setText("");
					JOptionPane.showMessageDialog(frame, "Please load dictionary first.", "Load Dictionary",
							JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					String text = textField.getText();
					lemmas = dict.getLemmaList(text);
					lemmaList = new DefaultTableModel();
					lemmaList.addColumn("Lemma");
					lemmaList.addColumn("Gloss");
					for (int i = 0; i < lemmas.size(); i++)
					{
						ArrayList<String> glosses = dict.getGlossList(lemmas.get(i));
						glosses.remove(lemmas.get(i));
						String gloss = joinList(glosses, ", ");
						String[] data = { lemmas.get(i), gloss };
						lemmaList.addRow(data);
					}
					list.setModel(lemmaList);
					list.updateUI();
				}
			}
		});

		JPanel panel_2 = new JPanel();
		splitPane.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		table = new JTable();
		JScrollPane tableScroll = new JScrollPane(table);
		panel_2.add(tableScroll);

		JButton btnAdvancedSearch = new JButton("Advanced Search");
		JButton filterBtn = new JButton();

		JPanel panel_5 = new JPanel();
		panel_2.add(panel_5, BorderLayout.SOUTH);
		panel_5.add(filterBtn);
		panel_5.add(btnAdvancedSearch);

		btnAdvancedSearch.setEnabled(false);
		filterBtn.setEnabled(false);

		filterBtn.setText("Filter Results");

		filterBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				filterOptions();
			}
		});

		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnAdvancedSearch.setEnabled(true);
				btnAdvancedSearch.setText("Advanced Search");
				if (dict == null)
				{
					JOptionPane.showMessageDialog(frame, "Please load dictionary first.", "Load Dictionary",
							JOptionPane.ERROR_MESSAGE);
				}
				else if (corpus == null)
				{
					JOptionPane.showMessageDialog(frame, "Please load transcriptions first.", "Load Transcriptions",
							JOptionPane.ERROR_MESSAGE);
				}
				else if (list.getSelectedRow() == -1)
				{
					JOptionPane.showMessageDialog(frame, "Please select a lemma to search.", "Select Search Term",
							JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					lemma = "";
					if (textField.getText().equals(""))
					{
						TableModel m = list.getModel();
						int i = list.getSelectedRow();
						lemma = (String) m.getValueAt(i, 0);
					}
					else
					{
						lemma = textField.getText();
					}

					position = (String) comboBox.getSelectedItem();

					try
					{
						results = Search.search(lemma, position, dict);
					} catch (FileNotFoundException e1)
					{
						e1.printStackTrace();
					} catch (IOException e1)
					{
						e1.printStackTrace();
					}

					updateTable();
				}
				filterBtn.setEnabled(true);
			}

		});

		btnAdvancedSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (btnAdvancedSearch.getText().equals("Advanced Search"))
				{
					int row = table.getSelectedRow();
					if (row == -1)
					{
						JOptionPane.showMessageDialog(frame,
								"Please select a row in the table to do an advanced search with.",
								"Select Advanced Search Term", JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						Object[] possibilities = { "0", "1", "2", "3", "4" };
						String s = (String) JOptionPane.showInputDialog(frame,
								"Select how many words you want inbetween:\n", "Advanced Search",
								JOptionPane.PLAIN_MESSAGE, null, possibilities, "0");

						int numBtwn;
						try
						{
							numBtwn = Integer.parseInt(s);

						} catch (Exception ex)
						{
							return;
						}

						result = (String) table.getValueAt(row, 0);

						advResults = AdvancedSearch.advancedSearch(lemma, result, numBtwn, position, dict);
						updateAdvancedSearchTable(numBtwn);
						btnAdvancedSearch.setText("Clear Advanced Search");
					}
				}
				else
				{
					btnAdvancedSearch.setText("Advanced Search");
					updateTable();
				}
			}
		});

		topLabel = new JLabel();
		topLabel.setText("No Corpus Loaded");
		panel_2.add(topLabel, BorderLayout.NORTH);
	}

	/**
	 * This method is called when the advanced search method is called and
	 * updates the table to show the results for an advanced search.
	 * 
	 * @param numBtwn number of words between the lemma and search term
	 */
	public void updateAdvancedSearchTable(int numBtwn)
	{
		for (String key : advResults.keySet())
		{
			if (!key.equals("TERM_TOTAL"))
			{
				Double freq = advResults.get(key) / advResults.get("TERM_TOTAL");
				advResults.put(key, freq);
			}
		}
		advResults.remove("TERM_TOTAL");
		tableModel = new DefaultTableModel();
		tableModel.addColumn("Appearing with search term");
		tableModel.addColumn("Frequency");
		for (String key : advResults.keySet())
		{
			NumberFormat defaultFormat = NumberFormat.getPercentInstance();
			String[] arr = { key, new DecimalFormat("##.#").format(advResults.get(key) * 100) };
			tableModel.addRow(arr);
		}

		ArrayList<String> glosses = dict.getGlossList(lemma);
		glosses.remove(lemma);

		topLabel.setText("Found the pair \"" + lemma + " (" + joinList(glosses, ",") + ") " + result + "\" was found "
				+ results.keySet().size() + " times with up to " + numBtwn + " words inbetween.");
		table.setModel(tableModel);
		DefaultRowSorter sorter = new TableRowSorter(tableModel);
		table.setRowSorter(sorter);
	}

	/**
	 * This method is called when the search method is called and updates the
	 * table to show the results for an advanced search.
	 */
	public void updateTable()
	{
		int total = results.get("TERM_TOTAL");
		tableModel = new DefaultTableModel() {
			@Override public Class getColumnClass(int column)
			{
				if (column == 2)
				{
					return Double.class;
				}
				else
				{
					return String.class;
				}
			}
		};
		tableModel.addColumn("Appearing with search term");
		tableModel.addColumn("Gloss");
		tableModel.addColumn("Frequency (%)");
		tableModel.addColumn("Parts of Speech");

		DecimalFormat format = new DecimalFormat("##.###");
		for (String key : results.keySet())
		{
			if (!key.equals("TERM_TOTAL"))
			{
				String gloss = "";
				String parts = "";
				if (dict.getLemmaList().contains(key))
				{
					ArrayList<String> glosses = dict.getGlossList(key);
					glosses.remove(key);
					gloss = joinList(glosses, ", ");
					ArrayList<String> partsList = dict.getParts(key, key);
					parts = joinList(partsList, ", ");
				}

				String freqString = format.format((results.get(key) / (double) total) * 100);
				Object[] arr = { key, gloss, Double.parseDouble(freqString), parts };
				tableModel.addRow(arr);
			}
		}
		String gloss = "";
		ArrayList<String> glosses = dict.getGlossList(lemma);
		glosses.remove(lemma);
		gloss = joinList(glosses, ", ");
		if (gloss.equals(""))
		{

			topLabel.setText("Found the lemma \"" + lemma + "\" " + total + " times.");

		}
		else
		{

			topLabel.setText("Found the lemma \"" + lemma + "\" (" + gloss + ") " + total + " times.");

		}
		table.setModel(tableModel);
		DefaultRowSorter sorter = new TableRowSorter(tableModel);
		table.setRowSorter(sorter);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		table.getColumn("Frequency (%)").setCellRenderer(rightRenderer);
	}

	/**
	 * Creates a corpus when the location of the transcriptions is updated
	 * 
	 * @param filename path to the folder enclosing the transcription files
	 * @throws UnsupportedEncodingException
	 */
	public void newCorpus(String filename) throws UnsupportedEncodingException
	{
		corpus = new Corpus(filename);
		new Search(corpus);
		if (corpus.getFiles().isEmpty())
		{
			JOptionPane.showMessageDialog(frame, "No Readable Transcriptions Found.", "No Transcriptions",
					JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			topLabel.setText("Transcriptions Loaded");
			settings.setProperty("corpus", filename);
			saveSettings();
		}
	}

	/**
	 * Creates a dictionary object when the user updated the location of the
	 * dictionary file.
	 * 
	 * @param filename path to the dictionary file
	 */
	public void newDict(String filename)
	{
		try
		{
			dict = new Dictionary(filename);
			lemmaList = new DefaultTableModel();
			lemmaList.addColumn("Lemma");
			lemmaList.addColumn("Gloss");
			lemmas = dict.getLemmaList();
			for (int i = 0; i < lemmas.size(); i++)
			{
				ArrayList<String> glosses = dict.getGlossList(lemmas.get(i));
				glosses.remove(lemmas.get(i));
				String gloss = joinList(glosses, ", ");
				String[] data = { lemmas.get(i), gloss };
				lemmaList.addRow(data);
			}
			list.setModel(lemmaList);
			list.updateUI();
			settings.setProperty("dictionary", filename);
			saveSettings();
			textField.setEnabled(true);
			textField.setText("");
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Saves the location of the transcriptions and the dictionary file to
	 * setting files easily accessible next time the user open the program.
	 */
	public void saveSettings()
	{
		File settingsFile = new File(PROPERTIES_FILE);
		try
		{
			FileWriter writer = new FileWriter(settingsFile);
			settings.store(writer, null);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates a dialogue to allow the user to filer the results based off of
	 * the part of the speech the word is.
	 */
	public void filterOptions()
	{
		List<String> filterList = FilterDialog.showDialog(frame, dict.getAllParts());
		if (filterList.isEmpty())
		{
			table.setModel(tableModel);
			tableModel.fireTableStructureChanged();
			table.setRowSorter(new TableRowSorter(tableModel));
		}
		else
		{
			DefaultTableModel filteredModel = new DefaultTableModel() {
				@Override public Class getColumnClass(int column)
				{
					if (column == 2)
					{
						return Double.class;
					}
					else
					{
						return String.class;
					}
				}
			};
			for (int col = 0; col < tableModel.getColumnCount(); col++)
			{
				filteredModel.addColumn(tableModel.getColumnName(col));
			}
			for (int row = 0; row < tableModel.getRowCount(); row++)
			{
				String resultParts = (String) tableModel.getValueAt(row, 3);
				boolean include = false;
				for (String part : filterList)
				{
					Pattern p = Pattern.compile("(^|\\s)" + part + "(,|$)");
					Matcher m = p.matcher(resultParts);
					if (m.find())
					{
						include = true;
					}
				}
				if (include)
				{
					Object[] rowData = new Object[tableModel.getColumnCount()];
					for (int col = 0; col < tableModel.getColumnCount(); col++)
					{
						rowData[col] = tableModel.getValueAt(row, col);
					}
					filteredModel.addRow(rowData);
				}
			}

			table.setModel(filteredModel);
			filteredModel.fireTableStructureChanged();
			table.setRowSorter(new TableRowSorter(filteredModel));
		}

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		table.getColumn("Frequency (%)").setCellRenderer(rightRenderer);

	}

	/**
	 * Method to join the indices of a list separated by some string.
	 * 
	 * @param list list to combine into one string
	 * @param literal what to each string in the list with
	 * @return the string created by joining the strings of the list together
	 */
	public static String joinList(List list, String literal)
	{
		return list.toString().replaceAll(",", literal).replaceAll("[\\[.\\].\\s+]", "");
	}
}