package htw.gui;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;
import htw.HuntTheWumpus.Direction;
import htw.factory.HtwFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static htw.HuntTheWumpus.Direction.EAST;
import static htw.HuntTheWumpus.Direction.NORTH;
import static htw.HuntTheWumpus.Direction.SOUTH;
import static htw.HuntTheWumpus.Direction.WEST;

public class Main extends JPanel implements HtwMessageReceiver {

    private static HuntTheWumpus game;
    private static List<String> caverns = new ArrayList<>();

    private JPanel warningPanel;
    private JPanel cavernControlPanel;
    private JPanel historyPanel;
    private JPanel buttonPanel;

    private JTextArea historyTextArea;
    private JTextArea warningTextArea;

    private JRadioButton moveRadioButton = new JRadioButton("Move");
    private JRadioButton shootRadioButton = new JRadioButton("Shoot");
    private JRadioButton addPitCoverRadioButton = new JRadioButton("Add Pit Cover");

    private JSeparator quiverSeparator = getVerticalSeparator();
    private JSeparator healthSeparator = getVerticalSeparator();

    private JLabel currentCavernLabel = new JLabel();
    private JLabel quiverCount = new JLabel();
    private JLabel healthLabel = new JLabel();

    private JButton northButton = new JButton();
    private JButton southButton = new JButton();
    private JButton westButton = new JButton();
    private JButton eastButton = new JButton();
    private JButton startButton = new JButton("Start");
    private JButton quitButton = new JButton("Quit");

    private static List<String> warnings = new ArrayList<>();
    private static List<Direction> passageDirections = new ArrayList<>();
    private static boolean playerMovesToWumpus = false;
    private static boolean wumpusMovesToPlayer = false;
    private static boolean playerKillsWumpus = false;
    private static int health = 10;
    private static Direction pitCoverDirection;

    private static final String[] environments = new String[]{
            "bright",
            "humid",
            "dry",
            "creepy",
            "ugly",
            "foggy",
            "hot",
            "cold",
            "drafty",
            "dreadful"
    };

    private static final String[] shapes = new String[]{
            "round",
            "square",
            "oval",
            "irregular",
            "long",
            "craggy",
            "rough",
            "tall",
            "narrow"
    };

    private static final String[] cavernTypes = new String[]{
            "cavern",
            "room",
            "chamber",
            "catacomb",
            "crevasse",
            "cell",
            "tunnel",
            "passageway",
            "hall",
            "expanse"
    };

    private static final String[] adornments = new String[]{
            "smelling of sulphur",
            "with engravings on the walls",
            "with a bumpy floor",
            "",
            "littered with garbage",
            "spattered with guano",
            "with piles of Wumpus droppings",
            "with bones scattered around",
            "with a corpse on the floor",
            "that seems to vibrate",
            "that feels stuffy",
            "that fills you with dread"
    };

    public Main() {
        super(new BorderLayout());
        add(createWarningsPanel(), BorderLayout.NORTH);
        add(getCavernPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        initializeNavigationButtons();
        disableAllComponents();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Hunt The Wumpus");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent newContentPane = new Main();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel getCavernPanel() {
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(2, 1));
        gamePanel.add(createCavernControlPanel(), BorderLayout.CENTER);
        gamePanel.add(createHistoryPanel());
        return gamePanel;
    }

    private JPanel createWarningsPanel() {
        initializeWarningPanel();
        initializeWarningPanelTextArea();
        warningPanel.add(warningTextArea, BorderLayout.WEST);
        return warningPanel;
    }

    private void initializeWarningPanel() {
        warningPanel = new JPanel();
        warningPanel.setLayout(new BorderLayout());
        warningPanel.setBorder(BorderFactory.createTitledBorder("Warnings"));
        warningPanel.setPreferredSize(new Dimension(500, 100));
    }

    private void initializeWarningPanelTextArea() {
        warningTextArea = new JTextArea(5, 30);
        warningTextArea.setEditable(false);
        warningTextArea.setForeground(Color.RED);
        warningTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        warningTextArea.setOpaque(false);
        warningTextArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
    }

    private void initializeNavigationButtons() {
        initializeNavigationButtonIcons();
        initializeNavigationButtonListeners();
        setNavigationButtonLabels();
        setNavigationButtonToolTips();
    }

    private void initializeNavigationButtonIcons() {
        northButton.setIcon(new ImageIcon(getClass().getResource("images/up24.gif")));
        southButton.setIcon(new ImageIcon(getClass().getResource("images/down24.gif")));
        eastButton.setIcon(new ImageIcon(getClass().getResource("images/right24.gif")));
        westButton.setIcon(new ImageIcon(getClass().getResource("images/left24.gif")));
    }

    private void initializeNavigationButtonListeners() {
        northButton.addActionListener(e -> navigationButtonHandler(Direction.NORTH));
        southButton.addActionListener(e -> navigationButtonHandler(Direction.SOUTH));
        eastButton.addActionListener(e -> navigationButtonHandler(Direction.EAST));
        westButton.addActionListener(e -> navigationButtonHandler(Direction.WEST));
    }

    private void navigationButtonHandler(Direction direction) {
        if (moveRadioButton.isSelected())
            moveButtonHandler(direction);
        else if (shootRadioButton.isSelected())
            shootButtonHandler(direction);
        else if (addPitCoverRadioButton.isSelected())
            addPitCoverButtonHandler(direction);
    }

    private void setNavigationButtonLabels() {
        northButton.setText(getNavigationButtonLabel(Direction.NORTH));
        southButton.setText(getNavigationButtonLabel(Direction.SOUTH));
        eastButton.setText(getNavigationButtonLabel(Direction.EAST));
        westButton.setText(getNavigationButtonLabel(Direction.WEST));
    }

    private String getNavigationButtonLabel(Direction direction) {
        String command;
        if (shootRadioButton.isSelected())
            command = "Shoot";
        else if (addPitCoverRadioButton.isSelected())
            command = "Add Pit Cover";
        else
            command = "Move";
        return command + " " + direction.name();
    }

    private void setNavigationButtonToolTips() {
        northButton.setToolTipText(getNavigationButtonToolTip(Direction.NORTH));
        southButton.setToolTipText(getNavigationButtonToolTip(Direction.SOUTH));
        eastButton.setToolTipText(getNavigationButtonToolTip(Direction.EAST));
        westButton.setToolTipText(getNavigationButtonToolTip(Direction.WEST));
    }

    private String getNavigationButtonToolTip(Direction direction) {
        String toolTip;
        if (shootRadioButton.isSelected())
            toolTip = "Shoot an arrow in the " + direction.name() + " direction";
        else if (addPitCoverRadioButton.isSelected())
            toolTip = "Add a pit cover in the " + direction.name() + " direction";
        else
            toolTip = "Move the player in the " + direction.name() + " direction";
        return toolTip;
    }

    private void moveButtonHandler(Direction direction) {
        warnings.clear();
        passageDirections.clear();
        game.makeMoveCommand(direction).execute();
        updateHistory("Moved " + direction.name() + " to cavern \"" + game.getPlayerCavern() + "\"");
        updatePresentation();
    }

    private void updateHistory(String message) {
        historyTextArea.append(message + "\n");
        historyTextArea.validate();
    }

    private void shootButtonHandler(Direction direction) {
        warnings.clear();
        updateHistory("Shot in direction " + direction.name());
        game.makeShootCommand(direction).execute();
        updatePresentation();
    }

    private void addPitCoverButtonHandler(Direction direction) {
        game.makeAddPitCoverCommand(direction).execute();
        addPitCoverRadioButton.setEnabled(false);
        moveRadioButton.doClick();
        if (pitCoverDirection != null) {
            updateHistory("Pit cover added to cavern to the " + direction);
            pitCoverDirection = null;
        }
    }

    private void updatePresentation() {
        if (playerKillsWumpus)
            gameOverWon("You killed the Wumpus.");
        else if (health <= 0)
            gameOverLose("You have died from your wounds");
        else if (playerMovesToWumpus)
            gameOverLose("You walked into the waiting arms of the Wumpus.");
        else if (wumpusMovesToPlayer)
            gameOverLose("The Wumpus has found you.");
        else
            updatePanels();
    }

    private void updatePanels() {
        updateCurrentCavern();
        enableButtonsForExistingPassages();
        updateQuiver();
        updateWarnings();
        updateHealth();
        showSeparators();
    }

    private void gameOverWon(String message) {
        updateHistory(message);
        disableAllComponents();
        warningTextArea.setText("You won! " + message);
    }

    private void gameOverLose(String message) {
        updateHistory(message);
        disableAllComponents();
        warningTextArea.setText("You lost. " + message);
    }

    private void updateCurrentCavern() {
        currentCavernLabel.setText(game.getPlayerCavern());
        setBoldItalicFont(currentCavernLabel);
    }

    private void setBoldItalicFont(JLabel label) {
        Font font = new Font(label.getFont().getName(), Font.ITALIC + Font.BOLD, label.getFont().getSize());
        label.setFont(font);
    }

    private void enableButtonsForExistingPassages() {
        northButton.setEnabled(passageDirections.contains(Direction.NORTH));
        southButton.setEnabled(passageDirections.contains(Direction.SOUTH));
        eastButton.setEnabled(passageDirections.contains(Direction.EAST));
        westButton.setEnabled(passageDirections.contains(Direction.WEST));
    }

    private void updateQuiver() {
        quiverCount.setText(String.format("Quiver Count: %d  ", game != null ? game.getQuiver() : 0));
        if (game.getQuiver() <= 0) {
            shootRadioButton.setEnabled(false);
            moveRadioButton.doClick();
        }
        else {
            shootRadioButton.setEnabled(true);
        }
    }

    private void updateWarnings() {
        warningTextArea.setText("");
        if (warnings.size() > 0) {
            for (String warning : warnings) {
                warningTextArea.append(warning + "\n");
            }
        } else {
            warningTextArea.append("No warnings at this time.");
        }
    }

    private void updateHealth() {
        healthLabel.setText(String.format("Health: %d", health));
    }

    private void showSeparators() {
        quiverSeparator.setVisible(true);
        healthSeparator.setVisible(true);
    }

    private JPanel createCavernControlPanel() {
        cavernControlPanel = new JPanel();
        cavernControlPanel.setBorder(BorderFactory.createTitledBorder("Cavern"));
        cavernControlPanel.setLayout(new BorderLayout());
        cavernControlPanel.add(createCavernPanel(), BorderLayout.CENTER);
        cavernControlPanel.add(createOptionPanel(), BorderLayout.SOUTH);

        moveRadioButton.setSelected(true);

        return cavernControlPanel;
    }

    private JPanel createCavernPanel() {
        JPanel cavernPanel = new JPanel();
        cavernPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        Border paddingBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        currentCavernLabel.setBorder(paddingBorder);

        gbc.gridy = 0;
        gbc.gridx = 1;
        cavernPanel.add(northButton, gbc);

        gbc.gridy = 2;
        cavernPanel.add(southButton, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        cavernPanel.add(westButton, gbc);

        gbc.gridx++;
        cavernPanel.add(currentCavernLabel, gbc);

        gbc.gridx++;
        cavernPanel.add(eastButton, gbc);

        return cavernPanel;
    }

    private JPanel createOptionPanel() {
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        addComponentsToOptionPanel(optionPanel);
        createRadioButtonGroup();
        initializeRadioButtonListeners();
        return optionPanel;
    }

    public void addComponentsToOptionPanel(JPanel optionPanel) {
        optionPanel.add(moveRadioButton);
        optionPanel.add(shootRadioButton);
        optionPanel.add(addPitCoverRadioButton);
        optionPanel.add(quiverSeparator);
        optionPanel.add(quiverCount);
        optionPanel.add(healthSeparator);
        optionPanel.add(healthLabel);
    }

    private void createRadioButtonGroup() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(moveRadioButton);
        bg.add(shootRadioButton);
        bg.add(addPitCoverRadioButton);
    }

    private void initializeRadioButtonListeners() {
        moveRadioButton.addActionListener(e -> {setNavigationButtonLabels(); setNavigationButtonToolTips();});
        shootRadioButton.addActionListener(e -> {setNavigationButtonLabels(); setNavigationButtonToolTips();});
        addPitCoverRadioButton.addActionListener(e -> {setNavigationButtonLabels(); setNavigationButtonToolTips();});
    }

    private JSeparator getVerticalSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(10, 15));
        separator.setVisible(false);
        return separator;
    }

    private JPanel createHistoryPanel() {
        initializeHistoryPanel();
        initializeHistoryTextArea();
        historyPanel.add(getHistoryScrollPane(), BorderLayout.WEST);
        return historyPanel;
    }

    private void initializeHistoryPanel() {
        historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
    }

    private void initializeHistoryTextArea() {
        historyTextArea = new JTextArea(15, 80);
        historyTextArea.setEditable(false);
        historyTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        historyTextArea.setOpaque(false);
        historyTextArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
    }

    private JScrollPane getHistoryScrollPane() {
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        initializeStartButton();
        initializeQuitButton();

        buttonPanel.add(startButton);
        buttonPanel.add(quitButton);

        return buttonPanel;
    }

    private void initializeStartButton() {
        startButton.setPreferredSize(new Dimension(80, 40));
        startButton.addActionListener(e -> startGame());
    }

    private void initializeQuitButton() {
        quitButton.setPreferredSize(new Dimension(80, 40));
        quitButton.addActionListener(e -> System.exit(0));
    }

    private void disableAllComponents() {
        disableNavigationButtons();
        disableOptionRadioButtons();
    }

    private void disableNavigationButtons() {
        northButton.setEnabled(false);
        southButton.setEnabled(false);
        eastButton.setEnabled(false);
        westButton.setEnabled(false);
    }

    private void disableOptionRadioButtons() {
        moveRadioButton.setEnabled(false);
        shootRadioButton.setEnabled(false);
        addPitCoverRadioButton.setEnabled(false);
    }

    private void startGame() {
        startButton.setEnabled(false);
        initializeGame();
    }

    private void initializeGame() {
        game = HtwFactory.makeGame("htw.game.HuntTheWumpusGame", new Main());
        createMap();
        game.makeRestCommand().execute();
        updateHistory("Started in cavern \"" + game.getPlayerCavern() + "\"");
        enableOptionRadioButtons();
        updatePresentation();
    }

    private static void createMap() {
        int ncaverns = (int) (Math.random() * 30.0 + 10.0);
        while (ncaverns-- > 0)
            caverns.add(makeName());

        for (String cavern : caverns) {
            maybeConnectCavern(cavern, NORTH);
            maybeConnectCavern(cavern, SOUTH);
            maybeConnectCavern(cavern, EAST);
            maybeConnectCavern(cavern, WEST);
        }

        String playerCavern = anyCavern();
        game.setPlayerCavern(playerCavern);
        game.setWumpusCavern(anyOther(playerCavern));
        game.addBatCavern(anyOther(playerCavern));
        game.addBatCavern(anyOther(playerCavern));
        game.addBatCavern(anyOther(playerCavern));

        game.addPitCavern(anyOther(playerCavern));
        game.addPitCavern(anyOther(playerCavern));
        game.addPitCavern(anyOther(playerCavern));

        game.setQuiver(5);
    }

    private static String makeName() {

        return "A " + chooseName(environments) + " " +
                chooseName(shapes) + " " +
                chooseName(cavernTypes) + " " +
                chooseName(adornments);
    }

    private static String chooseName(String[] names) {
        int n = names.length;
        int choice = (int) (Math.random() * (double) n);
        return names[choice];
    }

    private static void maybeConnectCavern(String cavern, Direction direction) {
        if (Math.random() > .2) {
            String other = anyOther(cavern);
            connectIfAvailable(cavern, direction, other);
            connectIfAvailable(other, direction.opposite(), cavern);
        }
    }

    private static String anyCavern() {
        return caverns.get((int) (Math.random() * caverns.size()));
    }

    private static String anyOther(String cavern) {
        String otherCavern = cavern;
        while (cavern.equals(otherCavern)) {
            otherCavern = anyCavern();
        }
        return otherCavern;
    }

    private static void connectIfAvailable(String from, Direction direction, String to) {
        if (game.findDestination(from, direction) == null) {
            game.connectCavern(from, to, direction);
        }
    }

    private void enableOptionRadioButtons() {
        addPitCoverRadioButton.setEnabled(true);
        moveRadioButton.setEnabled(true);
        shootRadioButton.setEnabled(true);
    }

    public void noPassage() {
        throw new RuntimeException("noPassage");
    }

    public void hearBats() {
        warnings.add("You hear chirping.");
    }

    public void hearPit() {
        warnings.add("You hear wind.");
    }

    public void smellWumpus() {
        warnings.add("There is a terrible smell.");
    }

    public void passage(Direction direction) {
        passageDirections.add(direction);
    }

    public void noArrows() {
    }

    public void arrowShot() {
    }

    public void playerShootsSelfInBack() {
        warnings.add("Ow! You shot yourself in the back.");
        health -= 3;
    }

    public void playerKillsWumpus() {
        playerKillsWumpus = true;
    }

    public void playerShootsWall() {
        warnings.add("You shot the wall and the ricochet hurt you.");
        health -= 3;
    }

    public void arrowsFound(Integer value) {
        warnings.add("You found " + value + " arrow(s)");
    }

    public void fellInPit() {
        warnings.add("You fell into a pit and hurt yourself");
        health -= 4;
    }

    public void playerMovesToWumpus() {
        playerMovesToWumpus = true;
    }

    public void wumpusMovesToPlayer() {
        wumpusMovesToPlayer = true;
    }

    public void batsTransport() {
    }

    public void addPitCoverToAdjacentCavern(Direction direction) {
        pitCoverDirection = direction;
    }

    public void noPitCover() {
    }

    public void cavernNotAdjacentForPitCover() {
    }
}
