package utilsUI;

import tracks.ArcadeMachine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;

import static utilsUI.Constants.DRAW_ET;
import static utilsUI.Constants.DRAW_EXPLORATION;
import static utilsUI.Constants.DRAW_THINKING;
import static utilsUI.HelperMethodsUI.*;

public class TestUI {
    public static boolean startGame = false, pauseGame = false, stopGame = false, closePlots = false;
    public static boolean drawHM = false, drawTH = false;
    public static boolean drawing = false;
    public static int drawingWhat = DRAW_ET; // move these to agent
    public static JButton pauseB;
    public static BufferedWriter resultWriter;
    public static File resultFile;

    public static void main(String[] args) {

        // General styling
        styleUI();

        // Setting up the JFrame
        JFrame frame = new JFrame("RHEA DEMO");
        frame.setLayout(new GridBagLayout());
        JTabbedPane mainPanel = new JTabbedPane();
        frame.getContentPane().setBackground(Color.black);

        // Create log dir and result writer
        File logDir = new File("logs");
        createDirs(logDir);
        try {
            resultFile = new File("logs/results.log");
            resultWriter = new BufferedWriter(new FileWriter(resultFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating things for parameter panel
        JPanel params = new JPanel();
        params.setLayout(new GridLayout(15, 4, 20, 0));
        ParameterSet paramSet = new ParameterSet();

        String[] p = paramSet.getParamNames();
        JLabel[] paramLabels = new JLabel[p.length];
        for (int i = 0; i < paramLabels.length; i++) {
            paramLabels[i] = new JLabel(p[i]);
            paramLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
        }

        String[] v = paramSet.getValues();
        Object[][] valueOptions = paramSet.getValueOptions();
        JComponent[] paramInputs = new JComponent[v.length];
        for (int i = 0; i < paramInputs.length; i++) {
            if (valueOptions[i].length == 0) {
                //it's a text field with default value in v
                paramInputs[i] = new JTextField(v[i], 10);
            } else {
                paramInputs[i] = new JComboBox<>(valueOptions[i]);
            }
        }

        // Adding things to parameter panel
        for (int i = 0; i < paramLabels.length; i++) {
            params.add(paramLabels[i]);
            params.add(paramInputs[i]);
        }

        // Create things for game panel
        JButton startB = new JButton("Step 2: Alg ready - Start!");
        startB.addActionListener(e -> startGame = true);
        pauseB = new JButton("Pause");
        pauseB.addActionListener(e -> pauseGame = !pauseGame);
        JButton stopB = new JButton("Stop");
        stopB.addActionListener(e -> stopGame = true);
        JButton closeB = new JButton("Close Plots");
        closeB.setToolTipText("Close all plot windows, ready for next run.");
        closeB.addActionListener(e -> closePlots = true);

        JComboBox gameOptions1 = new JComboBox<>(get1PGames());
        JComboBox gameOptions2 = new JComboBox<>(get2PGames());
        gameOptions2.setEnabled(false);
        Integer[] levels = new Integer[]{0, 1, 2, 3, 4};
        JComboBox levelOptions = new JComboBox<>(levels);
        String[] cons = new String[]{"Human", "DoNothing", "Random", "OneStepLookAhead", "Random Search", "RHEA", "MCTS"};
        JComboBox[] conOptions = new JComboBox[2];
        conOptions[0] = new JComboBox<>(cons);
        conOptions[1] = new JComboBox<>(cons);
        conOptions[1].setEnabled(false);
        String[] tracks = new String[]{"1P Planning", "2P Planning"};
        JComboBox trackOptions = new JComboBox<>(tracks);
//        String[] modes2P = new String[]{"1G x 1L x 1M", "1G x L x M", "N x L x M", "Round Robin"};
        String[] modes2P = new String[]{"1G x 1L x 1M", "Round Robin"};
        JComboBox mode2Options = new JComboBox<>(modes2P);
        mode2Options.setEnabled(false);
        String[] modes1P = new String[]{"1G x 1L x 1M"};
        JComboBox mode1Options = new JComboBox<>(modes1P);
        trackOptions.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                Object item = ev.getItem();
                if (item.equals(tracks[1])) {
                    mode1Options.setEnabled(false);
                    mode2Options.setEnabled(true);
                    gameOptions1.setEnabled(false);
                    gameOptions2.setEnabled(true);
                } else {
                    if (item.equals(tracks[0])) {
                        mode1Options.setEnabled(true);
                        mode2Options.setEnabled(false);
                        gameOptions1.setEnabled(true);
                        gameOptions2.setEnabled(false);
                    }
                }
            }
        });
        mode2Options.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                    Object item = ev.getItem();
                    if (item.equals(modes2P[1])) {
                        conOptions[0].setEnabled(false);
                        conOptions[1].setEnabled(false);
                    } else {
                        if (item.equals(modes2P[0])) {
                            conOptions[0].setEnabled(true);
                            conOptions[1].setEnabled(true);
                        }
                    }
                }
            });

        // Create result evo table
        DefaultTableModel evoResTableModel = createEvoResTableModel();
        JScrollPane evoResPanel = createEvoResPanel(evoResTableModel);

        // Create result game table
        DefaultTableModel gameResTableModel = createGameResTableModel();
        JScrollPane gameResPanel = createGameResPanel(gameResTableModel);

        // Toggle buttons for game overlay drawings
        JToggleButton jtb1 = new JToggleButton("Heatmap On/Off");
        jtb1.addItemListener(ev -> {
            if(ev.getStateChange()== ItemEvent.SELECTED){
                drawHM = true;
                drawing = true;
                if (drawTH) drawingWhat = DRAW_ET;
                else drawingWhat = DRAW_EXPLORATION;
            } else if(ev.getStateChange()==ItemEvent.DESELECTED){
                drawHM = false;
                if (!drawTH)
                    drawing = false;
                else
                    drawingWhat = DRAW_THINKING;
            }
        });

        JToggleButton jtb2 = new JToggleButton("Simulations On/Off");
        jtb2.addItemListener(ev -> {
            if(ev.getStateChange()==ItemEvent.SELECTED){
                drawTH = true;
                drawing = true;
                if (drawHM) drawingWhat = DRAW_ET;
                else drawingWhat = DRAW_THINKING;
            } else if(ev.getStateChange()==ItemEvent.DESELECTED){
                drawTH = false;
                if (!drawHM) {
                    drawing = false;
                }
                else
                    drawingWhat = DRAW_EXPLORATION;
            }
        });

        // Add all the things to the game panel
        JPanel gamePanel = getGamePanel(gameOptions1, gameOptions2, levelOptions, trackOptions, mode1Options, mode2Options, conOptions, startB,
                pauseB, stopB, closeB, evoResPanel, gameResPanel, jtb1, jtb2);

        // Create instructions panel
        JPanel instructions = new JPanel();
        JLabel insText = new JLabel("<html><div width=500>" +
                "<center><h1>How to use</h1></center></br><hr>" +
                "<ul><li>Step 1: Adjust game to play and level in \"Game\" tab</li>" +
                "<li>Step 2: Click the \"Game ready\" button</li>" +
                "<li>Step 3: Wait for plot windows to load, then continue.</li>" +
                "<li>Step 4: Adjust algorithm parameters in \"Parameters\" tab</li>" +
                "<li>Step 5: Click the \"Start\" button</li>" +
                "<li>Step 6: Watch the game play out and the different plots generated</li>" +
                "<li>Step 7: Pause/Resume the game using the \"Pause\" button</li>" +
                "<li>Step 8: Interrupt the game using the \"Stop\" button</li>" +
                "<li>Step 9: Analyze the final results in the tables and save plots</li>" +
                "<li>Step 10: Click the \"Close Plots\" button at the bottom of the screen</li>" +
                "<li>Repeat from step 1 with different settings. " +
                "</div></html>");
        instructions.add(insText);

        // Add tabs to main panel and panel to frame
        mainPanel.addTab("Game", gamePanel);
        mainPanel.addTab("Parameters", params);
        mainPanel.addTab("Instructions", instructions);

        frame.add(mainPanel);
        frame.pack();

        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        int id = 0;

        // Main loop to keep window open and playing games
        while (true) {


            // Reset frame contents
            frame.getContentPane().removeAll();
            mainPanel.removeAll();
            gamePanel = getGamePanel(gameOptions1, gameOptions2, levelOptions, trackOptions, mode2Options, mode2Options, conOptions, startB,
                    pauseB, stopB, closeB, evoResPanel, gameResPanel, jtb1, jtb2);
            mainPanel.addTab("Game", gamePanel);
            mainPanel.addTab("Parameters", params);
            mainPanel.addTab("Instructions", instructions);

            // Add all to frame
            frame.add(mainPanel);
            frame.getContentPane().revalidate();
            frame.getContentPane().repaint();
            frame.pack();

            while (!startGame) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }

            // Don't allow switching tabs while game is playing
            mainPanel.setEnabledAt(1, false);
            mainPanel.setEnabledAt(2, false);

            // Get params
            paramSet = setParamSet(paramInputs);

            //Play game with params
            try {
                // Get competition track (1p or 2p for now)
                int track = trackOptions.getSelectedIndex();

                // Get game(s) level(s) and repetitions to play
                int game_idx;
                String[] games;
                String gamespath;
                if (track == 0) {
                    game_idx = gameOptions1.getSelectedIndex(); // game index
                    games = get1PGames();
                    gamespath = "examples/gridphysics/";
                }
                else {
                    game_idx = gameOptions2.getSelectedIndex();
                    games = get2PGames();
                    gamespath = "examples/2player/";
                }
                int lvl_idx = levelOptions.getSelectedIndex();

                // Settings for game
                int seed = new Random().nextInt();
                String map = gamespath + games[game_idx] + ".txt";
                String level = gamespath + games[game_idx] + "_lvl" + lvl_idx + ".txt";

//                try {
//                    new FileWriter(new File(actionFile));
//                    new FileWriter(new File(evoFile));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                // Game execution mode params
                int N = 1;
                int L = 1;
                int M = 1;

                // Get execution mode
                // "1G x 1L x 1M", "1G x L x M", "N x L x M", "Round Robin"
                if (track == 1) {
                    int execMode = mode2Options.getSelectedIndex();
                    // Get controller(s)
                    String controller1 = getController(conOptions[0]);
                    String controller2 = getController(conOptions[1]);
                    String controllers = controller1 + " " + controller2;

                    // Log files
                    String actionFile1 = "logs/actions_" + conOptions[0].getSelectedItem() + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                    String actionFile2 = "logs/actions_" + conOptions[1].getSelectedItem() + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                    String evoFile1 = "logs/stats_" + conOptions[0].getSelectedItem() + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                    String evoFile2 = "logs/stats_" + conOptions[1].getSelectedItem() + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                    String actionFile = actionFile1 + " " + actionFile2;
                    String evoFile = evoFile1 + " " + evoFile2;
                    id++;

                    switch (execMode) {
                        case 1:
                            for (int i = 0; i < cons.length - 1; i++)
                                for (int j = i + 1; j < cons.length; j++) {
                                    controllers = getController(i) + " " + getController(j);

                                    // Log files
                                    actionFile1 = "logs/actions_" + cons[i] + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                                    actionFile2 = "logs/actions_" + cons[j] + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                                    evoFile1 = "logs/stats_" + cons[i] + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                                    evoFile2 = "logs/stats_" + cons[j] + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                                    actionFile = actionFile1 + " " + actionFile2;
                                    evoFile = evoFile1 + " " + evoFile2;
                                    id++;

                                    ArcadeMachine.runOneUIGame(frame, gamePanel, map, level, true, controllers,
                                            actionFile, evoFile, seed, 0, paramSet);
                                }
                            break;
                        case 0:
                            ArcadeMachine.runOneUIGame(frame, gamePanel, map, level, true, controllers,
                                    actionFile, evoFile, seed, 0, paramSet);
                        default:
                    }
                } else {
                    int execMode = mode1Options.getSelectedIndex();
                    // Get controller(s)
                    String controllers = getController(conOptions[0]);
                    // Log file
                    String actionFile = "logs/actions_" + conOptions[0].getSelectedItem() + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                    String evoFile = "logs/stats_" + conOptions[0].getSelectedItem() + "_" + game_idx + "_" + lvl_idx + "_" + id + ".log";
                    id++;
                    switch (execMode) {
                        case 1:
                            break;
                        case 0:
                            ArcadeMachine.runOneUIGame(frame, gamePanel, map, level, true, controllers,
                                    actionFile, evoFile, seed, 0, paramSet);
                        default:
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //End of run, reset and kill process
            startGame = false;
            stopGame = false;
            pauseGame = false;
            pauseB.setEnabled(false);
            stopB.setEnabled(false);

            // Display game result in frame
            parseResult(evoResTableModel, gameResTableModel, resultFile);
        }
    }

    private static void createDirs(File dir) {
        if (!dir.exists()) {
            boolean createdDirectory = dir.mkdir();
            if (!createdDirectory) {
                System.err.println("Failed in creating dir + " + dir.toString() + ". System exit.");
                System.exit(0);
            }
        }
    }

    private static String getController(JComboBox conOptions){
        return getController(conOptions.getSelectedIndex());
    }

    private static String getController(int idx){
        String controller;
        switch(idx) {
            case 1:
                controller = "tracks.multiPlayer.simple.doNothing.Agent"; break;
            case 2:
                controller = "tracks.multiPlayer.simple.sampleRandom.Agent"; break;
            case 3:
                controller = "tracks.multiPlayer.simple.sampleOneStepLookAhead.Agent"; break;
            case 4:
                controller = "tracks.multiPlayer.advanced.sampleRS.Agent"; break;
            case 5:
                controller = "tracks.multiPlayer.advanced.sampleRHEA.Agent"; break;
            case 6:
                controller = "tracks.multiPlayer.advanced.sampleMCTS.Agent"; break;
            case 0:
            default: controller = "tracks.multiPlayer.tools.human.Agent";
        }
        return controller;
    }

}
