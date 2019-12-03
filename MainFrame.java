package com.company;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;

    private JFileChooser fileChooser = null;

    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    private JCheckBoxMenuItem showSquareMenuItem;
    private JMenuItem showTurnLeftMenuItem;
    private JMenuItem showTurnRightMenuItem;

    private GraphicsDisplay display = new GraphicsDisplay();

    private boolean fileLoaded = false;

    public MainFrame() {
        super("Построение графиков функции на основе заранее подготовленных файлов");
        setSize(WIDTH, HEIGHT);

        Toolkit kit = Toolkit.getDefaultToolkit();

        setLocation((kit.getScreenSize().width - WIDTH) / 2,
                (kit.getScreenSize().height - HEIGHT) / 2);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);

        Action openGraphicsAction = new AbstractAction("Открыть файл") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    openGraphics(fileChooser.getSelectedFile());
                }
            }
        };

        fileMenu.add(openGraphicsAction);

        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);

        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };

        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);

        Action showMarkerAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };

        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkerAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);

        Action showSquareAction = new AbstractAction("Показывать замкнутые площади") {
            public void actionPerformed(ActionEvent ev) {
                display.setShowSquare(showSquareMenuItem.isSelected());
            }
        };

        showSquareMenuItem = new JCheckBoxMenuItem(showSquareAction);
        graphicsMenu.add(showSquareMenuItem);
        showSquareMenuItem.setSelected(true);

        graphicsMenu.addMenuListener(new GraphicsMenuListener());

        getContentPane().add(display, BorderLayout.CENTER);

    }

    protected void openGraphics(File selectedFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            ArrayList<String> strings = new ArrayList<String>();

            while (reader.ready()) {
                strings.add(reader.readLine());
            }

            Double[][] graphicsData = new Double[strings.size()][2];
            boolean[] pointsCon = new boolean[graphicsData.length];

            for (int i = 0; i < strings.size(); i++) {
                String[] str = strings.get(i).split(" ");
                for (int j = 0; j < str.length; j++) {
                    String s = str[j];
                    if (j == 1) {
                        str[j] = str[j].replace(".", "");
                        boolean f = true;
                        for (int h = 0; h < str[j].length() - 1; h++) {
                            if (str[j].charAt(h) > str[j].charAt(h + 1)) {
                                f = false;
                                break;
                            }
                        }
                        pointsCon[i] = f;
                    }
                    graphicsData[i][j] = Double.valueOf(s);
                }
            }

            if (graphicsData != null && graphicsData.length > 0) {
                fileLoaded = true;
                display.setPointCon(pointsCon);
                display.showGraphics(graphicsData);
                showTurnLeftMenuItem.setEnabled(true);
                showTurnRightMenuItem.setEnabled(true);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтеничя координа точек из файла",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private class GraphicsMenuListener implements MenuListener {
        public void menuSelected(MenuEvent e) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            showSquareMenuItem.setEnabled(fileLoaded);
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }
}