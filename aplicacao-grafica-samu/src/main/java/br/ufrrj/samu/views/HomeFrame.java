package br.ufrrj.samu.views;

import br.ufrrj.samu.SAMU;
import br.ufrrj.samu.entities.*;
import br.ufrrj.samu.entities.Semester.CurrentStatus;
import br.ufrrj.samu.utils.Util;
import br.ufrrj.samu.views.listeners.LogoutListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static br.ufrrj.samu.utils.Util.centreWindow;
import static java.util.Objects.requireNonNull;

public class HomeFrame extends JFrame {

    private static final Logger LOGGER = LogManager.getLogger(HomeFrame.class);

    private String frameTitle = "SAMU - Sistema de Aux\u00EDlio a Matr\u00EDcula Universit\u00E1ria";
    private int width = 1366;
    private int height = 720;

    JPanel mainJPanel;

    JTable concludedTable;

    private User user;
    private JButton logoutButton;
    private JButton avaliarDisciplinasButton;
    private JButton realizarMatriculaButton;
    private JTabbedPane tabbedPane;
    private JTable requestedTable;
    private JTable enrolledTable;
    private JTable teachingTable;
    private LogoutListener logoutListener;
    private CurrentStatus currentStatus;
    private JLabel currentStatusSemester;
    private JButton confirmRequestedLecturesButton;

    public HomeFrame(User user, SAMU samu) throws HeadlessException {
        super();
        frameInit();
        this.user = user;
        this.currentStatus = CurrentStatus.ENROLLMENT;

        mainJPanel = new JPanel();
        mainJPanel.setLayout(new BorderLayout());
        mainJPanel.setBackground(Color.GREEN);

        initLeftSite();
        initRightSite();

        this.add(mainJPanel);
        this.setVisible(true);
        konamiCode(this);
    }

    public void konamiCode(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JTable)
                c.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_K) {
                            if (currentStatus == CurrentStatus.ENROLLMENT) {
                                if (user instanceof Student) {
                                    realizarMatriculaButton.setEnabled(false);
                                    avaliarDisciplinasButton.setEnabled(false);
                                } else if (user instanceof Coordinator) {
                                    confirmRequestedLecturesButton.setEnabled(false);
                                }
                                currentStatus = CurrentStatus.ONGOING;
                            } else if (currentStatus == CurrentStatus.ONGOING) {
                                if (user instanceof Student) {
                                    realizarMatriculaButton.setEnabled(false);
                                    avaliarDisciplinasButton.setEnabled(true);
                                }
                                currentStatus = CurrentStatus.CONCLUDED;
                            } else {
                                if (user instanceof Student) {
                                    realizarMatriculaButton.setEnabled(true);
                                    avaliarDisciplinasButton.setEnabled(false);
                                } else if (user instanceof Coordinator) {
                                    confirmRequestedLecturesButton.setEnabled(true);
                                }
                                currentStatus = CurrentStatus.ENROLLMENT;
                            }
                            currentStatusSemester.setText("Semestre: " + currentStatus.getStatus());
                        }
                    }
                });

            if (c instanceof Container) {
                konamiCode((Container) c);
            }
        }
    }

    private void initLeftSite() {
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setPreferredSize(new Dimension(250, height));
        userInfoPanel.setLayout(new GridBagLayout());
//        userInfoPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("SAMU.homeBorderColor"), 2));
        userInfoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, UIManager.getColor("Button.default.startBorderColor")));

        JLabel userImage = new JLabel();
        userImage.setBackground(Color.WHITE);
        userImage.setOpaque(false);
        userImage.setSize(new Dimension(150, 150));
        userImage.setIcon(requireNonNull(Util.getImageWidth("images/userImage.png", 128,128)));

        JLabel username = new JLabel("Nome: " + user.getName());
        JLabel enrollment = new JLabel("Matr\u00EDcula: " + String.format("%s", user.getCpf()));
        JLabel course = new JLabel("Curso: " + user.getCourse().getName());
        JLabel semester = new JLabel("Semestre atual: " + Util.getCurrentSemester());
        currentStatusSemester = new JLabel("Semestre: " + this.currentStatus.getStatus());

        GridBagConstraints gridConstraints = new GridBagConstraints();

        gridConstraints.anchor = GridBagConstraints.CENTER;
        gridConstraints.weighty = 0.05;

        gridConstraints.gridy = 0;
        gridConstraints.gridx = 0;
        gridConstraints.fill = GridBagConstraints.NONE; /* Troquei para NONE, o both tava fazendo ficar com a parte branca bem maior */
        gridConstraints.insets = new Insets(0, 0, 10, 4);
        userInfoPanel.add(userImage, gridConstraints);

        gridConstraints.insets = new Insets(0, 0, 0, 0);

        gridConstraints.fill = GridBagConstraints.NONE;
        gridConstraints.gridy++;
        userInfoPanel.add(username, gridConstraints);

        gridConstraints.gridy++;
        userInfoPanel.add(enrollment, gridConstraints);

        gridConstraints.gridy++;
        userInfoPanel.add(course, gridConstraints);

        gridConstraints.gridy++;
        userInfoPanel.add(semester, gridConstraints);

        gridConstraints.gridy++;
        userInfoPanel.add(currentStatusSemester, gridConstraints);

        gridConstraints.weighty = 0.08;
        gridConstraints.gridy++;
        gridConstraints.insets = new Insets(20,0,0,0);
        userInfoPanel.add(new JLabel("Menu de Navega\u00E7\u00E3o"), gridConstraints);

        /* Zerei o espaçamento entre os botões e coloquei um preenchimento maneiro */
        gridConstraints.weighty = 0;
        gridConstraints.insets = new Insets(0,0,0,0);
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

        if (user instanceof Student) {
            gridConstraints.gridy++;
            realizarMatriculaButton = new JButton("Realizar Matr\u00EDcula");
            realizarMatriculaButton.setFocusable(false);
            realizarMatriculaButton.setFont(realizarMatriculaButton.getFont().deriveFont(15f));
            realizarMatriculaButton.setFont(realizarMatriculaButton.getFont().deriveFont(15f));
            realizarMatriculaButton.addActionListener(e -> {
                EnrollFrame enrollFrame = new EnrollFrame();
                enrollFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        LOGGER.debug("Closing HomeFrame window");
                        refreshData();
                    }
                });
            });
            userInfoPanel.add(realizarMatriculaButton, gridConstraints);

            gridConstraints.gridy++;
            avaliarDisciplinasButton = new JButton("Avaliar Disciplinas");
            avaliarDisciplinasButton.setFocusable(false);
            avaliarDisciplinasButton.setEnabled(false);
            avaliarDisciplinasButton.setFont(avaliarDisciplinasButton.getFont().deriveFont(15f));
            avaliarDisciplinasButton.addActionListener(e -> {
                new EvaluationFrame();
            });
            userInfoPanel.add(avaliarDisciplinasButton, gridConstraints);
        } else if (user instanceof Coordinator) {
            gridConstraints.gridy++;
            confirmRequestedLecturesButton = new JButton("Confirmar Matr\u00EDculas");
            confirmRequestedLecturesButton.setFocusable(false);
            confirmRequestedLecturesButton.setFont(confirmRequestedLecturesButton.getFont().deriveFont(15f));
            confirmRequestedLecturesButton.setFont(confirmRequestedLecturesButton.getFont().deriveFont(15f));
            ConfirmFrame confirmFrame = new ConfirmFrame();
            confirmRequestedLecturesButton.addActionListener(e -> {
                confirmFrame.setVisible(true);
                System.out.println("confirmar matrículas");
            });
            userInfoPanel.add(confirmRequestedLecturesButton, gridConstraints);

            gridConstraints.gridy++;
            JButton generateReportButton = new JButton("Gerar Relat\u00F3rio");
            generateReportButton.setFocusable(false);
            generateReportButton.setEnabled(true);
            generateReportButton.setFont(generateReportButton.getFont().deriveFont(15f));
            generateReportButton.addActionListener(e -> {
                System.out.println("gerar relatório");
            });
            userInfoPanel.add(generateReportButton, gridConstraints);
        }

        gridConstraints.gridy++;
        gridConstraints.weighty = 2.0;
        logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        logoutButton.setEnabled(true);
        logoutButton.setFont(logoutButton.getFont().deriveFont(15f));
        logoutButton.addActionListener(e -> {
            if (logoutListener != null) {
                this.dispose();
                logoutListener.logoutEventOccurred();
            }
        });
        userInfoPanel.add(logoutButton, gridConstraints);

        mainJPanel.add(userInfoPanel, BorderLayout.WEST);
    }

    private void initRightSite() {
        JPanel rightSidePanel = new JPanel();
        rightSidePanel.setLayout(new GridBagLayout());

        JPanel tableJPanel = new JPanel();
        tableJPanel.setLayout(new BorderLayout());

        if (user instanceof Student) {
            initStudentRightSide(rightSidePanel, tableJPanel);
        } else if (user instanceof Coordinator) {
            initCoordinatorRightSide(rightSidePanel, tableJPanel);
        }


        mainJPanel.add(rightSidePanel, BorderLayout.CENTER);
    }

    private void initCoordinatorRightSide(JPanel rightSidePanel, JPanel tableJPanel) {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        JScrollPane scrollPaneTeachingLectures = initTeachingLecturesTable();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Suas turmas", scrollPaneTeachingLectures);

        tableJPanel.add(tabbedPane, BorderLayout.CENTER);

        gridConstraints.gridx = 2;
        gridConstraints.gridy = 1;
        gridConstraints.insets = new Insets(10, 10, 10, 10);
        gridConstraints.anchor = GridBagConstraints.EAST;
        rightSidePanel.add(Util.THEME_BUTTON, gridConstraints);


        gridConstraints.gridx = 0;
        gridConstraints.gridy = 2;
        gridConstraints.weightx = 1;
        gridConstraints.weighty = 1;
        gridConstraints.anchor = GridBagConstraints.CENTER;
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.gridwidth = 3;
        gridConstraints.insets = new Insets(10, 10, 10, 10);
        rightSidePanel.add(tableJPanel, gridConstraints);
    }

    private JScrollPane initTeachingLecturesTable() {
        teachingTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            protected void createDefaultRenderers() {
                super.createDefaultRenderers();
            }
        };
        initTeachingLecturesData();
        teachingTable.setColumnSelectionAllowed(true);
        teachingTable.setShowGrid(false);
        teachingTable.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = teachingTable.rowAtPoint(e.getPoint());
                int col = teachingTable.columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object value = teachingTable.getValueAt(row, col);
                    if (null != value && !"".equals(value)) {
                        teachingTable.setToolTipText(value.toString());// floating display cell content
                    } else {
                        teachingTable.setToolTipText(null);
                    }
                }
            }
        });
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        teachingTable.setDefaultRenderer(String.class, centerRenderer);
        teachingTable.setFont(teachingTable.getFont().deriveFont(18f));
        teachingTable.setRowHeight(teachingTable.getFont().getSize() * 4);
        teachingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        teachingTable.getTableHeader().setReorderingAllowed(false);
        teachingTable.setCellSelectionEnabled(false);
        teachingTable.setDragEnabled(false);
        teachingTable.setFillsViewportHeight(true);

        return new JScrollPane(teachingTable);
    }

    private void initTeachingLecturesData() {
        String[] columnNames = {"Nome da Disciplina", "Hor\u00E1rio"};

        List<Lecture> teachingLectures = ((Teacher) user).getLectures();

        Object[][] data = new Object[teachingLectures.size()][columnNames.length];
        for (int i = 0; i < teachingLectures.size(); i++) {
            Lecture lecture = teachingLectures.get(i);
            data[i][0] = lecture.getSubject().getName();
            data[i][1] = lecture.getSchedule();

            LOGGER.debug(String.format("[Table] Inserting teaching lecture in line %d: %s %s", i, data[i][0], data[i][1]));
        }

        teachingTable.setModel(new DefaultTableModel(data, columnNames));
    }

    private void initStudentRightSide(JPanel rightSidePanel, JPanel tableJPanel) {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        JScrollPane scrollPaneEnrollLecture = initEnrollLecturesTable();
        JScrollPane scrollPaneRequestedLectures = initRequestedLectures();
        JScrollPane scrollPaneConcludedLectures = initConcludedSubjects();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Turmas Matriculadas", scrollPaneEnrollLecture);
        tabbedPane.addTab("Turmas Pr\u00E9-Matriculadas", scrollPaneRequestedLectures);
        tabbedPane.addTab("Disciplinas Conclu\u00EDdas", scrollPaneConcludedLectures);

        tableJPanel.add(tabbedPane, BorderLayout.CENTER);

        gridConstraints.gridx = 2;
        gridConstraints.gridy = 1;
        gridConstraints.insets = new Insets(10, 10, 10, 10);
        gridConstraints.anchor = GridBagConstraints.EAST;
        rightSidePanel.add(Util.THEME_BUTTON, gridConstraints);


        gridConstraints.gridx = 0;
        gridConstraints.gridy = 2;
        gridConstraints.weightx = 1;
        gridConstraints.weighty = 1;
        gridConstraints.anchor = GridBagConstraints.CENTER;
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.gridwidth = 3;
        gridConstraints.insets = new Insets(10, 10, 10, 10);
        rightSidePanel.add(tableJPanel, gridConstraints);
    }

    private JScrollPane initConcludedSubjects() {
        concludedTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            protected void createDefaultRenderers() {
                super.createDefaultRenderers();
            }
        };
        initConcludedSubjectsData();
        concludedTable.setColumnSelectionAllowed(true);
        concludedTable.setShowGrid(false);
        concludedTable.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = concludedTable.rowAtPoint(e.getPoint());
                int col = concludedTable.columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object value = concludedTable.getValueAt(row, col);
                    if (null != value && !"".equals(value)) {
                        concludedTable.setToolTipText(value.toString());// floating display cell content
                    } else {
                        concludedTable.setToolTipText(null);
                    }
                }
            }
        });
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        concludedTable.setDefaultRenderer(String.class, centerRenderer);
        concludedTable.setFont(concludedTable.getFont().deriveFont(18f));
        concludedTable.setRowHeight(concludedTable.getFont().getSize() * 4);
        concludedTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        concludedTable.getTableHeader().setReorderingAllowed(false);
        concludedTable.setCellSelectionEnabled(false);
        concludedTable.setDragEnabled(false);
        concludedTable.setFillsViewportHeight(true);

        return new JScrollPane(concludedTable);
    }

    private JScrollPane initRequestedLectures() {

        requestedTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            protected void createDefaultRenderers() {
                super.createDefaultRenderers();
            }
        };
        initRequestedLecturesData();
        requestedTable.setColumnSelectionAllowed(true);
        requestedTable.setShowGrid(false);
        requestedTable.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = requestedTable.rowAtPoint(e.getPoint());
                int col = requestedTable.columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object value = requestedTable.getValueAt(row, col);
                    if (null != value && !"".equals(value)) {
                        requestedTable.setToolTipText(value.toString());// floating display cell content
                    } else {
                        requestedTable.setToolTipText(null);
                    }
                }
            }
        });
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        requestedTable.setDefaultRenderer(String.class, centerRenderer);
        requestedTable.setFont(requestedTable.getFont().deriveFont(18f));
        requestedTable.setRowHeight(requestedTable.getFont().getSize() * 4);
        requestedTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        requestedTable.getTableHeader().setReorderingAllowed(false);
        requestedTable.setCellSelectionEnabled(false);
        requestedTable.setDragEnabled(false);
        requestedTable.setFillsViewportHeight(true);

        return new JScrollPane(requestedTable);
    }

    private JScrollPane initEnrollLecturesTable() {
        enrolledTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            protected void createDefaultRenderers() {
                super.createDefaultRenderers();
            }
        };
//        table.getModel().addTableModelListener(e -> {
//            int row = e.getFirstRow();
//            int column = e.getColumn();
//            if (column == 4) {
//                TableModel model = (TableModel) e.getSource();
//                String columnName = model.getColumnName(column);
//                Boolean checked = (Boolean) model.getValueAt(row, column);
//                if (checked) {
//                    System.out.println(columnName + ": " + true);
//                } else {
//                    System.out.println(columnName + ": " + false);
//                }
//            }
//        });
        initEnrolledLecturesData();
        enrolledTable.setColumnSelectionAllowed(true);
        enrolledTable.setShowGrid(false);
        enrolledTable.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = enrolledTable.rowAtPoint(e.getPoint());
                int col = enrolledTable.columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object value = enrolledTable.getValueAt(row, col);
                    if (null != value && !"".equals(value)) {
                        enrolledTable.setToolTipText(value.toString());// floating display cell content
                    } else {
                        enrolledTable.setToolTipText(null);
                    }
                }
            }
        });
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        enrolledTable.setDefaultRenderer(String.class, centerRenderer);
        enrolledTable.setFont(enrolledTable.getFont().deriveFont(18f));
        enrolledTable.setRowHeight(enrolledTable.getFont().getSize() * 4);
        enrolledTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        enrolledTable.getTableHeader().setReorderingAllowed(false);
        enrolledTable.setCellSelectionEnabled(false);
        enrolledTable.setDragEnabled(false);
        enrolledTable.setFillsViewportHeight(true);

        return new JScrollPane(enrolledTable);
    }

    public static void resizeColumnsWidth(JTable table, Dimension dimension) {
        final TableColumnModel columnModel = table.getColumnModel();
        float namePercentage = 0.50f;
        columnModel.getColumn(0).setPreferredWidth((int) (dimension.getWidth() * namePercentage));
        for (int columnIndex = 1; columnIndex < table.getColumnCount(); columnIndex++) {
            columnModel.getColumn(columnIndex).setPreferredWidth((int) (dimension.getWidth() * ((1.0 - namePercentage) / 2)));
        }
    }

    public void refreshData() {
        initRequestedLecturesData();
        initEnrolledLecturesData();
        initConcludedSubjectsData();
    }

    private void initConcludedSubjectsData() {
        String[] columnNames = {"C\u00F3digo", "Nome", "Descri\u00E7\u00E3o"};

        List<Subject> concludedSubjects = ((Student) user).getConcludedSubjects();

        Object[][] data = new Object[concludedSubjects.size()][columnNames.length];
        for (int i = 0; i < concludedSubjects.size(); i++) {
            Subject lecture = concludedSubjects.get(i);
            data[i][0] = lecture.getCode();
            data[i][1] = lecture.getName();
            data[i][2] = lecture.getDescription();

            LOGGER.debug(String.format("[Table] Inserting concluded subjects in line %d: %s %s %s", i, data[i][0], data[i][1], data[i][2]));
        }
        concludedTable.setModel(new DefaultTableModel(data, columnNames));
    }

    private void initEnrolledLecturesData() {
        String[] columnNames = {"Nome da Disciplina", "Professor", "Hor\u00E1rio"};

        List<Lecture> enrollLectures = ((Student) user).getEnrollLectures();

        Object[][] data = new Object[enrollLectures.size()][columnNames.length];
        for (int i = 0; i < enrollLectures.size(); i++) {
            Lecture lecture = enrollLectures.get(i);
            data[i][0] = lecture.getSubject().getName();
            data[i][1] = lecture.getTeacher().getName();
            data[i][2] = lecture.getSchedule();

            LOGGER.debug(String.format("[Table] Inserting enroll lecture in line %d: %s %s %s", i, data[i][0], data[i][1], data[i][2]));
        }
        enrolledTable.setModel(new DefaultTableModel(data, columnNames));
    }

    private void initRequestedLecturesData() {
        String[] columnNames = {"Nome da Disciplina", "Professor", "Hor\u00E1rio"};

        List<Lecture> requestedLectures = ((Student) user).getRequestedLectures();

        Object[][] data = new Object[requestedLectures.size()][columnNames.length];
        for (int i = 0; i < requestedLectures.size(); i++) {
            Lecture lecture = requestedLectures.get(i);
            data[i][0] = lecture.getSubject().getName();
            data[i][1] = lecture.getTeacher().getName();
            data[i][2] = lecture.getSchedule();

            LOGGER.debug(String.format("[Table] Inserting requested lecture in line %d: %s %s %s", i, data[i][0], data[i][1], data[i][2]));
        }

        requestedTable.setModel(new DefaultTableModel(data, columnNames));
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setSize(this.width, this.height);
        this.setMinimumSize(new Dimension(this.width, this.height));
        this.setTitle(this.frameTitle);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setIconImage(new ImageIcon(requireNonNull(this.getClass().getClassLoader().getResource("bemtevi.png"))).getImage());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LOGGER.info("Closing LoginFrame window");
            }
        });
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if (concludedTable != null) {
                    resizeColumnsWidth(concludedTable, concludedTable.getSize());
                }
            }
        });
        centreWindow(this);
//        this.setResizable(false);
    }

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

}
