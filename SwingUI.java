import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

public class SwingUI extends JFrame {
    private JTextField guessTextBox;
    private JLabel[] letterLabels;
    private JLabel[] alphaLabels;
    private JLabel invalidGuessLabel;
    private int guessNum = 0; // how many guesses made so far
    private String wordleWORD;
    private ArrayList<String> greenList = new ArrayList<String>();

    public void playSound(String type)
    {
        String file = "";
        if (type.equals("win"))
        {
            file = "win.wav";
        }
        else if (type.equals("invalid"))
        {
            file = "invalid.wav";
        }
        else
        {
            file = "lose.wav";
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    public SwingUI() {
        setTitle("Wordle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon img = new ImageIcon("wordle.png");
        setIconImage(img.getImage().getScaledInstance(800, 800, Image.SCALE_DEFAULT));
        try {
            wordleWORD = chooseAWordle();
        } catch (Exception e) {
            System.out.println("File not found.");
        }
        
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new GridLayout(6, 5, 5, 5));
        Border border = boxPanel.getBorder();
        Border margin = new EmptyBorder(10,10,10,10);
        boxPanel.setBorder(new CompoundBorder(border, margin));

        letterLabels = new JLabel[30];
        for (int i = 0; i < 30; i++) {
            letterLabels[i] = new JLabel();
            letterLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            letterLabels[i].setPreferredSize(new Dimension(50, 50));
            letterLabels[i].setFont(new Font("Desdemona", Font.PLAIN, 20));
            letterLabels[i].setBorder(new LineBorder(Color.BLACK));
            boxPanel.add(letterLabels[i]);
        }

        // text box for guessing
        guessTextBox = new JTextField();
        guessTextBox.setPreferredSize(new Dimension(250, 30));
        guessTextBox.setFont(new Font("Desdemona", Font.PLAIN, 15));
        guessTextBox.setEditable(true);

        JPanel guessPanel = new JPanel(new BorderLayout());
        guessPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        invalidGuessLabel = new JLabel("Invalid Guess");
        invalidGuessLabel.setFont(new Font("Desdemona", Font.PLAIN, 15));
        invalidGuessLabel.setForeground(getBackground());
        invalidGuessLabel.setHorizontalAlignment(SwingConstants.CENTER);
        invalidGuessLabel.setVisible(true);

        JPanel invalidPanel = new JPanel(new BorderLayout());
        invalidPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        invalidPanel.add(invalidGuessLabel);

        // alphabet to visualize letters used
        JPanel alphabetPanel = new JPanel();
        alphabetPanel.setLayout(new GridLayout(3, 9, 5, 5)); // three rows, nine columns
        alphabetPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        guessTextBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // check if guess is invalid
                String guess = guessTextBox.getText().toLowerCase();
                if (!guess.isEmpty()) {
                    try {
                        if (checkIfInGuessList(guess)) { // normal
                            updateLetters(guess, guessNum);
                            guessTextBox.setText(""); // clear
                            guessNum += 1;
                            invalidGuessLabel.setForeground(getBackground());
                            if (hasWon(guess)) {
                                guessTextBox.setEditable(false);
                                // TRY vs. TRIES
                                if (guessNum > 1) {
                                    playSound("win");
                                    invalidGuessLabel.setText("<html>Congratulations! You guessed the wordle in " + guessNum + " tries.<html>");
                                    invalidGuessLabel.setForeground(Color.MAGENTA);
                                    invalidGuessLabel.setVisible(true);
                                    
                                } else {
                                    playSound("win");
                                    invalidGuessLabel.setText("<html>Congratulations! You guessed the wordle in " + guessNum + " try.<html>");
                                    invalidGuessLabel.setForeground(Color.MAGENTA);
                                    invalidGuessLabel.setVisible(true);                                    
                                }
                            }
                            if (guessNum > 5 && !hasWon(guess)) {
                                playSound("lose");
                                guessTextBox.setEditable(false);
                                invalidGuessLabel.setText("<html>You have exceeded guesses. Try again next time. The word was: " + wordleWORD + ".</html>");
                                invalidGuessLabel.setForeground(Color.RED);
                                invalidGuessLabel.setVisible(true);
                            }
                        } else {
                            // show error message
                            playSound("invalid");
                            invalidGuessLabel.setForeground(Color.RED);
                            invalidGuessLabel.setVisible(true);

                        }
                    } catch (Exception ex) {
                        System.out.println("THERE'S A BUG IN THE CODE");
                    }
                }
            }
        });

        alphaLabels = new JLabel[27];
        Color gray = new Color(162, 186, 168);
        String alphabetString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ";
        for (int i = 0; i < 27; i++) {
            alphaLabels[i] = new JLabel();
            alphaLabels[i].setText(alphabetString.substring(i, i + 1));
            alphaLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            alphaLabels[i].setPreferredSize(new Dimension(20, 20));
            alphaLabels[i].setFont(new Font("Optima", Font.BOLD, 12));
            alphaLabels[i].setBorder(new LineBorder(gray));
            alphabetPanel.add(alphaLabels[i]);
        }
        guessPanel.add(guessTextBox, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boxPanel, BorderLayout.NORTH);
        mainPanel.add(guessPanel, BorderLayout.CENTER);
        mainPanel.add(alphabetPanel, BorderLayout.SOUTH);

        JPanel marginPanel = new JPanel(new BorderLayout());
        marginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 60, 20));
        JLabel welcomeLabel = new JLabel("W  O  R  D  L  E");

        welcomeLabel.setFont(new Font("Desdemonda", Font.PLAIN, 20));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        marginPanel.add(welcomeLabel, BorderLayout.NORTH);
        marginPanel.add(mainPanel, BorderLayout.CENTER);
        marginPanel.add(invalidPanel, BorderLayout.SOUTH);

        add(marginPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // update letters based on user's guesses
    public void updateLetters(String guess, int guessNum) {
        Color yellow = new Color(255, 208, 16);
        Color green = new Color(53, 171, 92);
        Color black = Color.BLACK;

        ArrayList<String> colorList = colorize(guess);

        for (int i = 5 * guessNum; i < 5 * guessNum + 5 && i < letterLabels.length; i++)
        {
            int colorIndex = i - 5 * guessNum;
            String alphabetString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ";
            String currLetter = guess.substring(i % 5, i % 5 + 1);
            int currInd = alphabetString.indexOf(currLetter.toUpperCase());
            if (colorIndex < colorList.size())
            {
                String color = colorList.get(colorIndex);
                if (color.equals("green"))
                {
                    greenList.add(currLetter);
                    letterLabels[i].setForeground(green);
                    alphaLabels[currInd].setBackground(green);
                    alphaLabels[currInd].setOpaque(true);

                }
                else if (color.equals("yellow"))
                {
                    if (!(greenList.contains(currLetter)))
                    {
                        alphaLabels[currInd].setBackground(yellow);
                        alphaLabels[currInd].setOpaque(true);
                    }
                    letterLabels[i].setForeground(yellow);
                }
                else 
                {
                    letterLabels[i].setForeground(black);
                    alphaLabels[currInd].setBackground(black);
                    alphaLabels[currInd].setOpaque(true);
                }
            } else {
                letterLabels[i].setForeground(black);
            }

            letterLabels[i].setText(String.valueOf(guess.charAt(i - 5 * guessNum)));
        }
    }

    public boolean checkIfInGuessList(String guess) throws Exception {
        FileReader guessList = new FileReader("guessList.txt");
        BufferedReader readGuessList = new BufferedReader(guessList);
        HashSet<String> guessListArray = new HashSet<>();
        String line;
        while ((line = readGuessList.readLine()) != null) {
            guessListArray.add(line);
        }
        guessList.close();
        return guessListArray.contains(guess);
    }

    public String chooseAWordle() throws Exception {
        FileReader answerList = new FileReader("answerList.txt");
        BufferedReader readAnswerList = new BufferedReader(answerList);
        ArrayList<String> answerListArray = new ArrayList<>();
        String line;
        while ((line = readAnswerList.readLine()) != null) {
            answerListArray.add(line);
        }
        readAnswerList.close();
        int random = (int) (Math.random() * answerListArray.size());
        //System.out.println(answerListArray.get(random)); // PRINTING HERE FOR DEBUGGING
        return answerListArray.get(random);
    }

    public ArrayList<String> colorize(String guess) {
        ArrayList<String> colorList = new ArrayList<>();
        ArrayList<String> wordleWordList = new ArrayList<>();
        for (int let = 0; let < wordleWORD.length(); let++) {
            wordleWordList.add(wordleWORD.substring(let, let + 1));
        }

        for (int letterInd = 0; letterInd < guess.length(); letterInd++) {
            String color = "black";
            if (wordleWordList.contains(guess.substring(letterInd, letterInd + 1).toLowerCase())) {
                if (wordleWordList.get(letterInd).equalsIgnoreCase(guess.substring(letterInd, letterInd + 1))) {
                    color = "green";
                    wordleWordList.set(letterInd, "!");
                } else {
                    color = "yellow";
                    int realLocation = wordleWORD.indexOf(guess.substring(letterInd, letterInd + 1));
                    wordleWordList.set(realLocation, "!");
                }
            }
            colorList.add(color);
        }
        return colorList;
    }

    public boolean hasWon(String guess) {
        return guess.equals(wordleWORD);
    }

    public static void main(String[] args) {
        Runnable runWORDLE = new Runnable() {
            public void run() {
                new SwingUI();
            }
        };
        SwingUtilities.invokeLater(runWORDLE);
    }
}