package gui;

import auth.SessionManager;
import model.DataRecord;
import service.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for viewing and managing favorite records (Innovation #1).
 */
public class FavoritesPanel extends JPanel {
    private JTable favoritesTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton removeFavoriteButton;
    private JButton editNoteButton;
    private JLabel statusLabel;
    
    private UserService userService;
    private SessionManager sessionManager;
    
    /**
     * Constructs a new FavoritesPanel.
     * 
     * @param userService the user service
     * @param sessionManager the session manager
     */
    public FavoritesPanel(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
        
        initializeComponents();
        layoutComponents();
        attachEventHandlers();
        
        refreshFavorites();
    }
    
    private void initializeComponents() {
        String[] columnNames = {"ID", "Title", "Author", "Note"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        favoritesTable = new JTable(tableModel);
        favoritesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        refreshButton = new JButton("Refresh");
        removeFavoriteButton = new JButton("Remove from Favorites");
        editNoteButton = new JButton("Edit Note");
        statusLabel = new JLabel("Loading favorites...");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(removeFavoriteButton);
        buttonPanel.add(editNoteButton);
        add(buttonPanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(favoritesTable);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Status label
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void attachEventHandlers() {
        refreshButton.addActionListener(e -> refreshFavorites());
        removeFavoriteButton.addActionListener(e -> removeFavorite());
        editNoteButton.addActionListener(e -> editNote());
    }
    
    private void refreshFavorites() {
        List<DataRecord> favorites = userService.getFavorites();
        updateTable(favorites);
        
        if (favorites.isEmpty()) {
            statusLabel.setText("No favorites yet. Mark records as favorites from search results.");
        } else {
            statusLabel.setText("You have " + favorites.size() + " favorite(s).");
        }
    }
    
    private void updateTable(List<DataRecord> favorites) {
        tableModel.setRowCount(0);
        
        for (DataRecord record : favorites) {
            String note = userService.getNote(record.getId());
            Object[] row = {
                record.getId(),
                record.getTitle(),
                record.getAuthor(),
                note
            };
            tableModel.addRow(row);
        }
    }
    
    private void removeFavorite() {
        int selectedRow = favoritesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a record to remove from favorites.", 
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String recordId = (String) tableModel.getValueAt(selectedRow, 0);
        userService.toggleFavorite(recordId);
        refreshFavorites();
    }
    
    private void editNote() {
        int selectedRow = favoritesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a record to edit note.", 
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String recordId = (String) tableModel.getValueAt(selectedRow, 0);
        String currentNote = userService.getNote(recordId);
        
        String newNote = JOptionPane.showInputDialog(this, 
            "Enter note for this record:", currentNote);
        
        if (newNote != null) {
            userService.updateNote(recordId, newNote);
            refreshFavorites();
        }
    }
}

