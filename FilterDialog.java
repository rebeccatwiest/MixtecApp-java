import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class FilterDialog extends JDialog implements ActionListener
{
	private static List<String> results;
	private JList<String> list;

	public static List<String> showDialog(JFrame frame, ArrayList<String> parts)
	{
		results = new ArrayList<String>();
		new FilterDialog(frame, parts);
		return results;
	}

	public FilterDialog(JFrame frame, ArrayList<String> parts)
	{
		super(frame, "Filter Options");
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setLocationRelativeTo(frame);
		Container pane = getContentPane();

		JPanel lPanel = new JPanel();
		JLabel label = new JLabel("Select Parts of Speech to Include (ctrl or shift click to select multiples)");
		lPanel.add(label);
		pane.add(lPanel, BorderLayout.NORTH);

		list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DefaultListModel model = new DefaultListModel();
		Collections.sort(parts);
		for (String part : parts)
		{
			model.addElement(part);
		}
		list.setModel(model);
		JScrollPane scrollPane = new JScrollPane(list);
		JPanel listPanel = new JPanel();
		listPanel.add(scrollPane);
		pane.add(listPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout());

		JButton filterButton = new JButton();
		filterButton.setText("Filter");
		filterButton.setActionCommand("Filter");
		filterButton.addActionListener(this);
		buttonPanel.add(filterButton);

		JButton cancelButton = new JButton();
		cancelButton.setText("Clear Filter");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		pane.add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	@Override public void actionPerformed(ActionEvent event)
	{
		if ("Filter".equals(event.getActionCommand()))
		{
			results = list.getSelectedValuesList();
		}
		setVisible(false);
	}

}
