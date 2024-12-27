import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class CinemaTicketApp {

    private JFrame frame;
    private JTextField txtName;
    private JComboBox<String> cmbMovie, cmbPrice, cmbTime;
    private JSpinner spnTicketCount;
    private ArrayList<JButton> seatButtons;
    private DefaultTableModel tableModel;
    private JTable ticketTable;

    // Key: "movie_time_price", Value: Set of booked seats
    private Map<String, Set<String>> bookedSeatsByMovieTimePrice;

    // Temporary storage for selected seats
    private Set<String> selectedSeats;

    public CinemaTicketApp() {
        frame = new JFrame("BOOKING TIKET MINI CINEMA");
        frame.setSize(1300, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        bookedSeatsByMovieTimePrice = new HashMap<>();
        selectedSeats = new HashSet<>();

        // Panel atas untuk input data
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Informasi Pemesanan"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Baris 1: Nama Pemesan dan Film
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Nama Pemesan:"), gbc);

        txtName = new JTextField();
        gbc.gridx = 1;
        topPanel.add(txtName, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Film:"), gbc);

        cmbMovie = new JComboBox<>(new String[]{"PEMBURU IBLIS-STUDIO 1", "SPIDERMAN-STUDIO 2", "DEADPOOL-STUDIO 3"});
        gbc.gridx = 3;
        topPanel.add(cmbMovie, gbc);

        // Baris 2: Harga Tiket dan Jam Tayang
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Harga Tiket:"), gbc);

        cmbPrice = new JComboBox<>(new String[]{"EXECUTIVE-30000", "DELUXE-50000"});
        gbc.gridx = 1;
        topPanel.add(cmbPrice, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Jam Tayang:"), gbc);

        cmbTime = new JComboBox<>(new String[]{"13:00 - 15:30", "16:00 - 18:30", "19:00 - 21:30"});
        gbc.gridx = 3;
        topPanel.add(cmbTime, gbc);

        // Baris 3: Jumlah Tiket
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("Jumlah Tiket:"), gbc);

        spnTicketCount = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        gbc.gridx = 3;
        topPanel.add(spnTicketCount, gbc);

        frame.add(topPanel, BorderLayout.NORTH);

        // Listener untuk update tampilan kursi
        cmbMovie.addActionListener(e -> updateSeatColors());
        cmbTime.addActionListener(e -> updateSeatColors());
        cmbPrice.addActionListener(e -> updateSeatColors());

        // Panel tengah untuk tata letak kursi
        JPanel seatPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        seatPanel.setBorder(BorderFactory.createTitledBorder("Pilih Kursi"));
        seatButtons = new ArrayList<>();

        String[] seatLabels = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3", "D1", "D2", "D3"};
        for (String label : seatLabels) {
            JButton seatButton = new JButton(label);
            seatButton.setBackground(Color.GREEN);
            seatButtons.add(seatButton);
            seatPanel.add(seatButton);

            seatButton.addActionListener(e -> {
                if (seatButton.getBackground() == Color.GREEN) {
                    selectedSeats.add(label);
                    seatButton.setBackground(Color.YELLOW); // Warna kuning untuk kursi yang dipilih
                } else if (seatButton.getBackground() == Color.YELLOW) {
                    selectedSeats.remove(label);
                    seatButton.setBackground(Color.GREEN);
                }
            });
        }

        frame.add(seatPanel, BorderLayout.CENTER);

        // Panel bawah untuk tombol aksi
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Aksi"));

        JButton btnPesan = new JButton("Pesan");
        btnPesan.addActionListener(e -> {
            String name = txtName.getText().trim();
            String movie = (String) cmbMovie.getSelectedItem();
            String time = (String) cmbTime.getSelectedItem();
            String priceString = (String) cmbPrice.getSelectedItem();
            int ticketCount = (Integer) spnTicketCount.getValue();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Nama pemesan harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedSeats.size() != ticketCount) {
                JOptionPane.showMessageDialog(frame, "Jumlah kursi yang dipilih harus sesuai dengan jumlah tiket.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Memisahkan harga tiket dari string (contoh: EXECUTIVE-30000)
            String[] priceParts = priceString.split("-");
            int ticketPrice = Integer.parseInt(priceParts[1]);
            int totalPrice = ticketPrice * ticketCount;

            String movieTimeKey = movie + "" + time + "" + priceString;
            bookedSeatsByMovieTimePrice.putIfAbsent(movieTimeKey, new HashSet<>());
            Set<String> bookedSeats = bookedSeatsByMovieTimePrice.get(movieTimeKey);
            bookedSeats.addAll(selectedSeats);

            // Update table model with new booking
            tableModel.addRow(new Object[]{name, movie, time, ticketCount, String.join(", ", selectedSeats), totalPrice});
            JOptionPane.showMessageDialog(frame, "Tiket berhasil dipesan untuk " + name + ". Total Harga: " + totalPrice, "Success", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
        });
        bottomPanel.add(btnPesan);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(e -> {
            int selectedRow = ticketTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Pilih baris yang akan diperbarui.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String oldSeats = (String) tableModel.getValueAt(selectedRow, 4);
            String[] oldSeatArray = oldSeats.split(", ");

            String name = txtName.getText().trim();
            String movie = (String) cmbMovie.getSelectedItem();
            String time = (String) cmbTime.getSelectedItem();
            String priceString = (String) cmbPrice.getSelectedItem();
            int ticketCount = (Integer) spnTicketCount.getValue();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Nama pemesan harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedSeats.size() != ticketCount) {
                JOptionPane.showMessageDialog(frame, "Jumlah kursi yang dipilih harus sesuai dengan jumlah tiket.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Memisahkan harga tiket dari string (contoh: EXECUTIVE-30000)
            String[] priceParts = priceString.split("-");
            int ticketPrice = Integer.parseInt(priceParts[1]);
            int totalPrice = ticketPrice * ticketCount;

            String movieTimeKey = movie + "" + time + "" + priceString;
            bookedSeatsByMovieTimePrice.putIfAbsent(movieTimeKey, new HashSet<>());
            Set<String> bookedSeats = bookedSeatsByMovieTimePrice.get(movieTimeKey);

            // Remove old seats from booked seats and update their colors
            for (String oldSeat : oldSeatArray) {
                bookedSeats.remove(oldSeat);
                for (JButton seatButton : seatButtons) {
                    if (seatButton.getText().equals(oldSeat) && !selectedSeats.contains(oldSeat)) {
                        seatButton.setBackground(Color.GREEN); // Return to green for unselected
                    }
                }
            }

            // Add new selected seats to booked seats
            bookedSeats.addAll(selectedSeats);

            // Update row data
            tableModel.setValueAt(name, selectedRow, 0);
            tableModel.setValueAt(movie, selectedRow, 1);
            tableModel.setValueAt(time, selectedRow, 2);
            tableModel.setValueAt(ticketCount, selectedRow, 3);
            tableModel.setValueAt(String.join(", ", selectedSeats), selectedRow, 4);
            tableModel.setValueAt(totalPrice, selectedRow, 5);

            JOptionPane.showMessageDialog(frame, "Tiket berhasil diperbarui untuk " + name + ". Total Harga: " + totalPrice, "Success", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
        });
        bottomPanel.add(btnUpdate);

        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> {
            int selectedRow = ticketTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Pilih baris yang akan dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get selected seats from the row to remove them
            String oldSeats = (String) tableModel.getValueAt(selectedRow, 4);
            String[] oldSeatArray = oldSeats.split(", ");

            String movie = (String) tableModel.getValueAt(selectedRow, 1);
            String time = (String) tableModel.getValueAt(selectedRow, 2);
            String priceString = (String) cmbPrice.getSelectedItem();
            String movieTimeKey = movie + "" + time + "" + priceString;

            // Remove the booked seats from the stored data
            Set<String> bookedSeats = bookedSeatsByMovieTimePrice.get(movieTimeKey);
            if (bookedSeats != null) {
                for (String seat : oldSeatArray) {
                    bookedSeats.remove(seat);

                    // Change the seat color back to green
                    for (JButton seatButton : seatButtons) {
                        if (seatButton.getText().equals(seat)) {
                            seatButton.setBackground(Color.GREEN); // Reset to green
                        }
                    }
                }
            }

            // Remove the row from the table
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(frame, "Pemesan berhasil dihapus.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        bottomPanel.add(btnHapus);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Table for ticket bookings
        tableModel = new DefaultTableModel(new String[]{"Nama", "Film", "Jam Tayang", "Jumlah Tiket", "Kursi", "Total Harga"}, 0);
        ticketTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(ticketTable);
        frame.add(tableScroll, BorderLayout.EAST);

        frame.setVisible(true);
    }

    private void updateSeatColors() {
        // Reset all seat buttons to green
        for (JButton seatButton : seatButtons) {
            seatButton.setBackground(Color.GREEN);
        }

        String movie = (String) cmbMovie.getSelectedItem();
        String time = (String) cmbTime.getSelectedItem();
        String priceString = (String) cmbPrice.getSelectedItem();
        String movieTimeKey = movie + "" + time + "" + priceString;

        // Get booked seats for the current movie, time, and price
        Set<String> bookedSeats = bookedSeatsByMovieTimePrice.get(movieTimeKey);
        if (bookedSeats != null) {
            // Mark booked seats with red color
            for (String bookedSeat : bookedSeats) {
                for (JButton seatButton : seatButtons) {
                    if (seatButton.getText().equals(bookedSeat)) {
                        seatButton.setBackground(Color.RED); // Red for booked seats
                    }
                }
            }
        }
    }

    private void resetForm() {
        txtName.setText("");
        cmbMovie.setSelectedIndex(0);
        cmbTime.setSelectedIndex(0);
        cmbPrice.setSelectedIndex(0);
        spnTicketCount.setValue(1);
        selectedSeats.clear();
        updateSeatColors();
    }

    public static void main(String[] args) {
        new CinemaTicketApp();
    }
}
